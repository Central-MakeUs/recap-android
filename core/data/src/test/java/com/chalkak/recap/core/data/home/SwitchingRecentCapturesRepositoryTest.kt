package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CapturePage
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SwitchingRecentCapturesRepositoryTest {
    @Test
    fun `remote mode uses remote recent captures`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.REMOTE
        val mock = mockk<MockRecentCapturesRepository>()
        val remotePage = CapturePage(
            count = 1,
            hasNext = false,
            items = listOf(summary(captureId = 1L)),
        )
        val remote = mockk<RemoteRecentCapturesRepository>()
        coEvery { remote.getRecentCaptures(page = 0, size = 20) } returns Result.success(remotePage)

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            remoteRecentCapturesRepository = remote,
        )

        assertEquals(remotePage, repository.getRecentCaptures(page = 0, size = 20).getOrThrow())
    }

    @Test
    fun `mock mode uses mock recent captures`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        val mockPage = CapturePage(
            count = 1,
            hasNext = false,
            items = listOf(summary(captureId = 1L)),
        )
        val mock = mockk<MockRecentCapturesRepository>()
        coEvery { mock.getRecentCaptures(page = 0, size = 20) } returns Result.success(mockPage)
        val remote = mockk<RemoteRecentCapturesRepository>()

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            remoteRecentCapturesRepository = remote,
        )

        assertEquals(mockPage, repository.getRecentCaptures(page = 0, size = 20).getOrThrow())
    }

    private fun summary(captureId: Long) =
        CaptureSummary(
            captureId = captureId,
            title = "t",
            summary = "s",
            typeCode = ScreenshotContentType.JOB,
            thumbnailUrl = null,
            isFavorite = false,
            organizedAt = "2026-07-19T00:00:00Z",
        )
}
