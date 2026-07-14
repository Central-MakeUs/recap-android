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
    data object PendingServerIntegration : AuthSignInResult
}

sealed interface AuthError {
    data object MissingKakaoNativeAppKey : AuthError
    data object Cancelled : AuthError
    data object ProviderUnavailable : AuthError
    data object Unknown : AuthError
}
