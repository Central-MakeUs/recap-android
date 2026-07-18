package com.chalkak.recap.core.data.screenshot.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ScreenshotCardDao {
    @Query("SELECT * FROM screenshot_cards ORDER BY organizedAtMillis DESC")
    abstract fun observeAllCards(): Flow<List<ScreenshotCardEntity>>

    @Query("SELECT * FROM screenshot_cards WHERE captureId = :captureId")
    abstract fun observeCard(captureId: Long): Flow<ScreenshotCardEntity?>

    @Query("SELECT * FROM screenshot_cards WHERE captureId = :captureId")
    abstract suspend fun getCardByCaptureId(captureId: Long): ScreenshotCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCards(cards: List<ScreenshotCardEntity>)

    @Query(
        """
        UPDATE screenshot_cards
        SET isFavorite = :isFavorite, updatedAtMillis = :updatedAtMillis
        WHERE captureId = :captureId
        """,
    )
    abstract suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
        updatedAtMillis: Long,
    )

    @Query(
        """
        UPDATE screenshot_cards
        SET title = :title,
            summary = :summary,
            body = :body,
            typeCode = :typeCode,
            updatedAtMillis = :updatedAtMillis
        WHERE captureId = :captureId
        """,
    )
    abstract suspend fun updateCardContent(
        captureId: Long,
        title: String,
        summary: String,
        body: String,
        typeCode: String,
        updatedAtMillis: Long,
    ): Int

    @Query("DELETE FROM screenshot_cards WHERE captureId = :captureId")
    abstract suspend fun deleteByCaptureId(captureId: Long)

    @Query("DELETE FROM screenshot_cards WHERE captureId IN (:captureIds)")
    abstract suspend fun deleteByCaptureIdsChunk(captureIds: List<Long>)

    @Query("DELETE FROM screenshot_cards")
    abstract suspend fun deleteAllCards()

    @Transaction
    open suspend fun deleteByCaptureIds(captureIds: List<Long>) {
        captureIds.chunked(DeleteBatchSize).forEach { chunk ->
            deleteByCaptureIdsChunk(chunk)
        }
    }

    @Transaction
    open suspend fun saveAnalysisResults(entries: List<ScreenshotCardSaveEntry>) {
        val baseTimeMillis = System.currentTimeMillis()
        entries.forEachIndexed { index, entry ->
            val existingCard = getCardByCaptureId(entry.analysisResult.captureId)
            val timestampMillis = baseTimeMillis + index
            val mergedResult = entry.analysisResult.copy(
                body = entry.analysisResult.body.ifBlank {
                    existingCard?.body.orEmpty()
                },
            )
            val cardEntity = mergedResult.toCardEntity(
                imageRefs = mergeImageRefs(entry.imageRefs, existingCard),
                updatedAtMillis = timestampMillis,
            )
            insertCards(listOf(cardEntity))
        }
    }

    private companion object {
        const val DeleteBatchSize = 900
    }
}
