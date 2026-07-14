package com.chalkak.recap.core.data.auth.remote

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    // 소셜 로그인/가입
    @POST("api/v1/auth/oauth/{provider}/login")
    suspend fun login(
        @Path("provider") provider: String,
        @Body body: OAuthLoginRequestDto,
    ): AuthTokenApiResponse

    // Access 토큰 재발급
    @POST("api/v1/auth/refresh")
    suspend fun refresh(
        @Body body: TokenRefreshRequestDto,
    ): AuthTokenApiResponse

    // 로그아웃
    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body body: LogoutRequestDto,
    ): AuthVoidApiResponse
}
