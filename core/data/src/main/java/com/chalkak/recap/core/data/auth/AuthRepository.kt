package com.chalkak.recap.core.data.auth

import android.content.Context
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
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

class AuthRepository @Inject constructor(
    private val kakaoLoginClient: KakaoLoginClient,
    private val authApi: AuthApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val sessionTokenStore: SessionTokenStore,
) {
    suspend fun signInWithKakao(context: Context): Result<AuthSignInResult> =
        kakaoLoginClient.login(context).fold(
            onSuccess = { credential -> loginWithServer(credential) },
            onFailure = { Result.failure(it) },
        )

    suspend fun getKakaoUserProfile(): Result<KakaoUserProfile> =
        kakaoLoginClient.fetchUserProfile()


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
        }
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
        return result
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
