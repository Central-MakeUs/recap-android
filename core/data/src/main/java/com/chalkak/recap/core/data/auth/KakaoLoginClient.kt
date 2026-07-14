package com.chalkak.recap.core.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import timber.log.Timber

class KakaoLoginClient @Inject constructor(
    @param:KakaoNativeAppKey private val nativeAppKey: String,
) : SocialLoginClient {
    override val provider: AuthProvider = AuthProvider.Kakao

    override suspend fun login(context: Context): Result<SocialAuthCredential> {
        if (nativeAppKey.isBlank()) {
            return Result.failure(AuthException(AuthError.MissingKakaoNativeAppKey))
        }

        val activity = context.findActivity()
            ?: return Result.failure(AuthException(AuthError.ProviderUnavailable))

        // 카카오 로그인 구현 예제와 동일한 조합:
        // 카카오톡 앱 로그인 우선 → (취소 제외) 실패 시 카카오계정(웹) 로그인
        // https://developers.kakao.com/docs/ko/kakaologin/android#kakaologin-sample
        return suspendCancellableCoroutine { continuation ->
            fun resumeAccountResult(token: OAuthToken?, error: Throwable?) {
                if (!continuation.isActive) return
                continuation.resume(toLoginResult(token, error))
            }

            if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                Timber.d("Kakao login: attempting KakaoTalk")
                UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                    if (!continuation.isActive) return@loginWithKakaoTalk

                    if (error != null) {
                        Timber.w(error, "KakaoTalk login failed")
                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 취소 처리
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            continuation.resume(Result.failure(AuthException(AuthError.Cancelled)))
                            return@loginWithKakaoTalk
                        }
                        // 카카오톡에 연결된 카카오계정이 없는 경우 등 → 카카오계정(웹) 로그인
                        Timber.d("Kakao login: falling back to Kakao Account")
                        UserApiClient.instance.loginWithKakaoAccount(activity) { accountToken, accountError ->
                            resumeAccountResult(accountToken, accountError)
                        }
                    } else if (token != null) {
                        Timber.d("KakaoTalk login succeeded")
                        continuation.resume(Result.success(token.toCredential()))
                    } else {
                        continuation.resume(Result.failure(AuthException(AuthError.Unknown)))
                    }
                }
            } else {
                Timber.d("Kakao login: KakaoTalk unavailable, using Kakao Account")
                UserApiClient.instance.loginWithKakaoAccount(activity) { accountToken, accountError ->
                    resumeAccountResult(accountToken, accountError)
                }
            }
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

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

class AuthException(
    val authError: AuthError,
    cause: Throwable? = null,
) : RuntimeException(cause)
