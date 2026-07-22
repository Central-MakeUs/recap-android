package com.chalkak.recap.core.data.screenshot.backend

import kotlinx.coroutines.flow.Flow

interface ScreenshotBackendModeStore {
    val mode: Flow<ScreenshotBackendMode>

    suspend fun currentMode(): ScreenshotBackendMode

    suspend fun setMode(mode: ScreenshotBackendMode)
}
