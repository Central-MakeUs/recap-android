package com.chalkak.recap.core.data.storage

import com.chalkak.recap.core.data.capture.matchesSearch
import com.chalkak.recap.core.data.capture.sortedByOrganizedAt
import com.chalkak.recap.core.data.capture.toCaptureSummary
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageOverview
import com.chalkak.recap.core.model.storage.StorageType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class MockStorageRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : StorageRepository {
    override fun observeOverview(searchQuery: String): Flow<StorageOverview> {
        return screenshotCardRepository.observeStoredCards().map { cards ->
            cards.toStorageOverview(searchQuery = searchQuery)
        }
    }

    override fun observeCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort,
        searchQuery: String,
    ): Flow<CaptureList> {
        return screenshotCardRepository.observeStoredCards().map { cards ->
            cards
                .map { it.toCaptureSummary() }
                .filter { summary -> summary.typeCode == typeCode }
                .matchesSearch(searchQuery)
                .sortedByOrganizedAt(sort)
                .toCaptureList()
        }
    }

    override fun observeFavoriteCaptures(
        sort: CaptureSort,
        searchQuery: String,
    ): Flow<CaptureList> {
        return screenshotCardRepository.observeStoredCards().map { cards ->
            cards
                .map { it.toCaptureSummary() }
                .filter { summary -> summary.isFavorite }
                .matchesSearch(searchQuery)
                .sortedByOrganizedAt(sort)
                .toCaptureList()
        }
    }

    override suspend fun getStorageTypes(): Result<List<StorageType>> =
        Result.success(
            screenshotCardRepository.observeStoredCards().first()
                .toStorageOverview(searchQuery = "")
                .types,
        )

    override suspend fun getCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort,
    ): Result<CaptureList> =
        Result.success(
            screenshotCardRepository.observeStoredCards().first()
                .map { it.toCaptureSummary() }
                .filter { summary -> summary.typeCode == typeCode }
                .sortedByOrganizedAt(sort)
                .toCaptureList(),
        )

    override suspend fun getFavoriteCaptures(): Result<CaptureList> =
        Result.success(
            screenshotCardRepository.observeStoredCards().first()
                .map { it.toCaptureSummary() }
                .filter { summary -> summary.isFavorite }
                .sortedByOrganizedAt(CaptureSort.Latest)
                .toCaptureList(),
        )

    override suspend fun getEtcCaptures(sort: CaptureSort): Result<CaptureList> =
        getCapturesByType(typeCode = ScreenshotContentType.ETC, sort = sort)
}

internal fun List<StoredScreenshotCard>.toStorageOverview(searchQuery: String): StorageOverview {
    val allSummaries = map { it.toCaptureSummary() }
    val filtered = allSummaries.matchesSearch(searchQuery)
    val favoriteCount = filtered.count { it.isFavorite }
    val types = StorageOverviewCategoryOrder.mapNotNull { contentType ->
        val typeCards = filtered.filter { summary -> summary.typeCode == contentType }
        if (typeCards.isEmpty()) {
            return@mapNotNull null
        }
        StorageType(
            typeCode = contentType,
            count = typeCards.size.toLong(),
            representativeTitles = typeCards.map { it.title }.take(2),
        )
    }
    return StorageOverview(
        hasAnyCapture = allSummaries.isNotEmpty(),
        favoriteCount = favoriteCount,
        types = types,
    )
}

private fun List<CaptureSummary>.toCaptureList(): CaptureList =
    CaptureList(count = size, items = this)

internal val StorageOverviewCategoryOrder: List<ScreenshotContentType> = listOf(
    ScreenshotContentType.SHOPPING,
    ScreenshotContentType.PLACE,
    ScreenshotContentType.SCHEDULE,
    ScreenshotContentType.KNOWLEDGE,
    ScreenshotContentType.CONTENT,
    ScreenshotContentType.BENEFIT,
    ScreenshotContentType.RECORD,
    ScreenshotContentType.JOB,
    ScreenshotContentType.ETC,
)