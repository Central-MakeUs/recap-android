package com.chalkak.recap.core.data.screenshot.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ScreenshotCardDao {
    @Transaction
    @Query("SELECT * FROM screenshot_cards ORDER BY createdAtMillis DESC")
    abstract fun observeAllCards(): Flow<List<ScreenshotCardWithKeyFields>>

    @Transaction
    @Query("SELECT * FROM screenshot_cards WHERE imageId = :imageId")
    abstract suspend fun getCardByImageId(imageId: String): ScreenshotCardWithKeyFields?

    @Query("SELECT * FROM screenshot_cards WHERE imageId = :imageId")
    abstract suspend fun getCardEntityByImageId(imageId: String): ScreenshotCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCards(cards: List<ScreenshotCardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertKeyFields(fields: List<ScreenshotKeyFieldEntity>)

    @Query("DELETE FROM screenshot_key_fields WHERE imageId = :imageId")
    abstract suspend fun deleteKeyFieldsByImageId(imageId: String)

    @Query(
        """
        UPDATE screenshot_cards
        SET isFavorite = :isFavorite, updatedAtMillis = :updatedAtMillis
        WHERE imageId = :imageId
        """,
    )
    abstract suspend fun updateFavorite(
        imageId: String,
        isFavorite: Boolean,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM screenshot_cards WHERE imageId = :imageId")
    abstract suspend fun deleteByImageId(imageId: String)

    @Query("DELETE FROM screenshot_cards WHERE imageId IN (:imageIds)")
    abstract suspend fun deleteByImageIdsChunk(imageIds: List<String>)

    @Query("DELETE FROM screenshot_cards")
    abstract suspend fun deleteAllCards()

    @Transaction
    open suspend fun deleteByImageIds(imageIds: List<String>) {
        imageIds.chunked(DeleteBatchSize).forEach { chunk ->
            deleteByImageIdsChunk(chunk)
        }
    }

    @Transaction
    open suspend fun saveAnalysisResults(entries: List<ScreenshotCardSaveEntry>) {
        val baseTimeMillis = System.currentTimeMillis()
        entries.forEachIndexed { index, entry ->
            val existingCard = getCardEntityByImageId(entry.analysisResult.imageId)
            val timestampMillis = baseTimeMillis + index
            val createdAtMillis = existingCard?.createdAtMillis ?: timestampMillis
            val cardEntity = entry.analysisResult.toCardEntity(
                imageRefs = entry.imageRefs,
                createdAtMillis = createdAtMillis,
                updatedAtMillis = timestampMillis,
            )
            insertCards(listOf(cardEntity))
            deleteKeyFieldsByImageId(cardEntity.imageId)
            insertKeyFields(entry.analysisResult.toKeyFieldEntities())
        }
    }

    private companion object {
        const val DeleteBatchSize = 900
    }
}
