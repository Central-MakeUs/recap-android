package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.toCaptureSummary
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.home.HomeSummary
import com.chalkak.recap.core.model.home.TopType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MockHomeRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : HomeRepository {
    override fun observeSummary(): Flow<HomeSummary> {
        return screenshotCardRepository.observeStoredCards().map { cards ->
            cards.toHomeSummary()
        }
    }
}

internal fun List<StoredScreenshotCard>.toHomeSummary(): HomeSummary {
    val sortedCards = sortedByDescending { card ->
        card.analysisResult.organizedAt.toEpochMilli()
    }
    val recentCaptures = sortedCards
        .take(HomeRecentScreenshotLimit)
        .map { it.toCaptureSummary() }
    val favorites = sortedCards
        .filter { card -> card.analysisResult.isFavorite }
        .take(HomeFavoriteItemLimit)
        .map { it.toCaptureSummary() }
    val topTypes = ScreenshotContentType.entries
        .mapNotNull { contentType ->
            val typeCards = sortedCards.filter { card ->
                card.analysisResult.typeCode == contentType
            }
            if (typeCards.isEmpty()) {
                return@mapNotNull null
            }
            TopType(
                typeCode = contentType,
                count = typeCards.size.toLong(),
                representativeThumbnailUrl = typeCards.first().toCaptureSummary().thumbnailUrl,
            )
        }
        .sortedByDescending { type -> type.count }
        .take(HomeFrequentSaveTypeLimit)

    return HomeSummary(
        recentCaptures = recentCaptures,
        favorites = favorites,
        topTypes = topTypes,
        hasAnyCapture = sortedCards.isNotEmpty(),
    )
}

private const val HomeRecentScreenshotLimit = 3
private const val HomeFavoriteItemLimit = 4
private const val HomeFrequentSaveTypeLimit = 4
