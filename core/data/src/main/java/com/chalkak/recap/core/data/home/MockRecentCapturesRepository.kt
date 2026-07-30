package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.toCaptureSummary
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.capture.CapturePage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class MockRecentCapturesRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : RecentCapturesRepository {
    override fun observeRecentCaptures(
        page: Int,
        size: Int,
    ): Flow<Result<CapturePage>> {
        return screenshotCardRepository.observeStoredCards().map { cards ->
            Result.success(cards.toCapturePage(page = page, size = size))
        }
    }

    override suspend fun getRecentCaptures(
        page: Int,
        size: Int,
    ): Result<CapturePage> {
        val cards = screenshotCardRepository.observeStoredCards().first()
        return Result.success(cards.toCapturePage(page = page, size = size))
    }
}

private fun List<StoredScreenshotCard>.toCapturePage(
    page: Int,
    size: Int,
): CapturePage {
    val sorted = sortedByDescending { card -> card.analysisResult.organizedAt.toEpochMilli() }
        .map { it.toCaptureSummary() }

    val safePage = page.coerceAtLeast(0)
    val safeSize = size.coerceAtLeast(1)
    val fromIndex = (safePage * safeSize).coerceAtMost(sorted.size)
    val toIndex = (fromIndex + safeSize).coerceAtMost(sorted.size)

    return CapturePage(
        count = sorted.size.toLong(),
        hasNext = toIndex < sorted.size,
        items = sorted.subList(fromIndex, toIndex),
    )
}
