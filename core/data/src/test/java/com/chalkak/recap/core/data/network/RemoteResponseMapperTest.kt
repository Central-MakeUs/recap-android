package com.chalkak.recap.core.data.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class RemoteResponseMapperTest {
    @Test
    fun `mapHttpException reads error code from envelope body`() {
        val exception =
            httpException(
                code = 409,
                body =
                    """
                    {"success":false,"data":null,"error":{"code":"ORGANIZE_IN_PROGRESS","message":"already running"}}
                    """.trimIndent(),
            )

        val mapped = mapHttpException(exception)

        assertEquals("ORGANIZE_IN_PROGRESS", mapped.code)
        assertEquals("already running", mapped.message)
        assertEquals(exception, mapped.cause)
    }

    @Test
    fun `mapHttpException falls back to HTTP status code when body is empty`() {
        val exception = httpException(code = 409, body = "")

        val mapped = mapHttpException(exception)

        assertEquals("HTTP_409", mapped.code)
        assertEquals(exception, mapped.cause)
    }

    @Test
    fun `runRemoteCatchingSuspend maps HttpException to RemoteApiException`() = runTest {
        val exception =
            httpException(
                code = 404,
                body =
                    """
                    {"success":false,"error":{"code":"NOT_FOUND","message":"missing"}}
                    """.trimIndent(),
            )

        val result =
            runRemoteCatchingSuspend {
                throw exception
            }

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("NOT_FOUND", error.code)
        assertEquals("missing", error.message)
    }

    @Test
    fun `runRemoteCatchingSuspend propagates CancellationException`() {
        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking {
                runRemoteCatchingSuspend {
                    throw CancellationException("cancelled")
                }
            }
        }
    }

    @Test
    fun `runRemoteCatchingSuspend maps IOException to RemoteNetworkException`() = runTest {
        val result =
            runRemoteCatchingSuspend {
                throw IOException("offline")
            }

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }

    @Test
    fun `isUsageLimitExceeded is true for envelope 429`() {
        val exception =
            mapHttpException(
                httpException(
                    code = 429,
                    body =
                        """
                        {"success":false,"data":null,"error":{"code":"USAGE_EXCEEDED","message":"이번 달 AI 분석 사용량을 초과했습니다"}}
                        """.trimIndent(),
                ),
            )

        assertTrue(exception.isUsageLimitExceeded())
    }

    @Test
    fun `isUsageLimitExceeded is true for empty-body HTTP_429`() {
        val exception = mapHttpException(httpException(code = 429, body = ""))

        assertEquals("HTTP_429", exception.code)
        assertTrue(exception.isUsageLimitExceeded())
    }

    @Test
    fun `isUsageLimitExceeded is true when message mentions usage exceeded`() {
        val exception = RemoteApiException(
            code = "USAGE_EXCEEDED",
            message = "이번 달 AI 분석 사용량을 초과했습니다",
        )

        assertTrue(exception.isUsageLimitExceeded())
    }

    @Test
    fun `isUsageLimitExceeded is false for 409`() {
        val exception =
            mapHttpException(
                httpException(
                    code = 409,
                    body =
                        """
                        {"success":false,"data":null,"error":{"code":"ORGANIZE_IN_PROGRESS","message":"already running"}}
                        """.trimIndent(),
                ),
            )

        assertFalse(exception.isUsageLimitExceeded())
        assertFalse(RuntimeException("boom").isUsageLimitExceeded())
    }

    private fun httpException(
        code: Int,
        body: String,
    ): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
