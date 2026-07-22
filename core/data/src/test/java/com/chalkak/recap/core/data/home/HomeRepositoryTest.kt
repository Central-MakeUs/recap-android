package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.home.remote.HomeSummaryResponseDto
import com.chalkak.recap.core.data.home.remote.TopTypeResponseDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HomeRepositoryTest {
    private val homeApi = mockk<HomeApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private val changeNotifier = RemoteCaptureChangeNotifier()
    private lateinit var repository: RemoteHomeRepository

    @BeforeEach
    fun setUp() {
        coEvery { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }
        repository = RemoteHomeRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
        )
    }

    @Test
    fun `getSummary maps success response`() = runTest {
        coEvery { homeApi.getSummary() } returns ApiResponseDto(
            success = true,
            data = HomeSummaryResponseDto(
                recentCaptures = listOf(
                    CaptureSummaryResponseDto(
                        captureId = 1L,
                        title = "recent",
                        summary = "s",
                        typeCode = CardTypeDto.JOB,
                        thumbnailUrl = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
                favorites = emptyList(),
                topTypes = listOf(
                    TopTypeResponseDto(
                        typeCode = CardTypeDto.JOB,
                        count = 4L,
                        representativeThumbnailUrl = "https://thumb",
                    ),
                ),
                hasAnyCapture = true,
            ),
        )

        val result = repository.getSummary()

        assertEquals(true, result.getOrNull()?.hasAnyCapture)
        assertEquals(1, result.getOrNull()?.recentCaptures?.size)
        assertEquals(ScreenshotContentType.JOB, result.getOrNull()?.topTypes?.single()?.typeCode)
    }

    @Test
    fun `getSummary maps server error`() = runTest {
        coEvery { homeApi.getSummary() } returns ApiResponseDto(
            success = false,
            data = null,
            error = ApiErrorDto(code = "UNAUTHORIZED", message = "login required"),
        )

        val result = repository.getSummary()

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("UNAUTHORIZED", error.code)
    }

    @Test
    fun `getSummary maps network failure`() = runTest {
        coEvery { homeApi.getSummary() } throws IOException("offline")

        val result = repository.getSummary()

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }
}