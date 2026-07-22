package com.chalkak.recap.core.data.network

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TokenAuthenticatorTest {
    private val sessionTokenStore = mockk<SessionTokenStore>()
    private val tokenRefreshCoordinator = mockk<TokenRefreshCoordinator>()

    private lateinit var authenticator: TokenAuthenticator

    @BeforeEach
    fun setUp() {
        authenticator = TokenAuthenticator(
            sessionTokenStore = sessionTokenStore,
            tokenRefreshCoordinator = tokenRefreshCoordinator,
        )
    }

    @Test
    fun `authenticate refreshes and retries with new access token on 401`() {
        every { sessionTokenStore.peekAccessToken() } returnsMany listOf("old-access", "new-access")
        coEvery { tokenRefreshCoordinator.refreshIfNeeded(force = true) } returns true

        val response = unauthorizedResponse(accessToken = "old-access")
        val retry = authenticator.authenticate(null, response)

        assertEquals("Bearer new-access", retry?.header("Authorization"))
    }

    @Test
    fun `authenticate reuses concurrent refresh without calling coordinator again`() {
        every { sessionTokenStore.peekAccessToken() } returns "new-access"

        val response = unauthorizedResponse(accessToken = "old-access")
        val retry = authenticator.authenticate(null, response)

        assertEquals("Bearer new-access", retry?.header("Authorization"))
        coVerify(exactly = 0) { tokenRefreshCoordinator.refreshIfNeeded(any()) }
    }

    @Test
    fun `authenticate returns null when refresh fails`() {
        every { sessionTokenStore.peekAccessToken() } returns "old-access"
        coEvery { tokenRefreshCoordinator.refreshIfNeeded(force = true) } returns false

        val response = unauthorizedResponse(accessToken = "old-access")
        assertNull(authenticator.authenticate(null, response))
    }

    @Test
    fun `authenticate skips auth refresh endpoints`() {
        val request = Request.Builder()
            .url("https://re-cap.duckdns.org/api/v1/auth/refresh")
            .header("Authorization", "Bearer old-access")
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()

        assertNull(authenticator.authenticate(null, response))
    }

    private fun unauthorizedResponse(accessToken: String): Response {
        val request = Request.Builder()
            .url("https://re-cap.duckdns.org/api/v1/home/summary")
            .header("Authorization", "Bearer $accessToken")
            .build()
        return Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()
    }
}
