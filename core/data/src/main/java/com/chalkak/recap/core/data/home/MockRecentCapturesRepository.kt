package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.toCaptureSummary
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.capture.CapturePage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class MockRecentCapturesRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : RecentCapturesRepository {
    override suspend fun getRecentCaptures(
        page: Int,
        size: Int,
    ): Result<CapturePage> {
        val sorted = screenshotCardRepository.observeStoredCards().first()
            .sortedByDescending { card -> card.analysisResult.organizedAt.toEpochMilli() }
            .map { it.toCaptureSummary() }

        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceAtLeast(1)
        val fromIndex = (safePage * safeSize).coerceAtMost(sorted.size)
        val toIndex = (fromIndex + safeSize).coerceAtMost(sorted.size)

        return Result.success(
            CapturePage(
                count = sorted.size.toLong(),
                hasNext = toIndex < sorted.size,
                items = sorted.subList(fromIndex, toIndex),
            ),
        )
    }
}
