package com.chalkak.recap.core.data.screenshot

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

sealed interface ScreenshotBackendSwitchResult {
    data object Success : ScreenshotBackendSwitchResult
    data object RejectedBusy : ScreenshotBackendSwitchResult
    data object Failure : ScreenshotBackendSwitchResult
}

@Singleton
class ScreenshotBackendSwitcher @Inject constructor(
    private val modeStore: ScreenshotBackendModeStore,
    private val mockScreenshotDataResetter: MockScreenshotDataResetter,
    private val screenshotAnalysisRunState: ScreenshotAnalysisRunState,
) {
    private val switchMutex = Mutex()
    private val _isSwitching = MutableStateFlow(false)
    val isSwitching: StateFlow<Boolean> = _isSwitching.asStateFlow()

    suspend fun switchTo(targetMode: ScreenshotBackendMode): ScreenshotBackendSwitchResult {
        if (!switchMutex.tryLock()) {
            return ScreenshotBackendSwitchResult.RejectedBusy
        }
        try {
            if (modeStore.currentMode() == targetMode) {
                return ScreenshotBackendSwitchResult.Success
            }
            if (screenshotAnalysisRunState.isRunning.value) {
                return ScreenshotBackendSwitchResult.RejectedBusy
            }

            _isSwitching.value = true
            return try {
                mockScreenshotDataResetter.reset()
                modeStore.setMode(targetMode)
                ScreenshotBackendSwitchResult.Success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                ScreenshotBackendSwitchResult.Failure
            }
        } finally {
            _isSwitching.value = false
            switchMutex.unlock()
        }
    }
}
