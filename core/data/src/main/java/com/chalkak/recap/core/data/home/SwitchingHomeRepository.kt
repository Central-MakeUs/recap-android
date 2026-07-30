package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.home.HomeSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
class SwitchingHomeRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockHomeRepository: MockHomeRepository,
    private val remoteHomeRepository: RemoteHomeRepository,
) : HomeRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSummary(): Flow<Result<HomeSummary>> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            when (mode) {
                ScreenshotBackendMode.MOCK -> mockHomeRepository.observeSummary()
                ScreenshotBackendMode.REMOTE -> remoteHomeRepository.observeSummary()
            }
        }
    }

    override suspend fun prefetchSummary(): Result<HomeSummary> {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockHomeRepository.prefetchSummary()
            ScreenshotBackendMode.REMOTE -> remoteHomeRepository.prefetchSummary()
        }
    }

    override fun refreshSummary() {
        remoteHomeRepository.refreshSummary()
        mockHomeRepository.refreshSummary()
    }
}
