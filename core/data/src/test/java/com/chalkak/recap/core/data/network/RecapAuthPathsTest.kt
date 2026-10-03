package com.chalkak.recap.core.data.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecapAuthPathsTest {
    @Test
    fun `only the HTTPS API origin receives session credentials`() {
        assertTrue(
            RecapAuthPaths.shouldAttachAuth(
                "https://re-cap.duckdns.org/api/v1/home/summary".toHttpUrl(),
            ),
        )
        listOf(
            "https://thumbnail.example/api/v1/image",
            "http://re-cap.duckdns.org/api/v1/home/summary",
            "https://re-cap.duckdns.org:8443/api/v1/home/summary",
            "https://re-cap.duckdns.org.example/api/v1/home/summary",
        ).forEach { url ->
            assertFalse(RecapAuthPaths.shouldAttachAuth(url.toHttpUrl()), url)
        }
    }

    @Test
    fun `OAuth and refresh endpoints do not receive the app access token`() {
        listOf(
            "https://re-cap.duckdns.org/api/v1/auth/oauth/kakao",
            "https://re-cap.duckdns.org/api/v1/auth/refresh",
        ).forEach { url ->
            assertFalse(RecapAuthPaths.shouldAttachAuth(url.toHttpUrl()), url)
        }
    }
}
