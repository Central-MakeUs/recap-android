package com.chalkak.recap.core.data.network

import okhttp3.HttpUrl

internal object RecapAuthPaths {
    fun shouldAttachAuth(url: HttpUrl): Boolean {
        val path = url.encodedPath
        if (!path.startsWith("/api/v1/")) return false
        if (path.contains("/api/v1/auth/oauth/")) return false
        if (path.endsWith("/api/v1/auth/refresh")) return false
        return true
    }
}
