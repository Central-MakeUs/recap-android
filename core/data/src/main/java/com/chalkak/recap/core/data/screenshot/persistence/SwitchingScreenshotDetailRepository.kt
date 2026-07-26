package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
class SwitchingScreenshotDetailRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockScreenshotDetailRepository: MockScreenshotDetailRepository,
    private val remoteScreenshotDetailRepository: RemoteScreenshotDetailRepository,
) : ScreenshotDetailRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCard(captureId: Long): Flow<StoredScreenshotCard?> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            when (mode) {
                ScreenshotBackendMode.MOCK -> mockScreenshotDetailRepository.observeCard(captureId)
                ScreenshotBackendMode.REMOTE -> remoteScreenshotDetailRepository.observeCard(captureId)
            }
        }
    }
}
