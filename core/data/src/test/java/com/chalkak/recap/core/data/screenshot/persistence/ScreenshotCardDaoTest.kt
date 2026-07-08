package com.chalkak.recap.core.data.screenshot.persistence

import android.content.Context
import androidx.room.Room
import com.chalkak.recap.core.data.RecapDatabase
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
import com.chalkak.recap.core.model.screenshot.ScreenshotKeyField
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ScreenshotCardDaoTest {
    private lateinit var database: RecapDatabase
    private lateinit var dao: ScreenshotCardDao

    @Before
    fun setUp() {
        val context: Context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RecapDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.screenshotCardDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveAnalysisResults_persistsCardsInCreatedAtMillisDescendingOrder() = runBlocking {
        val firstResult = sampleResult(imageId = "first", title = "First")
        val secondResult = sampleResult(imageId = "second", title = "Second")

        dao.saveAnalysisResults(
            entries = listOf(
                ScreenshotCardSaveEntry(firstResult),
                ScreenshotCardSaveEntry(secondResult),
            ),
        )

        val storedCards = dao.observeAllCards().first()

        assertEquals(2, storedCards.size)
        assertEquals("second", storedCards[0].card.imageId)
        assertEquals("first", storedCards[1].card.imageId)
    }

    @Test
    fun saveAnalysisResults_replacesKeyFieldsOnReSave() = runBlocking {
        val initialResult = sampleResult(
            imageId = "card-1",
            keyFields = listOf(
                keyField(label = "old-label", value = "old-value", priority = 1),
            ),
        )
        val updatedResult = sampleResult(
            imageId = "card-1",
            keyFields = listOf(
                keyField(label = "new-label", value = "new-value", priority = 1),
                keyField(label = "new-label-2", value = "new-value-2", priority = 2),
            ),
        )

        dao.saveAnalysisResults(listOf(ScreenshotCardSaveEntry(initialResult)))
        dao.saveAnalysisResults(listOf(ScreenshotCardSaveEntry(updatedResult)))

        val storedCard = dao.getCardByImageId("card-1")

        assertNotNull(storedCard)
        assertEquals(2, storedCard!!.keyFields.size)
        assertEquals("new-label", storedCard.keyFields[0].label)
        assertEquals("new-value", storedCard.keyFields[0].value)
        assertEquals("new-label-2", storedCard.keyFields[1].label)
    }

    @Test
    fun updateFavorite_changesOnlyFavoriteState() = runBlocking {
        val result = sampleResult(imageId = "favorite-card", isFavorite = false)
        dao.saveAnalysisResults(listOf(ScreenshotCardSaveEntry(result)))

        dao.updateFavorite(
            imageId = "favorite-card",
            isFavorite = true,
            updatedAtMillis = 9_999L,
        )

        val storedCard = dao.getCardByImageId("favorite-card")

        assertNotNull(storedCard)
        assertTrue(storedCard!!.card.isFavorite)
        assertEquals("favorite-card", storedCard.card.imageId)
        assertEquals(9_999L, storedCard.card.updatedAtMillis)
    }

    @Test
    fun deleteAllCards_removesAllCardsAndKeyFields() = runBlocking {
        val firstResult = sampleResult(imageId = "first")
        val secondResult = sampleResult(imageId = "second")
        dao.saveAnalysisResults(
            listOf(
                ScreenshotCardSaveEntry(firstResult),
                ScreenshotCardSaveEntry(secondResult),
            ),
        )

        dao.deleteAllCards()

        assertTrue(dao.observeAllCards().first().isEmpty())
        assertNull(dao.getCardByImageId("first"))
        assertNull(dao.getCardByImageId("second"))
    }

    @Test
    fun repositoryDeleteAllCards_removesAllStoredCards() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        repository.saveAnalysisResults(listOf(sampleResult(imageId = "card-1")))
        repository.saveAnalysisResults(listOf(sampleResult(imageId = "card-2")))

        repository.deleteAllCards()

        assertTrue(repository.observeStoredCards().first().isEmpty())
    }

    @Test
    fun deleteByImageId_removesCardAndKeyFields() = runBlocking {
        val result = sampleResult(imageId = "delete-me")
        dao.saveAnalysisResults(listOf(ScreenshotCardSaveEntry(result)))

        dao.deleteByImageId("delete-me")

        assertNull(dao.getCardByImageId("delete-me"))
        assertTrue(dao.observeAllCards().first().isEmpty())
    }

    @Test
    fun repositorySaveAndObserve_roundTripPreservesAnalysisResult() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val result = sampleResult(
            imageId = "round-trip",
            title = "Round trip title",
            summary = "Round trip summary",
            isFavorite = false,
        )

        repository.saveAnalysisResults(listOf(result))

        val storedCard = repository.getCard("round-trip")

        assertNotNull(storedCard)
        assertEquals(result.copy(), storedCard!!.analysisResult)
        assertFalse(storedCard.analysisResult.isFavorite)
    }

    private fun sampleResult(
        imageId: String,
        title: String = "title-$imageId",
        summary: String = "summary-$imageId",
        isFavorite: Boolean = false,
        keyFields: List<ScreenshotKeyField> = listOf(
            keyField(label = "label-1", value = "value-1", priority = 1),
        ),
    ): ScreenshotAnalysisResult {
        return ScreenshotAnalysisResult(
            imageId = imageId,
            title = title,
            summary = summary,
            contentTypes = ScreenshotContentTypes(
                primaryContentType = ScreenshotContentType.SHOPPING_PRODUCT,
            ),
            keyFields = keyFields,
            confidence = ScreenshotAnalysisConfidence.HIGH,
            isFavorite = isFavorite,
        )
    }

    private fun keyField(
        label: String,
        value: String,
        priority: Int,
    ): ScreenshotKeyField {
        return ScreenshotKeyField(
            label = label,
            value = value,
            displayPriority = priority,
            isSensitive = false,
        )
    }
}
