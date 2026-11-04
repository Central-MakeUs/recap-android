package com.chalkak.recap.core.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

internal object RecapAuthPaths {
    private val apiOrigin = RecapApiConfig.BASE_URL.toHttpUrl()

    fun shouldAttachAuth(url: HttpUrl): Boolean {
        if (url.scheme != apiOrigin.scheme || url.host != apiOrigin.host || url.port != apiOrigin.port) {
            return false
        }
        val path = url.encodedPath
        if (!path.startsWith("/api/v1/")) return false
        if (path.contains("/api/v1/auth/oauth/")) return false
        if (path.endsWith("/api/v1/auth/refresh")) return false
        return true
    }
}
