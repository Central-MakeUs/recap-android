package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.home.HomeSummary
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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
        val localResult = Result.success(localSummary)
        val remoteResult = Result.success(remoteSummary)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.MOCK)
        val mock = mockk<MockHomeRepository>()
        every { mock.observeSummary() } returns flowOf(localResult)
        val remote = mockk<RemoteHomeRepository>()
        every { remote.observeSummary() } returns flowOf(remoteResult)

        val repository = SwitchingHomeRepository(
            screenshotBackendModeStore = modeStore,
            mockHomeRepository = mock,
            remoteHomeRepository = remote,
        )

        assertEquals(localResult, repository.observeSummary().first())
        assertSame(localSummary, repository.observeSummary().first().getOrNull())
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
        val localResult = Result.success(localSummary)
        val remoteResult = Result.success(remoteSummary)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.REMOTE)
        val mock = mockk<MockHomeRepository>()
        every { mock.observeSummary() } returns flowOf(localResult)
        val remote = mockk<RemoteHomeRepository>()
        every { remote.observeSummary() } returns flowOf(remoteResult)

        val repository = SwitchingHomeRepository(
            screenshotBackendModeStore = modeStore,
            mockHomeRepository = mock,
            remoteHomeRepository = remote,
        )

        assertEquals(remoteResult, repository.observeSummary().first())
        assertSame(remoteSummary, repository.observeSummary().first().getOrNull())
    }
}
