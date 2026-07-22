package com.chalkak.recap.core.data.network

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response

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

    private fun httpException(
        code: Int,
        body: String,
    ): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
