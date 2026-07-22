package com.chalkak.recap.core.model.auth

import java.time.Instant

enum class AuthProvider {
    Kakao,
    Email,
}

data class SocialAuthCredential(
    val provider: AuthProvider,
    val accessToken: String,
    val idToken: String? = null,
)

data class KakaoUserProfile(
    val email: String?,
    val connectedAt: Instant?,
    val emailNeedsAgreement: Boolean = false,
)

sealed interface AuthSignInResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val accessTokenExpiresAt: String,
    ) : AuthSignInResult
}

sealed interface AuthError {
    data object MissingKakaoNativeAppKey : AuthError
    data object Cancelled : AuthError
    data object ProviderUnavailable : AuthError
    data class Server(
        val code: String,
        val message: String,
    ) : AuthError
    data object Network : AuthError
    data object Unknown : AuthError
}
