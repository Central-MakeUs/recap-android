package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.data.auth.remote.AuthApi
import com.chalkak.recap.core.data.auth.remote.AuthPlatformDto
import com.chalkak.recap.core.data.auth.remote.OAuthLoginRequestDto
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthProvider
import com.chalkak.recap.core.model.auth.AuthSignInResult
import com.chalkak.recap.core.model.auth.SocialAuthCredential
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val kakaoLoginClient: KakaoLoginClient,
    private val authApi: AuthApi,
    private val deviceIdProvider: DeviceIdProvider,
) {
    suspend fun signInWithKakao(context: Context): Result<AuthSignInResult> =
        kakaoLoginClient.login(context).fold(
            onSuccess = { credential -> loginWithServer(credential) },
            onFailure = { Result.failure(it) },
        )

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

            when {
                response.success && response.data != null -> {
                    val tokens = response.data
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
        } catch (_: IOException) {
            Result.failure(AuthException(AuthError.Network))
        } catch (error: Throwable) {
            Result.failure(AuthException(AuthError.Unknown, error))
        }
    }

    private fun AuthProvider.toApiPath(): String =
        when (this) {
            AuthProvider.Kakao -> "kakao"
            AuthProvider.Apple -> "apple"
            AuthProvider.Email -> "email"
        }
}
