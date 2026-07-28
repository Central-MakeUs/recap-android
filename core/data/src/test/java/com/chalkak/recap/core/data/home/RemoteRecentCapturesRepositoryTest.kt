package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CapturePageResponseDto
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RemoteRecentCapturesRepositoryTest {
    private val homeApi = mockk<HomeApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>()

    @Test
    fun `getRecentCaptures returns single page with thumbnail cache applied`() = runTest {
        coEvery { homeApi.getRecentCaptures(page = 0, size = 20) } returns ApiResponseDto(
            success = true,
            data = CapturePageResponseDto(
                count = 3,
                hasNext = true,
                items = listOf(summaryDto(1L), summaryDto(2L)),
            ),
        )
        coEvery { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }

        val repository = RemoteRecentCapturesRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
        )

        val page = repository.getRecentCaptures(page = 0, size = 20).getOrThrow()

        assertEquals(3L, page.count)
        assertTrue(page.hasNext)
        assertEquals(listOf(1L, 2L), page.items.map(CaptureSummary::captureId))
        assertEquals(ScreenshotContentType.JOB, page.items.first().typeCode)
    }

    @Test
    fun `getRecentCaptures returns failure on api error`() = runTest {
        coEvery { homeApi.getRecentCaptures(any(), any()) } throws RuntimeException("offline")

        val repository = RemoteRecentCapturesRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
        )

        val result = repository.getRecentCaptures(page = 0, size = 20)

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
    }

    private fun summaryDto(captureId: Long) =
        CaptureSummaryResponseDto(
            captureId = captureId,
            title = "t$captureId",
            summary = "s$captureId",
            typeCode = CardTypeDto.JOB,
            thumbnailUrl = "https://thumb/$captureId",
            isFavorite = false,
            organizedAt = "2026-07-19T00:00:00Z",
        )
}
