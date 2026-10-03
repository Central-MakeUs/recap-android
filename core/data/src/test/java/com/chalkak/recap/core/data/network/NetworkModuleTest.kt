package com.chalkak.recap.core.data.network

import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NetworkModuleTest {
    @Test
    fun `refresh completes while every protected API dispatcher slot is occupied`() {
        val offlineInterceptor = mockk<OfflineNetworkInterceptor>()
        every { offlineInterceptor.intercept(any()) } answers {
            val chain = firstArg<Interceptor.Chain>()
            chain.proceed(chain.request())
        }
        val authInterceptor = mockk<AuthInterceptor>()
        every { authInterceptor.intercept(any()) } answers {
            val chain = firstArg<Interceptor.Chain>()
            chain.proceed(chain.request())
        }
        val protectedCallsStarted = CountDownLatch(5)
        val releaseProtectedCalls = CountDownLatch(1)
        val protectedClient = NetworkModule.provideOkHttpClient(
            offlineInterceptor,
            authInterceptor,
            mockk<TokenAuthenticator>(),
        ).newBuilder()
            .addInterceptor { chain ->
                protectedCallsStarted.countDown()
                check(releaseProtectedCalls.await(10, TimeUnit.SECONDS))
                okResponse(chain.request())
            }
            .build()
        val refreshClient = NetworkModule.provideTokenRefreshClient(offlineInterceptor)
            .newBuilder()
            .addInterceptor { chain -> okResponse(chain.request()) }
            .build()

        try {
            val protectedResults = List(5) {
                protectedClient.enqueueResult("https://re-cap.duckdns.org/api/v1/home/summary")
            }
            assertTrue(protectedCallsStarted.await(5, TimeUnit.SECONDS))
            val refresh = refreshClient.enqueueResult(
                "https://re-cap.duckdns.org/api/v1/auth/refresh",
            )

            assertTrue(refresh.get(5, TimeUnit.SECONDS))
            releaseProtectedCalls.countDown()
            protectedResults.forEach { assertTrue(it.get(5, TimeUnit.SECONDS)) }
        } finally {
            releaseProtectedCalls.countDown()
            protectedClient.dispatcher.cancelAll()
            refreshClient.dispatcher.cancelAll()
            protectedClient.dispatcher.executorService.shutdownNow()
            refreshClient.dispatcher.executorService.shutdownNow()
            protectedClient.connectionPool.evictAll()
            refreshClient.connectionPool.evictAll()
        }
    }

    private fun OkHttpClient.enqueueResult(url: String): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                result.completeExceptionally(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { result.complete(it.isSuccessful) }
            }
        })
        return result
    }

    private fun okResponse(request: Request): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body("{}".toResponseBody())
        .build()
}
