package com.chalkak.recap.core.data.screenshot.backend

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockScreenshotDataResetter @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) {
    suspend fun reset() {
        screenshotCardRepository.deleteAllCards()
        screenshotImageStorage.clearStoredImages()
    }
}
