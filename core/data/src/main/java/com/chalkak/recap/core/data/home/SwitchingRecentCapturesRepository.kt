package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CaptureSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
class SwitchingRecentCapturesRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockRecentCapturesRepository: MockRecentCapturesRepository,
    private val stubRemoteRecentCapturesRepository: StubRemoteRecentCapturesRepository,
) : RecentCapturesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecentCaptures(): Flow<List<CaptureSummary>> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            when (mode) {
                ScreenshotBackendMode.MOCK -> mockRecentCapturesRepository.observeRecentCaptures()
                ScreenshotBackendMode.REMOTE -> stubRemoteRecentCapturesRepository.observeRecentCaptures()
            }
        }
    }
}
