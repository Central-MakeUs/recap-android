package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ScreenshotCardRepository {
    fun observeStoredCards(): Flow<List<StoredScreenshotCard>>

    fun observeCard(captureId: Long): Flow<StoredScreenshotCard?>

    suspend fun getCard(captureId: Long): StoredScreenshotCard?

    suspend fun saveAnalysisResults(
        results: List<ScreenshotAnalysisResult>,
        imageRefsByCaptureId: Map<Long, ScreenshotCardImageRefs> = emptyMap(),
    )

    suspend fun updateFavorite(captureId: Long, isFavorite: Boolean)

    suspend fun updateCardContent(
        captureId: Long,
        title: String,
        summary: String,
        body: String,
        typeCode: ScreenshotContentType,
        updatedAtMillis: Long = System.currentTimeMillis(),
    ): Boolean

    suspend fun deleteCard(captureId: Long)

    suspend fun deleteCards(captureIds: Set<Long>)

    suspend fun deleteAllCards()
}

@Singleton
class DefaultScreenshotCardRepository @Inject constructor(
    private val screenshotCardDao: ScreenshotCardDao,
) : ScreenshotCardRepository {
    override fun observeStoredCards(): Flow<List<StoredScreenshotCard>> {
        return screenshotCardDao.observeAllCards().map { cards ->
            cards.map(ScreenshotCardEntity::toStoredScreenshotCard)
        }
    }

    override fun observeCard(captureId: Long): Flow<StoredScreenshotCard?> {
        return screenshotCardDao.observeCard(captureId).map { card ->
            card?.toStoredScreenshotCard()
        }
    }

    override suspend fun getCard(captureId: Long): StoredScreenshotCard? {
        return screenshotCardDao.getCardByCaptureId(captureId)?.toStoredScreenshotCard()
    }

    override suspend fun saveAnalysisResults(
        results: List<ScreenshotAnalysisResult>,
        imageRefsByCaptureId: Map<Long, ScreenshotCardImageRefs>,
    ) {
        if (results.isEmpty()) {
            return
        }
        val entries = results.map { result ->
            ScreenshotCardSaveEntry(
                analysisResult = result,
                imageRefs = imageRefsByCaptureId[result.captureId] ?: ScreenshotCardImageRefs(),
            )
        }
        screenshotCardDao.saveAnalysisResults(entries)
    }

    override suspend fun updateFavorite(captureId: Long, isFavorite: Boolean) {
        screenshotCardDao.updateFavorite(
            captureId = captureId,
            isFavorite = isFavorite,
            updatedAtMillis = System.currentTimeMillis(),
        )
    }

    override suspend fun updateCardContent(
        captureId: Long,
        title: String,
        summary: String,
        body: String,
        typeCode: ScreenshotContentType,
        updatedAtMillis: Long,
    ): Boolean {
        val updatedRows = screenshotCardDao.updateCardContent(
            captureId = captureId,
            title = title,
            summary = summary,
            body = body,
            typeCode = typeCode.name,
            updatedAtMillis = updatedAtMillis,
        )
        return updatedRows > 0
    }

    override suspend fun deleteCard(captureId: Long) {
        screenshotCardDao.deleteByCaptureId(captureId)
    }

    override suspend fun deleteCards(captureIds: Set<Long>) {
        if (captureIds.isEmpty()) {
            return
        }
        screenshotCardDao.deleteByCaptureIds(captureIds.toList())
    }

    override suspend fun deleteAllCards() {
        screenshotCardDao.deleteAllCards()
    }
}
