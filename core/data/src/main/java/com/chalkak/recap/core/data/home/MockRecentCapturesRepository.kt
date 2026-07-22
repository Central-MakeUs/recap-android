package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.toCaptureSummary
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.capture.CaptureSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MockRecentCapturesRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : RecentCapturesRepository {
    override fun observeRecentCaptures(): Flow<List<CaptureSummary>> {
        return screenshotCardRepository.observeStoredCards().map { cards ->
            cards
                .sortedByDescending { card -> card.analysisResult.organizedAt.toEpochMilli() }
                .map { it.toCaptureSummary() }
        }
    }
}
