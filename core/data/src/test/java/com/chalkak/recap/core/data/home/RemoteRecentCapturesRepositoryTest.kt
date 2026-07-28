package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CapturePageResponseDto
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RemoteRecentCapturesRepositoryTest {
    private val homeApi = mockk<HomeApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>()
    private val changeNotifier = mockk<RemoteCaptureChangeNotifier>()

    @Test
    fun `observeRecentCaptures accumulates pages until hasNext is false`() = runTest {
        val changes = MutableSharedFlow<Unit>(replay = 1)
        changes.tryEmit(Unit)
        every { changeNotifier.changes } returns changes
        coEvery { homeApi.getRecentCaptures(page = 0, size = 20) } returns ApiResponseDto(
            success = true,
            data = CapturePageResponseDto(
                count = 3,
                hasNext = true,
                items = listOf(summaryDto(1L), summaryDto(2L)),
            ),
        )
        coEvery { homeApi.getRecentCaptures(page = 1, size = 20) } returns ApiResponseDto(
            success = true,
            data = CapturePageResponseDto(
                count = 3,
                hasNext = false,
                items = listOf(summaryDto(3L)),
            ),
        )
        coEvery { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }

        val repository = RemoteRecentCapturesRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
        )

        val items = repository.observeRecentCaptures().first()

        assertEquals(listOf(1L, 2L, 3L), items.map(CaptureSummary::captureId))
        assertEquals(ScreenshotContentType.JOB, items.first().typeCode)
    }

    @Test
    fun `observeRecentCaptures returns empty list on failure`() = runTest {
        val changes = MutableSharedFlow<Unit>(replay = 1)
        changes.tryEmit(Unit)
        every { changeNotifier.changes } returns changes
        coEvery { homeApi.getRecentCaptures(any(), any()) } throws RuntimeException("offline")

        val repository = RemoteRecentCapturesRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
        )

        assertTrue(repository.observeRecentCaptures().first().isEmpty())
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
