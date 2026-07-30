package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CapturePage
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
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

    @Test
    fun `observeRecentCaptures delegates to mock in mock mode`() = runTest {
        val mockPage = CapturePage(
            count = 1,
            hasNext = false,
            items = listOf(summary(captureId = 1L)),
        )
        val remotePage = CapturePage(
            count = 1,
            hasNext = false,
            items = listOf(summary(captureId = 2L)),
        )
        val mockResult = Result.success(mockPage)
        val remoteResult = Result.success(remotePage)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.MOCK)
        val mock = mockk<MockRecentCapturesRepository>()
        every { mock.observeRecentCaptures(page = 0, size = 20) } returns flowOf(mockResult)
        val remote = mockk<RemoteRecentCapturesRepository>()
        every { remote.observeRecentCaptures(page = 0, size = 20) } returns flowOf(remoteResult)

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            remoteRecentCapturesRepository = remote,
        )

        assertEquals(mockResult, repository.observeRecentCaptures(page = 0, size = 20).first())
        assertSame(mockPage, repository.observeRecentCaptures(page = 0, size = 20).first().getOrNull())
    }

    @Test
    fun `observeRecentCaptures delegates to remote in remote mode`() = runTest {
        val mockPage = CapturePage(
            count = 1,
            hasNext = false,
            items = listOf(summary(captureId = 1L)),
        )
        val remotePage = CapturePage(
            count = 1,
            hasNext = false,
            items = listOf(summary(captureId = 2L)),
        )
        val mockResult = Result.success(mockPage)
        val remoteResult = Result.success(remotePage)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.REMOTE)
        val mock = mockk<MockRecentCapturesRepository>()
        every { mock.observeRecentCaptures(page = 0, size = 20) } returns flowOf(mockResult)
        val remote = mockk<RemoteRecentCapturesRepository>()
        every { remote.observeRecentCaptures(page = 0, size = 20) } returns flowOf(remoteResult)

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            remoteRecentCapturesRepository = remote,
        )

        assertEquals(remoteResult, repository.observeRecentCaptures(page = 0, size = 20).first())
        assertSame(remotePage, repository.observeRecentCaptures(page = 0, size = 20).first().getOrNull())
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
