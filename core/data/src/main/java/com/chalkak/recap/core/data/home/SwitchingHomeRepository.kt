package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.ScreenshotBackendModeStore
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
    override fun observeSummary(): Flow<HomeSummary> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            when (mode) {
                ScreenshotBackendMode.MOCK -> mockHomeRepository.observeSummary()
                ScreenshotBackendMode.REMOTE -> remoteHomeRepository.observeSummary()
            }
        }
    }
}
