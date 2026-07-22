package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.home.HomeSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class SwitchingHomeRepositoryTest {
    @Test
    fun `observeSummary delegates to mock in mock mode`() = runTest {
        val localSummary = HomeSummary(
            recentCaptures = emptyList(),
            favorites = emptyList(),
            topTypes = emptyList(),
            hasAnyCapture = true,
        )
        val remoteSummary = localSummary.copy(hasAnyCapture = false)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.MOCK)
        val mock = mockk<MockHomeRepository>()
        every { mock.observeSummary() } returns flowOf(localSummary)
        val remote = mockk<RemoteHomeRepository>()
        every { remote.observeSummary() } returns flowOf(remoteSummary)

        val repository = SwitchingHomeRepository(
            screenshotBackendModeStore = modeStore,
            mockHomeRepository = mock,
            remoteHomeRepository = remote,
        )

        assertSame(localSummary, repository.observeSummary().first())
    }

    @Test
    fun `observeSummary delegates to remote in remote mode`() = runTest {
        val localSummary = HomeSummary(
            recentCaptures = emptyList(),
            favorites = emptyList(),
            topTypes = emptyList(),
            hasAnyCapture = true,
        )
        val remoteSummary = localSummary.copy(hasAnyCapture = false)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.REMOTE)
        val mock = mockk<MockHomeRepository>()
        every { mock.observeSummary() } returns flowOf(localSummary)
        val remote = mockk<RemoteHomeRepository>()
        every { remote.observeSummary() } returns flowOf(remoteSummary)

        val repository = SwitchingHomeRepository(
            screenshotBackendModeStore = modeStore,
            mockHomeRepository = mock,
            remoteHomeRepository = remote,
        )

        assertSame(remoteSummary, repository.observeSummary().first())
    }
}
