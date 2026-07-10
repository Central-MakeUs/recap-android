package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ScreenshotCardRepository {
    fun observeStoredCards(): Flow<List<StoredScreenshotCard>>

    suspend fun getCard(imageId: String): StoredScreenshotCard?

    suspend fun saveAnalysisResults(
        results: List<ScreenshotAnalysisResult>,
        imageRefsByImageId: Map<String, ScreenshotCardImageRefs> = emptyMap(),
    )

    suspend fun updateFavorite(imageId: String, isFavorite: Boolean)

    suspend fun deleteCard(imageId: String)

    suspend fun deleteCards(imageIds: Set<String>)

    suspend fun deleteAllCards()
}

@Singleton
class DefaultScreenshotCardRepository @Inject constructor(
    private val screenshotCardDao: ScreenshotCardDao,
) : ScreenshotCardRepository {
    override fun observeStoredCards(): Flow<List<StoredScreenshotCard>> {
        return screenshotCardDao.observeAllCards().map { cards ->
            cards.map(ScreenshotCardWithKeyFields::toStoredScreenshotCard)
        }
    }

    override suspend fun getCard(imageId: String): StoredScreenshotCard? {
        return screenshotCardDao.getCardByImageId(imageId)?.toStoredScreenshotCard()
    }

    override suspend fun saveAnalysisResults(
        results: List<ScreenshotAnalysisResult>,
        imageRefsByImageId: Map<String, ScreenshotCardImageRefs>,
    ) {
        if (results.isEmpty()) {
            return
        }
        val entries = results.map { result ->
            ScreenshotCardSaveEntry(
                analysisResult = result,
                imageRefs = imageRefsByImageId[result.imageId] ?: ScreenshotCardImageRefs(),
            )
        }
        screenshotCardDao.saveAnalysisResults(entries)
    }

    override suspend fun updateFavorite(imageId: String, isFavorite: Boolean) {
        screenshotCardDao.updateFavorite(
            imageId = imageId,
            isFavorite = isFavorite,
            updatedAtMillis = System.currentTimeMillis(),
        )
    }

    override suspend fun deleteCard(imageId: String) {
        screenshotCardDao.deleteByImageId(imageId)
    }

    override suspend fun deleteCards(imageIds: Set<String>) {
        if (imageIds.isEmpty()) {
            return
        }
        screenshotCardDao.deleteByImageIds(imageIds.toList())
    }

    override suspend fun deleteAllCards() {
        screenshotCardDao.deleteAllCards()
    }
}
