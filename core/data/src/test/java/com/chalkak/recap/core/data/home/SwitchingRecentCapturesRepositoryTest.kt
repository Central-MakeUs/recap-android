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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SwitchingRecentCapturesRepositoryTest {
    @Test
    fun `remote mode emits empty list`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.REMOTE)
        val mock = mockk<MockRecentCapturesRepository>()
        every { mock.observeRecentCaptures() } returns flowOf(
            listOf(
                CaptureSummary(
                    captureId = 1L,
                    title = "t",
                    summary = "s",
                    typeCode = ScreenshotContentType.JOB,
                    thumbnailUrl = null,
                    isFavorite = false,
                    organizedAt = "2026-07-19T00:00:00Z",
                ),
            ),
        )
        val remote = StubRemoteRecentCapturesRepository()

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            stubRemoteRecentCapturesRepository = remote,
        )

        assertTrue(repository.observeRecentCaptures().first().isEmpty())
    }

    @Test
    fun `mock mode uses mock recent captures`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.MOCK)
        val localItems = listOf(
            CaptureSummary(
                captureId = 1L,
                title = "t",
                summary = "s",
                typeCode = ScreenshotContentType.JOB,
                thumbnailUrl = null,
                isFavorite = false,
                organizedAt = "2026-07-19T00:00:00Z",
            ),
        )
        val mock = mockk<MockRecentCapturesRepository>()
        every { mock.observeRecentCaptures() } returns flowOf(localItems)
        val remote = StubRemoteRecentCapturesRepository()

        val repository = SwitchingRecentCapturesRepository(
            screenshotBackendModeStore = modeStore,
            mockRecentCapturesRepository = mock,
            stubRemoteRecentCapturesRepository = remote,
        )

        assertEquals(localItems, repository.observeRecentCaptures().first())
    }
}
