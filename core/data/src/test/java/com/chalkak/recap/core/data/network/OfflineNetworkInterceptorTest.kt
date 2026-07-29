package com.chalkak.recap.core.data.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class OfflineNetworkInterceptorTest {
    private val networkConnectivityMonitor = mockk<NetworkConnectivityMonitor>()
    private val interceptor = OfflineNetworkInterceptor(networkConnectivityMonitor)

    @Test
    fun `intercept throws when device has no active network`() {
        every { networkConnectivityMonitor.hasActiveNetwork() } returns false
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns Request.Builder()
            .url("https://re-cap.duckdns.org/api/v1/home/summary")
            .build()

        val error = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        assertEquals(
            OfflineNetworkInterceptor.NO_ACTIVE_NETWORK_MESSAGE,
            error.message,
        )
        verify(exactly = 0) { chain.proceed(any()) }
    }

    @Test
    fun `intercept proceeds when device has an active network`() {
        every { networkConnectivityMonitor.hasActiveNetwork() } returns true
        val chain = mockk<Interceptor.Chain>()
        val request = Request.Builder()
            .url("https://re-cap.duckdns.org/api/v1/home/summary")
            .build()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody())
            .build()

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        verify(exactly = 1) { chain.proceed(request) }
    }
}
