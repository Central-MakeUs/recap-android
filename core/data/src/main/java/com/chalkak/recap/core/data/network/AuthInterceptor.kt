package com.chalkak.recap.core.data.network

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val sessionTokenStore: SessionTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!shouldAttachAuth(request)) {
            return chain.proceed(request)
        }

        val accessToken = sessionTokenStore.peekAccessToken()
        if (accessToken.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticatedRequest =
            request.newBuilder()
                .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$accessToken")
                .build()
        return chain.proceed(authenticatedRequest)
    }

    private fun shouldAttachAuth(request: Request): Boolean {
        val path = request.url.encodedPath
        if (!path.startsWith("/api/v1/")) return false
        if (path.contains("/api/v1/auth/oauth/")) return false
        if (path.endsWith("/api/v1/auth/refresh")) return false
        return true
    }

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
