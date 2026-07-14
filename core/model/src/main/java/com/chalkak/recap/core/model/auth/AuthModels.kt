package com.chalkak.recap.core.model.auth

enum class AuthProvider {
    Kakao,
    Apple,
    Email,
}

data class SocialAuthCredential(
    val provider: AuthProvider,
    val accessToken: String,
    val idToken: String? = null,
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
