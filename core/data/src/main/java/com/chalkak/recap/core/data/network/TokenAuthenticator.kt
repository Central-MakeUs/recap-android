package com.chalkak.recap.core.data.network

import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator @Inject constructor(
    private val sessionTokenStore: SessionTokenStore,
    private val tokenRefreshCoordinator: TokenRefreshCoordinator,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (!RecapAuthPaths.shouldAttachAuth(response.request.url)) {
            return null
        }
        if (responseCount(response) >= MAX_AUTH_RETRIES) {
            return null
        }

        val failedAuthorization = response.request.header(HEADER_AUTHORIZATION)
        val currentAccessToken = sessionTokenStore.peekAccessToken()
        if (
            currentAccessToken != null &&
            failedAuthorization != "$BEARER_PREFIX$currentAccessToken"
        ) {
            // Access token already rotated by a concurrent refresh.
            return response.request.newBuilder()
                .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$currentAccessToken")
                .build()
        }

        val refreshed = runBlocking {
            tokenRefreshCoordinator.refreshIfNeeded(force = true)
        }
        if (!refreshed) return null

        val newAccessToken = sessionTokenStore.peekAccessToken()
        if (newAccessToken.isNullOrBlank()) return null
        if (failedAuthorization == "$BEARER_PREFIX$newAccessToken") return null

        return response.request.newBuilder()
            .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private companion object {
        const val MAX_AUTH_RETRIES = 2
        const val HEADER_AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
