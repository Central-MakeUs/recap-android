package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SwitchingCaptureMutationRepositoryTest {
    @Test
    fun `updateFavorite delegates to mock in mock mode`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        val mock = mockk<MockCaptureMutationRepository>()
        coEvery { mock.updateFavorite(1L, true) } returns Result.success(Unit)
        val remote = mockk<RemoteCaptureMutationRepository>()

        val repository = SwitchingCaptureMutationRepository(
            screenshotBackendModeStore = modeStore,
            mockCaptureMutationRepository = mock,
            remoteCaptureMutationRepository = remote,
        )

        repository.updateFavorite(captureId = 1L, isFavorite = true)

        coVerify(exactly = 1) { mock.updateFavorite(1L, true) }
        coVerify(exactly = 0) { remote.updateFavorite(any(), any()) }
    }

    @Test
    fun `deleteCaptures delegates to remote in remote mode`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.REMOTE
        val mock = mockk<MockCaptureMutationRepository>()
        val remote = mockk<RemoteCaptureMutationRepository>()
        coEvery { remote.deleteCaptures(setOf(1L, 2L)) } returns Result.success(
            CaptureDeleteResult(deletedIds = setOf(1L, 2L), failedIds = emptySet()),
        )

        val repository = SwitchingCaptureMutationRepository(
            screenshotBackendModeStore = modeStore,
            mockCaptureMutationRepository = mock,
            remoteCaptureMutationRepository = remote,
        )

        repository.deleteCaptures(setOf(1L, 2L))

        coVerify(exactly = 1) { remote.deleteCaptures(setOf(1L, 2L)) }
        coVerify(exactly = 0) { mock.deleteCaptures(any()) }
    }
}
