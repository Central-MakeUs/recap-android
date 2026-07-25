package com.chalkak.recap.core.data.screenshot.persistence

import kotlinx.coroutines.flow.Flow

interface ScreenshotDetailRepository {
    fun observeCard(captureId: Long): Flow<StoredScreenshotCard?>
}
