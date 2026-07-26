package com.chalkak.recap.core.data.screenshot.persistence

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class MockScreenshotDetailRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : ScreenshotDetailRepository {
    override fun observeCard(captureId: Long): Flow<StoredScreenshotCard?> {
        return screenshotCardRepository.observeCard(captureId)
    }
}
