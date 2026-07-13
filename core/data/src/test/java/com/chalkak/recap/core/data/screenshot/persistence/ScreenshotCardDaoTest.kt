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
    fun repositoryDeleteCards_removesOnlySelectedCardsAndKeyFields() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        repository.saveAnalysisResults(
            listOf(
                sampleResult(imageId = "selected-1"),
                sampleResult(imageId = "kept"),
                sampleResult(imageId = "selected-2"),
            ),
        )

        repository.deleteCards(setOf("selected-1", "selected-2"))

        val remainingCards = repository.observeStoredCards().first()
        assertEquals(listOf("kept"), remainingCards.map { it.analysisResult.imageId })
        assertNull(dao.getCardByImageId("selected-1"))
        assertNull(dao.getCardByImageId("selected-2"))
        assertNotNull(dao.getCardByImageId("kept"))
        database.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM screenshot_key_fields WHERE imageId IN (?, ?)",
            arrayOf<Any?>("selected-1", "selected-2"),
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun repositoryDeleteCards_chunksSelectionsAboveSqliteBindLimit() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val imageIds = (0..900).mapTo(linkedSetOf()) { index -> "selected-$index" }
        repository.saveAnalysisResults(
            imageIds.map { imageId ->
                sampleResult(imageId = imageId, keyFields = emptyList())
            },
        )

        repository.deleteCards(imageIds)

        assertTrue(repository.observeStoredCards().first().isEmpty())
    }

    @Test
    fun repositorySaveAndObserve_roundTripPreservesAnalysisResult() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val result = sampleResult(
            imageId = "round-trip",
            title = "Round trip title",
            summary = "Round trip summary",
            body = "Round trip body",
            isFavorite = false,
        )

        repository.saveAnalysisResults(listOf(result))

        val storedCard = repository.getCard("round-trip")

        assertNotNull(storedCard)
        assertEquals(result.copy(), storedCard!!.analysisResult)
        assertFalse(storedCard.analysisResult.isFavorite)
    }

    @Test
    fun observeCard_emitsSingleCardUpdates() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        repository.saveAnalysisResults(
            listOf(sampleResult(imageId = "observe-me", title = "Before")),
        )

        val before = repository.observeCard("observe-me").first()
        assertNotNull(before)
        assertEquals("Before", before!!.analysisResult.title)

        repository.updateCardContent(
            imageId = "observe-me",
            title = "After",
            summary = before.analysisResult.summary,
            body = before.analysisResult.body,
            primaryContentType = before.analysisResult.contentTypes.primaryContentType,
        )

        val after = repository.observeCard("observe-me").first()
        assertNotNull(after)
        assertEquals("After", after!!.analysisResult.title)
    }

    @Test
    fun updateCardContent_updatesEditableColumnsAndPreservesOthers() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val imageRefs = ScreenshotCardImageRefs(
            sourceImageUri = "content://source",
            storedImagePath = "/images/edit-me",
            thumbnailPath = "/thumbs/edit-me",
        )
        val result = sampleResult(
            imageId = "edit-me",
            title = "Old title",
            summary = "Old summary",
            body = "Old body",
            isFavorite = true,
            keyFields = listOf(keyField(label = "keep", value = "field", priority = 1)),
        )
        repository.saveAnalysisResults(
            results = listOf(result),
            imageRefsByImageId = mapOf("edit-me" to imageRefs),
        )
        val before = repository.getCard("edit-me")!!

        val updated = repository.updateCardContent(
            imageId = "edit-me",
            title = "New title",
            summary = "New summary",
            body = "New body",
            primaryContentType = ScreenshotContentType.SCHEDULE_RESERVATION,
            updatedAtMillis = 12_345L,
        )

        assertTrue(updated)
        val after = repository.getCard("edit-me")!!
        assertEquals("New title", after.analysisResult.title)
        assertEquals("New summary", after.analysisResult.summary)
        assertEquals("New body", after.analysisResult.body)
        assertEquals(
            ScreenshotContentType.SCHEDULE_RESERVATION,
            after.analysisResult.contentTypes.primaryContentType,
        )
        assertEquals(12_345L, after.updatedAtMillis)
        assertEquals(before.createdAtMillis, after.createdAtMillis)
        assertTrue(after.analysisResult.isFavorite)
        assertEquals(before.analysisResult.confidence, after.analysisResult.confidence)
        assertEquals(before.analysisResult.keyFields, after.analysisResult.keyFields)
        assertEquals(before.imageRefs, after.imageRefs)
    }

    @Test
    fun updateCardContent_returnsFalseForMissingImageId() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)

        val updated = repository.updateCardContent(
            imageId = "missing",
            title = "title",
            summary = "summary",
            body = "body",
            primaryContentType = ScreenshotContentType.OTHER,
        )

        assertFalse(updated)
        assertNull(repository.getCard("missing"))
    }

    @Test
    fun saveAndObserve_roundTripsOtherContentType() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val result = sampleResult(
            imageId = "other-card",
            title = "기타 카드",
            summary = "기타 요약",
        ).copy(
            contentTypes = ScreenshotContentTypes(
                primaryContentType = ScreenshotContentType.OTHER,
            ),
        )

        repository.saveAnalysisResults(
            results = listOf(result),
            imageRefsByImageId = mapOf(
                "other-card" to ScreenshotCardImageRefs(sourceImageUri = "content://other-card"),
            ),
        )

        val observed = repository.observeStoredCards().first().single()
        val loaded = repository.getCard("other-card")

        assertEquals(ScreenshotContentType.OTHER, observed.analysisResult.contentTypes.primaryContentType)
        assertEquals(ScreenshotContentType.OTHER, loaded?.analysisResult?.contentTypes?.primaryContentType)
    }

    @Test
    fun saveAnalysisResults_preservesExistingBodyAndImageRefsOnBlankResave() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val imageRefs = ScreenshotCardImageRefs(
            sourceImageUri = "content://source",
            storedImagePath = "/images/preserve-me",
            thumbnailPath = "/thumbs/preserve-me",
        )
        repository.saveAnalysisResults(
            results = listOf(
                sampleResult(
                    imageId = "preserve-me",
                    title = "Original title",
                    summary = "Original summary",
                    body = "User edited body",
                ),
            ),
            imageRefsByImageId = mapOf("preserve-me" to imageRefs),
        )

        repository.saveAnalysisResults(
            results = listOf(
                sampleResult(
                    imageId = "preserve-me",
                    title = "Reanalyzed title",
                    summary = "Reanalyzed summary",
                    body = "",
                ),
            ),
        )

        val stored = repository.getCard("preserve-me")
        assertNotNull(stored)
        assertEquals("Reanalyzed title", stored!!.analysisResult.title)
        assertEquals("Reanalyzed summary", stored.analysisResult.summary)
        assertEquals("User edited body", stored.analysisResult.body)
        assertEquals(imageRefs, stored.imageRefs)
    }

    private fun sampleResult(
        imageId: String,
        title: String = "title-$imageId",
        summary: String = "summary-$imageId",
        body: String = "body-$imageId",
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
            body = body,
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
