package com.chalkak.recap.core.data.auth.remote

import com.chalkak.recap.core.data.network.ApiResponseDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class OAuthLoginRequestDto(
    val deviceId: String,
    val providerToken: String,
    val platform: AuthPlatformDto,
)

@Serializable
enum class AuthPlatformDto {
    @SerialName("IOS")
    IOS,

    @SerialName("ANDROID")
    ANDROID,
}

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: String,
)

@Serializable
data class TokenRefreshRequestDto(
    val refreshToken: String,
)

@Serializable
data class LogoutRequestDto(
    val refreshToken: String,
)

typealias AuthTokenApiResponse = ApiResponseDto<TokenResponseDto>

typealias AuthVoidApiResponse = ApiResponseDto<JsonElement?>
