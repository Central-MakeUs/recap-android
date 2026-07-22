package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MockHomeRepositoryMappingTest {
    @Test
    fun `toHomeSummary keeps four most recent favorites`() {
        val cards = listOf(
            storedCard(1L, ScreenshotContentType.SHOPPING, isFavorite = true, organizedAtMillis = 4_000L),
            storedCard(2L, ScreenshotContentType.PLACE, isFavorite = true, organizedAtMillis = 3_000L),
            storedCard(3L, ScreenshotContentType.KNOWLEDGE, isFavorite = true, organizedAtMillis = 2_000L),
            storedCard(4L, ScreenshotContentType.CONTENT, isFavorite = true, organizedAtMillis = 1_000L),
            storedCard(5L, ScreenshotContentType.RECORD, isFavorite = true, organizedAtMillis = 500L),
            storedCard(6L, ScreenshotContentType.SHOPPING, isFavorite = false, organizedAtMillis = 5_000L),
        )

        val summary = cards.toHomeSummary()

        assertEquals(listOf(1L, 2L, 3L, 4L), summary.favorites.map { it.captureId })
        assertTrue(summary.favorites.all { it.isFavorite })
    }

    @Test
    fun `toHomeSummary keeps top four frequent save types by count`() {
        val cards = buildList {
            repeat(5) { index ->
                add(storedCard(100L + index, ScreenshotContentType.SHOPPING, organizedAtMillis = 1_000L + index))
            }
            repeat(4) { index ->
                add(storedCard(200L + index, ScreenshotContentType.PLACE, organizedAtMillis = 2_000L + index))
            }
            repeat(3) { index ->
                add(storedCard(300L + index, ScreenshotContentType.KNOWLEDGE, organizedAtMillis = 3_000L + index))
            }
            repeat(2) { index ->
                add(storedCard(400L + index, ScreenshotContentType.CONTENT, organizedAtMillis = 4_000L + index))
            }
            add(storedCard(500L, ScreenshotContentType.RECORD, organizedAtMillis = 5_000L))
        }

        val summary = cards.toHomeSummary()

        assertEquals(
            listOf(
                ScreenshotContentType.SHOPPING,
                ScreenshotContentType.PLACE,
                ScreenshotContentType.KNOWLEDGE,
                ScreenshotContentType.CONTENT,
            ),
            summary.topTypes.map { it.typeCode },
        )
        assertEquals(listOf(5L, 4L, 3L, 2L), summary.topTypes.map { it.count })
    }

    @Test
    fun `toHomeSummary maps recent screenshots newest first with limit three`() {
        val cards = listOf(
            storedCard(1L, ScreenshotContentType.SHOPPING, organizedAtMillis = 1_000L),
            storedCard(2L, ScreenshotContentType.PLACE, organizedAtMillis = 2_000L),
            storedCard(3L, ScreenshotContentType.JOB, organizedAtMillis = 3_000L),
            storedCard(4L, ScreenshotContentType.RECORD, organizedAtMillis = 4_000L),
        )

        val summary = cards.toHomeSummary()

        assertEquals(listOf(4L, 3L, 2L), summary.recentCaptures.map { it.captureId })
        assertTrue(summary.hasAnyCapture)
    }

    private fun storedCard(
        captureId: Long,
        contentType: ScreenshotContentType,
        isFavorite: Boolean = false,
        organizedAtMillis: Long,
    ): StoredScreenshotCard {
        val organizedAt = Instant.ofEpochMilli(organizedAtMillis)
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                captureId = captureId,
                typeCode = contentType,
                title = "title-$captureId",
                summary = "summary-$captureId",
                body = "body-$captureId",
                originalImageUrl = "mock://captures/$captureId",
                isFavorite = isFavorite,
                organizedAt = organizedAt,
            ),
            imageRefs = ScreenshotCardImageRefs(thumbnailPath = "thumb/$captureId"),
            updatedAtMillis = organizedAtMillis,
        )
    }
}
