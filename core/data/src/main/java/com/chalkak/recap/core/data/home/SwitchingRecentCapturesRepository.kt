package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CapturePage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchingRecentCapturesRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockRecentCapturesRepository: MockRecentCapturesRepository,
    private val remoteRecentCapturesRepository: RemoteRecentCapturesRepository,
) : RecentCapturesRepository {
    override suspend fun getRecentCaptures(
        page: Int,
        size: Int,
    ): Result<CapturePage> =
        resolveDelegate().getRecentCaptures(page = page, size = size)

    private suspend fun resolveDelegate(): RecentCapturesRepository {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockRecentCapturesRepository
            ScreenshotBackendMode.REMOTE -> remoteRecentCapturesRepository
        }
    }
}
