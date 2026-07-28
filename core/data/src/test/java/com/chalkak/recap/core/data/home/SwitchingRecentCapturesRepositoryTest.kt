package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SwitchingRecentCapturesRepositoryTest {
    @Test
    fun `remote mode uses remote recent captures`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.REMOTE)
        val mock = mockk<MockRecentCapturesRepository>()
        every { mock.observeRecentCaptures() } returns flowOf(
            listOf(summary(captureId = 99L)),
        )
        val remoteItems = listOf(summary(captureId = 1L))
        val remote = mockk<RemoteRecentCapturesRepository>()
        every { remote.observeRecentCaptures() } returns flowOf(remoteItems)

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            remoteRecentCapturesRepository = remote,
        )

        assertEquals(remoteItems, repository.observeRecentCaptures().first())
    }

    @Test
    fun `mock mode uses mock recent captures`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.MOCK)
        val localItems = listOf(summary(captureId = 1L))
        val mock = mockk<MockRecentCapturesRepository>()
        every { mock.observeRecentCaptures() } returns flowOf(localItems)
        val remote = mockk<RemoteRecentCapturesRepository>()

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            remoteRecentCapturesRepository = remote,
        )

        assertEquals(localItems, repository.observeRecentCaptures().first())
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
