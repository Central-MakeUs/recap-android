package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class MockScreenshotAnalysisRepositoryTest {
    @Test
    fun `analyze composes title summary and body from file name`() = runTest {
        val repository = repository(captureId = 1L)

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "capture_01.png"))

        assertEquals("스크린샷capture_01.png", result.title)
        assertEquals("요약capture_01.png", result.summary)
        assertEquals("본문capture_01.png", result.body)
    }

    @Test
    fun `analyze maps content type index 3 to SCHEDULE`() = runTest {
        val repository = repository(captureId = 1L, contentTypeIndex = 3)

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(ScreenshotContentType.SCHEDULE, result.typeCode)
    }

    @Test
    fun `analyze maps last content type index to ETC`() = runTest {
        val repository = repository(
            captureId = 1L,
            contentTypeIndex = ScreenshotContentType.entries.lastIndex,
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(ScreenshotContentType.ETC, result.typeCode)
    }

    @Test
    fun `analyze uses injected captureId`() = runTest {
        val repository = repository(captureId = 42L)

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(42L, result.captureId)
    }

    @Test
    fun `analyze defaults isFavorite to false`() = runTest {
        val repository = repository(captureId = 1L)

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertFalse(result.isFavorite)
    }

    @Test
    fun `analyze batch preserves input order`() = runTest {
        val captureIds = mutableListOf(10L, 20L)
        val repository = repository(captureIds = captureIds)

        val results = repository.analyze(
            inputs = listOf(
                ScreenshotAnalysisInput(fileName = "first.png"),
                ScreenshotAnalysisInput(fileName = "second.png"),
            ),
        )

        assertEquals(2, results.size)
        assertEquals(10L, results[0].captureId)
        assertEquals("스크린샷first.png", results[0].title)
        assertEquals(20L, results[1].captureId)
        assertEquals("스크린샷second.png", results[1].title)
    }

    @Test
    fun `analyze generates nonblank mock originalImageUrl`() = runTest {
        val repository = repository(captureId = 99L)

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertTrue(result.originalImageUrl.isNotBlank())
        assertEquals("mock://captures/99", result.originalImageUrl)
    }

    @Test
    fun `analyze uses injected organizedAt instant`() = runTest {
        val fixedInstant = Instant.parse("2024-06-01T10:30:00Z")
        val repository = repository(captureId = 1L, organizedAt = fixedInstant)

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(fixedInstant, result.organizedAt)
    }

    @Test
    fun `default captureId suppliers from separate randomizers do not restart from one`() {
        val firstIds = List(8) { ScreenshotMockRandomizer().captureId() }
        val secondIds = List(8) { ScreenshotMockRandomizer().captureId() }
        val sequentialRestart = (1L..8L).toList()

        assertTrue(firstIds.all { it > 0L })
        assertTrue(secondIds.all { it > 0L })
        assertFalse(firstIds == sequentialRestart)
        assertFalse(secondIds == sequentialRestart)
        assertEquals(firstIds.size, firstIds.toSet().size)
        assertEquals(secondIds.size, secondIds.toSet().size)
        assertTrue(firstIds.toSet().intersect(secondIds.toSet()).isEmpty())
    }

    @Test
    fun `default captureId supplier yields positive ids that avoid sequential restart`() {
        val ids = List(16) { ScreenshotMockRandomizer.nextPositiveCaptureId() }

        assertTrue(ids.all { it > 0L })
        assertEquals(ids.size, ids.toSet().size)
        assertFalse(ids.take(8) == (1L..8L).toList())
    }

    private fun repository(
        captureId: Long = 1L,
        captureIds: List<Long>? = null,
        contentTypeIndex: Int = 0,
        organizedAt: Instant = Instant.parse("2024-01-01T00:00:00Z"),
    ): MockScreenshotAnalysisRepository {
        val captureIdIterator = (captureIds ?: listOf(captureId)).iterator()
        return MockScreenshotAnalysisRepository(
            randomizer = ScreenshotMockRandomizer(
                nextCaptureId = {
                    if (captureIdIterator.hasNext()) {
                        captureIdIterator.next()
                    } else {
                        captureId
                    }
                },
                nextOrganizedAt = { organizedAt },
                nextContentTypeIndex = { contentTypeIndex },
            ),
        )
    }
}
