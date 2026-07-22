package com.chalkak.recap.core.data.screenshot.persistence

import android.content.Context
import androidx.room.Room
import com.chalkak.recap.core.data.RecapDatabase
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
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
import java.time.Instant

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
    fun saveAnalysisResults_persistsCardsInOrganizedAtMillisDescendingOrder() = runBlocking {
        val firstResult = sampleResult(captureId = 1L, organizedAt = Instant.ofEpochMilli(1000L))
        val secondResult = sampleResult(captureId = 2L, organizedAt = Instant.ofEpochMilli(2000L))

        dao.saveAnalysisResults(
            entries = listOf(
                ScreenshotCardSaveEntry(firstResult),
                ScreenshotCardSaveEntry(secondResult),
            ),
        )

        val storedCards = dao.observeAllCards().first()

        assertEquals(2, storedCards.size)
        assertEquals(2L, storedCards[0].captureId)
        assertEquals(1L, storedCards[1].captureId)
    }

    @Test
    fun saveAnalysisResults_roundTripsAllFields() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val result = sampleResult(
            captureId = 100L,
            title = "Round trip title",
            summary = "Round trip summary",
            body = "Round trip body",
            typeCode = ScreenshotContentType.SCHEDULE,
            originalImageUrl = "mock://captures/100",
            organizedAt = Instant.ofEpochMilli(5000L),
        )

        repository.saveAnalysisResults(listOf(result))

        val storedCard = repository.getCard(100L)

        assertNotNull(storedCard)
        assertEquals(100L, storedCard!!.analysisResult.captureId)
        assertEquals("Round trip title", storedCard.analysisResult.title)
        assertEquals("Round trip summary", storedCard.analysisResult.summary)
        assertEquals("Round trip body", storedCard.analysisResult.body)
        assertEquals(ScreenshotContentType.SCHEDULE, storedCard.analysisResult.typeCode)
        assertEquals("mock://captures/100", storedCard.analysisResult.originalImageUrl)
        assertEquals(Instant.ofEpochMilli(5000L), storedCard.analysisResult.organizedAt)
        assertFalse(storedCard.analysisResult.isFavorite)
    }

    @Test
    fun updateFavorite_changesOnlyFavoriteState() = runBlocking {
        val result = sampleResult(captureId = 1L)
        dao.saveAnalysisResults(listOf(ScreenshotCardSaveEntry(result)))

        dao.updateFavorite(
            captureId = 1L,
            isFavorite = true,
            updatedAtMillis = 9_999L,
        )

        val storedCard = dao.getCardByCaptureId(1L)

        assertNotNull(storedCard)
        assertTrue(storedCard!!.isFavorite)
        assertEquals(1L, storedCard.captureId)
        assertEquals(9_999L, storedCard.updatedAtMillis)
    }

    @Test
    fun deleteAllCards_removesAllCards() = runBlocking {
        val firstResult = sampleResult(captureId = 1L)
        val secondResult = sampleResult(captureId = 2L)
        dao.saveAnalysisResults(
            listOf(
                ScreenshotCardSaveEntry(firstResult),
                ScreenshotCardSaveEntry(secondResult),
            ),
        )

        dao.deleteAllCards()

        assertTrue(dao.observeAllCards().first().isEmpty())
        assertNull(dao.getCardByCaptureId(1L))
        assertNull(dao.getCardByCaptureId(2L))
    }

    @Test
    fun repositoryDeleteAllCards_removesAllStoredCards() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        repository.saveAnalysisResults(listOf(sampleResult(captureId = 1L)))
        repository.saveAnalysisResults(listOf(sampleResult(captureId = 2L)))

        repository.deleteAllCards()

        assertTrue(repository.observeStoredCards().first().isEmpty())
    }

    @Test
    fun deleteByCaptureId_removesCard() = runBlocking {
        val result = sampleResult(captureId = 1L)
        dao.saveAnalysisResults(listOf(ScreenshotCardSaveEntry(result)))

        dao.deleteByCaptureId(1L)

        assertNull(dao.getCardByCaptureId(1L))
        assertTrue(dao.observeAllCards().first().isEmpty())
    }

    @Test
    fun repositoryDeleteCards_removesOnlySelectedCards() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        repository.saveAnalysisResults(
            listOf(
                sampleResult(captureId = 1L, title = "Selected-1"),
                sampleResult(captureId = 2L, title = "Kept"),
                sampleResult(captureId = 3L, title = "Selected-2"),
            ),
        )

        repository.deleteCards(setOf(1L, 3L))

        val remainingCards = repository.observeStoredCards().first()
        assertEquals(listOf(2L), remainingCards.map { it.analysisResult.captureId })
        assertNull(dao.getCardByCaptureId(1L))
        assertNull(dao.getCardByCaptureId(3L))
        assertNotNull(dao.getCardByCaptureId(2L))
    }

    @Test
    fun repositoryDeleteCards_chunksSelectionsAboveSqliteBindLimit() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val captureIds = (1L..901L).toSet()
        repository.saveAnalysisResults(
            captureIds.map { id -> sampleResult(captureId = id) },
        )

        repository.deleteCards(captureIds)

        assertTrue(repository.observeStoredCards().first().isEmpty())
    }

    @Test
    fun observeCard_emitsSingleCardUpdates() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        repository.saveAnalysisResults(
            listOf(sampleResult(captureId = 1L, title = "Before")),
        )

        val before = repository.observeCard(1L).first()
        assertNotNull(before)
        assertEquals("Before", before!!.analysisResult.title)

        repository.updateCardContent(
            captureId = 1L,
            title = "After",
            summary = before.analysisResult.summary,
            body = before.analysisResult.body,
            typeCode = before.analysisResult.typeCode,
        )

        val after = repository.observeCard(1L).first()
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
            captureId = 1L,
            title = "Old title",
            summary = "Old summary",
            body = "Old body",
            typeCode = ScreenshotContentType.SHOPPING,
        ).copy(isFavorite = true)
        repository.saveAnalysisResults(
            results = listOf(result),
            imageRefsByCaptureId = mapOf(1L to imageRefs),
        )
        val before = repository.getCard(1L)!!

        val updated = repository.updateCardContent(
            captureId = 1L,
            title = "New title",
            summary = "New summary",
            body = "New body",
            typeCode = ScreenshotContentType.SCHEDULE,
            updatedAtMillis = 12_345L,
        )

        assertTrue(updated)
        val after = repository.getCard(1L)!!
        assertEquals("New title", after.analysisResult.title)
        assertEquals("New summary", after.analysisResult.summary)
        assertEquals("New body", after.analysisResult.body)
        assertEquals(ScreenshotContentType.SCHEDULE, after.analysisResult.typeCode)
        assertEquals(12_345L, after.updatedAtMillis)
        assertTrue(after.analysisResult.isFavorite)
        assertEquals(before.analysisResult.originalImageUrl, after.analysisResult.originalImageUrl)
        assertEquals(before.analysisResult.organizedAt, after.analysisResult.organizedAt)
        assertEquals(before.imageRefs, after.imageRefs)
    }

    @Test
    fun updateCardContent_returnsFalseForMissingCaptureId() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)

        val updated = repository.updateCardContent(
            captureId = 999L,
            title = "title",
            summary = "summary",
            body = "body",
            typeCode = ScreenshotContentType.ETC,
        )

        assertFalse(updated)
        assertNull(repository.getCard(999L))
    }

    @Test
    fun saveAndObserve_roundTripsEtcContentType() = runBlocking {
        val repository = DefaultScreenshotCardRepository(dao)
        val result = sampleResult(
            captureId = 1L,
            title = "기타 카드",
            typeCode = ScreenshotContentType.ETC,
        )

        repository.saveAnalysisResults(
            results = listOf(result),
            imageRefsByCaptureId = mapOf(
                1L to ScreenshotCardImageRefs(sourceImageUri = "content://etc-card"),
            ),
        )

        val observed = repository.observeStoredCards().first().single()
        val loaded = repository.getCard(1L)

        assertEquals(ScreenshotContentType.ETC, observed.analysisResult.typeCode)
        assertEquals(ScreenshotContentType.ETC, loaded?.analysisResult?.typeCode)
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
                    captureId = 1L,
                    title = "Original title",
                    summary = "Original summary",
                    body = "User edited body",
                ),
            ),
            imageRefsByCaptureId = mapOf(1L to imageRefs),
        )

        repository.saveAnalysisResults(
            results = listOf(
                sampleResult(
                    captureId = 1L,
                    title = "Reanalyzed title",
                    summary = "Reanalyzed summary",
                    body = "",
                ),
            ),
        )

        val stored = repository.getCard(1L)
        assertNotNull(stored)
        assertEquals("Reanalyzed title", stored!!.analysisResult.title)
        assertEquals("Reanalyzed summary", stored.analysisResult.summary)
        assertEquals("User edited body", stored.analysisResult.body)
        assertEquals(imageRefs, stored.imageRefs)
    }

    private fun sampleResult(
        captureId: Long,
        title: String = "title-$captureId",
        summary: String = "summary-$captureId",
        body: String = "body-$captureId",
        typeCode: ScreenshotContentType = ScreenshotContentType.SHOPPING,
        originalImageUrl: String = "mock://captures/$captureId",
        organizedAt: Instant = Instant.ofEpochMilli(1000L),
    ): ScreenshotAnalysisResult {
        return ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = typeCode,
            title = title,
            summary = summary,
            body = body,
            originalImageUrl = originalImageUrl,
            isFavorite = false,
            organizedAt = organizedAt,
        )
    }
}
