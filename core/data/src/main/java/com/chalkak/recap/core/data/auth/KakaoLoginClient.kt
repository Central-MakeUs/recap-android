package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthProvider
import com.chalkak.recap.core.model.auth.SocialAuthCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class KakaoLoginClient @Inject constructor(
    @get:KakaoNativeAppKey private val nativeAppKey: String,
) : SocialLoginClient {
    override val provider: AuthProvider = AuthProvider.Kakao

    override suspend fun login(context: Context): Result<SocialAuthCredential> {
        if (nativeAppKey.isBlank()) {
            return Result.failure(AuthException(AuthError.MissingKakaoNativeAppKey))
        }

        return if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            loginWithKakaoTalkOrAccount(context)
        } else {
            loginWithKakaoAccount(context)
        }
    }

    private suspend fun loginWithKakaoTalkOrAccount(
        context: Context,
    ): Result<SocialAuthCredential> =
        suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                when {
                    token != null -> continuation.resume(Result.success(token.toCredential()))
                    error is ClientError && error.reason == ClientErrorCause.Cancelled -> {
                        continuation.resume(Result.failure(AuthException(AuthError.Cancelled)))
                    }
                    error != null -> {
                        UserApiClient.instance.loginWithKakaoAccount(context) { accountToken, accountError ->
                            continuation.resume(toLoginResult(accountToken, accountError))
                        }
                    }
                    else -> continuation.resume(Result.failure(AuthException(AuthError.Unknown)))
                }
            }
        }

    private suspend fun loginWithKakaoAccount(
        context: Context,
    ): Result<SocialAuthCredential> =
        suspendCancellableCoroutine { continuation ->
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                continuation.resume(toLoginResult(token, error))
            }
        }

    private fun toLoginResult(
        token: OAuthToken?,
        error: Throwable?,
    ): Result<SocialAuthCredential> =
        when {
            token != null -> Result.success(token.toCredential())
            error is ClientError && error.reason == ClientErrorCause.Cancelled -> {
                Result.failure(AuthException(AuthError.Cancelled))
            }
            error != null -> Result.failure(AuthException(AuthError.ProviderUnavailable, error))
            else -> Result.failure(AuthException(AuthError.Unknown))
        }

    private fun OAuthToken.toCredential(): SocialAuthCredential =
        SocialAuthCredential(
            provider = AuthProvider.Kakao,
            accessToken = accessToken,
            idToken = idToken,
        )
}

class AuthException(
    val authError: AuthError,
    cause: Throwable? = null,
) : RuntimeException(cause)
