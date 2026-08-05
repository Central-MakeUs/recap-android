package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.data.LocalAppDataResetter
import com.chalkak.recap.core.data.account.AccountOwnerHasher
import com.chalkak.recap.core.data.account.AccountOwnerStore
import com.chalkak.recap.core.data.auth.remote.AuthApi
import com.chalkak.recap.core.data.auth.remote.AuthPlatformDto
import com.chalkak.recap.core.data.auth.remote.AuthTokenApiResponse
import com.chalkak.recap.core.data.auth.remote.LogoutRequestDto
import com.chalkak.recap.core.data.auth.remote.OAuthLoginRequestDto
import com.chalkak.recap.core.data.auth.remote.TokenRefreshRequestDto
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.network.SessionTokens
import com.chalkak.recap.core.data.network.mapHttpException
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthProvider
import com.chalkak.recap.core.model.auth.AuthSignInResult
import com.chalkak.recap.core.model.auth.KakaoUserProfile
import com.chalkak.recap.core.model.auth.SocialAuthCredential
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.ObservabilityKeys
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

class AuthRepository @Inject constructor(
    private val kakaoLoginClient: KakaoLoginClient,
    private val authApi: AuthApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val sessionTokenStore: SessionTokenStore,
    private val accountOwnerStore: AccountOwnerStore,
    private val accountOwnerHasher: AccountOwnerHasher,
    private val localAppDataResetter: LocalAppDataResetter,
    private val crashReporter: CrashReporter,
) {
    suspend fun signInWithKakao(context: Context): Result<AuthSignInResult> =
        kakaoLoginClient.login(context).fold(
            onSuccess = { credential ->
                reconcileAccountOwner().fold(
                    onSuccess = { loginWithServer(credential).alsoReportAuthFailure() },
                    onFailure = { error ->
                        reportAuthFailure(error)
                        Result.failure(error)
                    },
                )
            },
            onFailure = { error ->
                reportAuthFailure(error)
                Result.failure(error)
            },
        )

    suspend fun getKakaoUserProfile(): Result<KakaoUserProfile> =
        kakaoLoginClient.fetchUserProfile().alsoReportAuthFailure()

    /**
     * 카카오 user.id 해시로 로컬 데이터 소유자를 맞춘다.
     * 해시 없음/불일치면 계정 종속 로컬 데이터를 wipe한 뒤에만 새 해시를 저장한다.
     *
     * DataStore 읽기/쓰기와 wipe 실패는 모두 [Result.failure]로 돌려준다.
     * 호출 측이 `Result`만 다루므로 이 경로에서 예외가 새어 나가면 안 된다.
     */
    private suspend fun reconcileAccountOwner(): Result<Unit> {
        val profile = kakaoLoginClient.fetchUserProfile().getOrElse { return Result.failure(it) }
        return try {
            val hash = accountOwnerHasher.hashKakaoUserId(profile.id)
            if (accountOwnerStore.getHash() != hash) {
                localAppDataResetter.wipeAndRebindOwner(hash)
            }
            Result.success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Result.failure(AuthException(AuthError.Unknown, error))
        }
    }

    suspend fun refresh(): Result<AuthSignInResult.Success> {
        val refreshToken = sessionTokenStore.getRefreshToken()
            ?: return Result.failure(AuthException(AuthError.Unknown))

        return try {
            val response = authApi.refresh(
                body = TokenRefreshRequestDto(refreshToken = refreshToken),
            )
            mapTokenResponse(response)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: HttpException) {
            Result.failure(error.toAuthException())
        } catch (_: IOException) {
            Result.failure(AuthException(AuthError.Network))
        } catch (error: Throwable) {
            Result.failure(AuthException(AuthError.Unknown, error))
        }.alsoReportAuthFailure()
    }

    suspend fun logout(): Result<Unit> {
        val refreshToken = sessionTokenStore.getRefreshToken()
        if (refreshToken == null) {
            sessionTokenStore.clear()
            return Result.success(Unit)
        }

        val result = try {
            val response = authApi.logout(
                body = LogoutRequestDto(refreshToken = refreshToken),
            )
            when {
                response.success -> Result.success(Unit)
                response.error != null -> {
                    Result.failure(
                        AuthException(
                            AuthError.Server(
                                code = response.error.code,
                                message = response.error.message,
                            ),
                        ),
                    )
                }
                else -> Result.failure(AuthException(AuthError.Unknown))
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: HttpException) {
            Result.failure(error.toAuthException())
        } catch (_: IOException) {
            Result.failure(AuthException(AuthError.Network))
        } catch (error: Throwable) {
            Result.failure(AuthException(AuthError.Unknown, error))
        }
        // 서버 실패여도 로컬 세션은 비워 재로그인 가능하게 한다.
        sessionTokenStore.clear()
        return result.alsoReportAuthFailure()
    }

    private suspend fun loginWithServer(
        credential: SocialAuthCredential,
    ): Result<AuthSignInResult> {
        val deviceId = deviceIdProvider.getOrCreate()
        val providerPath = credential.provider.toApiPath()

        return try {
            val response = authApi.login(
                provider = providerPath,
                body = OAuthLoginRequestDto(
                    deviceId = deviceId,
                    providerToken = credential.accessToken,
                    platform = AuthPlatformDto.ANDROID,
                ),
            )
            mapTokenResponse(response).map { it }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: HttpException) {
            Result.failure(error.toAuthException())
        } catch (_: IOException) {
            Result.failure(AuthException(AuthError.Network))
        } catch (error: Throwable) {
            Result.failure(AuthException(AuthError.Unknown, error))
        }
    }

    private fun <T> Result<T>.alsoReportAuthFailure(): Result<T> {
        exceptionOrNull()?.let(::reportAuthFailure)
        return this
    }

    private fun reportAuthFailure(error: Throwable) {
        val authError = (error as? AuthException)?.authError ?: return
        when (authError) {
            is AuthError.Server -> {
                crashReporter.setCustomKey(ObservabilityKeys.AUTH_ERROR_CODE, authError.code)
                crashReporter.recordException(error)
            }
            AuthError.Network -> crashReporter.recordException(error)
            else -> Unit
        }
    }

    private suspend fun mapTokenResponse(
        response: AuthTokenApiResponse,
    ): Result<AuthSignInResult.Success> {
        val tokens = response.data
        return when {
            response.success && tokens != null -> {
                sessionTokenStore.save(
                    SessionTokens(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        accessTokenExpiresAt = tokens.accessTokenExpiresAt,
                    ),
                )
                Result.success(
                    AuthSignInResult.Success(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        accessTokenExpiresAt = tokens.accessTokenExpiresAt,
                    ),
                )
            }
            response.error != null -> {
                Result.failure(
                    AuthException(
                        AuthError.Server(
                            code = response.error.code,
                            message = response.error.message,
                        ),
                    ),
                )
            }
            else -> Result.failure(AuthException(AuthError.Unknown))
        }
    }

    private fun AuthProvider.toApiPath(): String =
        when (this) {
            AuthProvider.Kakao -> "kakao"
            AuthProvider.Email -> "email"
        }

    private fun HttpException.toAuthException(): AuthException {
        val remote = mapHttpException(this)
        return AuthException(
            AuthError.Server(code = remote.code, message = remote.message),
            cause = this,
        )
    }
}
