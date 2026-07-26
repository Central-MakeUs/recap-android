package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.model.capture.CaptureDetail
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CaptureDetailMappersTest {
    @Test
    fun `toStoredScreenshotCard maps fields and image refs`() {
        val detail = CaptureDetail(
            captureId = 42L,
            typeCode = ScreenshotContentType.PLACE,
            title = "title",
            summary = "summary",
            body = "body",
            originalImageUrl = "https://cdn.example/img.jpg",
            isFavorite = true,
            organizedAt = "2026-07-19T00:00:00Z",
        )

        val card = detail.toStoredScreenshotCard(
            thumbnailPath = "/cache/42.jpg",
            updatedAtMillis = 9_000L,
        )

        assertEquals(42L, card.analysisResult.captureId)
        assertEquals(ScreenshotContentType.PLACE, card.analysisResult.typeCode)
        assertEquals("title", card.analysisResult.title)
        assertEquals("summary", card.analysisResult.summary)
        assertEquals("body", card.analysisResult.body)
        assertEquals("https://cdn.example/img.jpg", card.analysisResult.originalImageUrl)
        assertEquals(true, card.analysisResult.isFavorite)
        assertEquals(Instant.parse("2026-07-19T00:00:00Z"), card.analysisResult.organizedAt)
        assertEquals("https://cdn.example/img.jpg", card.imageRefs.sourceImageUri)
        assertNull(card.imageRefs.storedImagePath)
        assertEquals("/cache/42.jpg", card.imageRefs.thumbnailPath)
        assertEquals(9_000L, card.updatedAtMillis)
    }

    @Test
    fun `parseOrganizedAt falls back for offset datetime`() {
        assertEquals(
            Instant.parse("2026-07-19T09:00:00Z"),
            parseOrganizedAt("2026-07-19T18:00:00+09:00"),
        )
    }

    @Test
    fun `parseOrganizedAt uses epoch when unparseable`() {
        assertEquals(Instant.EPOCH, parseOrganizedAt("not-a-date"))
    }
}
