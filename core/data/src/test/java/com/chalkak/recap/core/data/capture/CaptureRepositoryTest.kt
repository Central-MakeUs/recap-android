package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.capture.remote.CaptureDetailResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.capture.remote.OrganizeResponseDto
import com.chalkak.recap.core.data.capture.remote.OrganizeStatusDto
import com.chalkak.recap.core.data.capture.remote.UploadItemDto
import com.chalkak.recap.core.data.capture.remote.UploadUrlsResponseDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response

class CaptureRepositoryTest {
    private val captureApi = mockk<CaptureApi>()
    private val okHttpClient = mockk<OkHttpClient>(relaxed = true)

    private lateinit var repository: CaptureRepository

    @BeforeEach
    fun setUp() {
        repository = CaptureRepository(
            captureApi = captureApi,
            okHttpClient = okHttpClient,
        )
    }

    @Test
    fun `issueUploadUrls maps success response`() = runTest {
        coEvery { captureApi.issueUploadUrls(any()) } returns ApiResponseDto(
            success = true,
            data = UploadUrlsResponseDto(
                uploads = listOf(
                    UploadItemDto(imageKey = "key-1", uploadUrl = "https://upload.example/1"),
                ),
            ),
        )

        val result = repository.issueUploadUrls(count = 1)

        assertEquals("key-1", result.getOrNull()?.uploads?.single()?.imageKey)
        assertEquals("https://upload.example/1", result.getOrNull()?.uploads?.single()?.uploadUrl)
    }

    @Test
    fun `organize maps server error`() = runTest {
        coEvery { captureApi.organize(any()) } returns ApiResponseDto(
            success = false,
            data = null,
            error = ApiErrorDto(code = "ORGANIZE_IN_PROGRESS", message = "already running"),
        )

        val result = repository.organize(listOf("key-1"))

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("ORGANIZE_IN_PROGRESS", error.code)
    }

    @Test
    fun `organize maps HttpException error body code`() = runTest {
        val body =
            """
            {"success":false,"data":null,"error":{"code":"ORGANIZE_IN_PROGRESS","message":"already running"}}
            """.trimIndent().toResponseBody("application/json".toMediaType())
        coEvery { captureApi.organize(any()) } throws HttpException(Response.error<Unit>(409, body))

        val result = repository.organize(listOf("key-1"))

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("ORGANIZE_IN_PROGRESS", error.code)
        assertEquals("already running", error.message)
    }

    @Test
    fun `getDetail maps network failure`() = runTest {
        coEvery { captureApi.getDetail(1L) } throws IOException("offline")

        val result = repository.getDetail(1L)

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }

    @Test
    fun `getDetail maps capture detail`() = runTest {
        coEvery { captureApi.getDetail(10L) } returns ApiResponseDto(
            success = true,
            data = CaptureDetailResponseDto(
                captureId = 10L,
                typeCode = CardTypeDto.KNOWLEDGE,
                title = "title",
                summary = "summary",
                body = "body",
                originalImageUrl = "https://img",
                isFavorite = true,
                organizedAt = "2026-07-19T00:00:00Z",
            ),
        )

        val result = repository.getDetail(10L)

        assertEquals(10L, result.getOrNull()?.captureId)
        assertEquals(ScreenshotContentType.KNOWLEDGE, result.getOrNull()?.typeCode)
        assertEquals(true, result.getOrNull()?.isFavorite)
    }

    @Test
    fun `organize maps batch response`() = runTest {
        coEvery { captureApi.organize(any()) } returns ApiResponseDto(
            success = true,
            data = OrganizeResponseDto(
                batchId = 3L,
                totalCount = 2,
                status = OrganizeStatusDto.PROCESSING,
            ),
        )

        val result = repository.organize(listOf("a", "b"))

        assertEquals(3L, result.getOrNull()?.batchId)
        assertEquals(OrganizeStatus.PROCESSING, result.getOrNull()?.status)
    }

    @Test
    fun `cancelOrganize returns success`() = runTest {
        coEvery { captureApi.cancelOrganize(5L) } returns Unit

        val result = repository.cancelOrganize(5L)

        assertTrue(result.isSuccess)
    }
}
