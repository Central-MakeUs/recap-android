package com.chalkak.recap.core.data.network

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthInterceptorTest {
    private val sessionTokenStore = mockk<SessionTokenStore>()
    private val tokenRefreshCoordinator = mockk<TokenRefreshCoordinator>()

    private lateinit var interceptor: AuthInterceptor

    @BeforeEach
    fun setUp() {
        interceptor = AuthInterceptor(
            sessionTokenStore = sessionTokenStore,
            tokenRefreshCoordinator = tokenRefreshCoordinator,
        )
    }

    @Test
    fun `intercept refreshes when needed then attaches bearer token`() {
        coEvery { tokenRefreshCoordinator.refreshIfNeeded(force = false) } returns true
        every { sessionTokenStore.peekAccessToken() } returns "access-token"

        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder()
            .url("https://re-cap.duckdns.org/api/v1/home/summary")
            .build()
        every { chain.request() } returns request
        every { chain.proceed(any()) } answers {
            val proceeded = firstArg<Request>()
            Response.Builder()
                .request(proceeded)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        verify {
            chain.proceed(
                match { proceeded ->
                    proceeded.header("Authorization") == "Bearer access-token"
                },
            )
        }
        coVerify(exactly = 1) { tokenRefreshCoordinator.refreshIfNeeded(force = false) }
    }

    @Test
    fun `intercept skips refresh and auth header for refresh endpoint`() {
        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder()
            .url("https://re-cap.duckdns.org/api/v1/auth/refresh")
            .build()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody(null))
            .build()

        interceptor.intercept(chain)

        coVerify(exactly = 0) { tokenRefreshCoordinator.refreshIfNeeded(any()) }
        verify { chain.proceed(request) }
    }
}
