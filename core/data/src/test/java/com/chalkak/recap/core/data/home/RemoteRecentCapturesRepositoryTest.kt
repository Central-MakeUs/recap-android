package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.CaptureChange
import com.chalkak.recap.core.data.capture.CaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CapturePageResponseDto
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.capture.CapturePage
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteRecentCapturesRepositoryTest {
    private val homeApi = mockk<HomeApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>()
    private val changeNotifier = CaptureChangeNotifier()

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
        every { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }

        val repository = createRepository()

        val page = repository.getRecentCaptures(page = 0, size = 20).getOrThrow()

        assertEquals(3L, page.count)
        assertTrue(page.hasNext)
        assertEquals(listOf(1L, 2L), page.items.map(CaptureSummary::captureId))
        assertEquals(ScreenshotContentType.JOB, page.items.first().typeCode)
    }

    @Test
    fun `getRecentCaptures returns failure on api error`() = runTest {
        coEvery { homeApi.getRecentCaptures(any(), any()) } throws RuntimeException("offline")

        val repository = createRepository()

        val result = repository.getRecentCaptures(page = 0, size = 20)

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
    }

    @Test
    fun `observeRecentCaptures refetches when capture change is notified`() = runTest {
        coEvery { homeApi.getRecentCaptures(page = 0, size = 20) } returnsMany listOf(
            ApiResponseDto(
                success = true,
                data = CapturePageResponseDto(
                    count = 2,
                    hasNext = false,
                    items = listOf(summaryDto(1L), summaryDto(2L)),
                ),
            ),
            ApiResponseDto(
                success = true,
                data = CapturePageResponseDto(
                    count = 1,
                    hasNext = false,
                    items = listOf(summaryDto(2L)),
                ),
            ),
        )
        every { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }

        val repository = createRepository()
        val emissions = mutableListOf<Result<CapturePage>>()
        val collectJob = launch {
            repository.observeRecentCaptures(page = 0, size = 20)
                .take(2)
                .toList(emissions)
        }
        advanceUntilIdle()

        changeNotifier.emit(CaptureChange.Invalidated)
        advanceUntilIdle()
        collectJob.join()

        assertEquals(listOf(1L, 2L), emissions[0].getOrThrow().items.map(CaptureSummary::captureId))
        assertEquals(listOf(2L), emissions[1].getOrThrow().items.map(CaptureSummary::captureId))
        assertEquals(1L, emissions[1].getOrThrow().count)
        coVerify(exactly = 2) { homeApi.getRecentCaptures(page = 0, size = 20) }
    }

    private fun createRepository() =
        RemoteRecentCapturesRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
        )

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
