package com.chalkak.recap.core.data.network

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val sessionTokenStore: SessionTokenStore,
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!RecapAuthPaths.shouldAttachAuth(request.url)) {
            return chain.proceed(request)
        }

        runBlocking {
            tokenRefreshCoordinator.refreshIfNeeded(force = false)
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

    private companion object {
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
