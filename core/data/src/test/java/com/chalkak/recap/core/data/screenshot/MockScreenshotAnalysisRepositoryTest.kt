package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MockScreenshotAnalysisRepositoryTest {
    @Test
    fun `analyze composes title and summary from file name`() {
        val repository = repository(
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.0),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "capture_01.png"))

        assertEquals("스크린샷capture_01.png", result.title)
        assertEquals("요약capture_01.png", result.summary)
    }

    @Test
    fun `analyze maps content type index to enum`() {
        val repository = repository(
            contentTypeIndex = 3,
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.0),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(
            ScreenshotContentType.SCHEDULE_RESERVATION,
            result.contentTypes.primaryContentType,
        )
    }

    @Test
    fun `analyze generates exactly three key fields with fixed labels values and priorities`() {
        val repository = repository(
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.0),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(3, result.keyFields.size)
        assertEquals("라벨1", result.keyFields[0].label)
        assertEquals("값1", result.keyFields[0].value)
        assertEquals(1, result.keyFields[0].displayPriority)
        assertEquals("라벨2", result.keyFields[1].label)
        assertEquals("값2", result.keyFields[1].value)
        assertEquals(2, result.keyFields[1].displayPriority)
        assertEquals("라벨3", result.keyFields[2].label)
        assertEquals("값3", result.keyFields[2].value)
        assertEquals(3, result.keyFields[2].displayPriority)
    }

    @Test
    fun `analyze marks key field sensitive when random value is below threshold`() {
        val repository = repository(
            unitDoubles = listOf(0.04, 0.04, 0.04, 0.0),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertTrue(result.keyFields.all { it.isSensitive })
    }

    @Test
    fun `analyze keeps key field non-sensitive when random value reaches threshold`() {
        val repository = repository(
            unitDoubles = listOf(0.05, 0.05, 0.05, 0.0),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertFalse(result.keyFields.any { it.isSensitive })
    }

    @Test
    fun `analyze returns high confidence below high upper bound`() {
        val repository = repository(
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.59),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(ScreenshotAnalysisConfidence.HIGH, result.confidence)
    }

    @Test
    fun `analyze returns medium confidence at medium lower bound`() {
        val repository = repository(
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.60),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(ScreenshotAnalysisConfidence.MEDIUM, result.confidence)
    }

    @Test
    fun `analyze returns medium confidence below low lower bound`() {
        val repository = repository(
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.84),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(ScreenshotAnalysisConfidence.MEDIUM, result.confidence)
    }

    @Test
    fun `analyze returns low confidence at low lower bound`() {
        val repository = repository(
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.85),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals(ScreenshotAnalysisConfidence.LOW, result.confidence)
    }

    @Test
    fun `analyze uses injected image id`() {
        val repository = repository(
            imageId = "mock-image-id",
            unitDoubles = listOf(0.0, 0.0, 0.0, 0.0),
        )

        val result = repository.analyze(ScreenshotAnalysisInput(fileName = "sample.png"))

        assertEquals("mock-image-id", result.imageId)
    }

    @Test
    fun `analyze batch preserves input order`() {
        val repository = repository(
            imageIds = listOf("first-id", "second-id"),
            unitDoubles = listOf(
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0,
            ),
        )

        val results = repository.analyze(
            inputs = listOf(
                ScreenshotAnalysisInput(fileName = "first.png"),
                ScreenshotAnalysisInput(fileName = "second.png"),
            ),
        )

        assertEquals(2, results.size)
        assertEquals("first-id", results[0].imageId)
        assertEquals("스크린샷first.png", results[0].title)
        assertEquals("second-id", results[1].imageId)
        assertEquals("스크린샷second.png", results[1].title)
    }

    private fun repository(
        imageId: String = "default-image-id",
        imageIds: List<String>? = null,
        contentTypeIndex: Int = 0,
        unitDoubles: List<Double>,
    ): MockScreenshotAnalysisRepository {
        val imageIdIterator = (imageIds ?: listOf(imageId)).iterator()
        val unitDoubleIterator = unitDoubles.iterator()
        return MockScreenshotAnalysisRepository(
            randomizer = ScreenshotMockRandomizer(
                nextImageId = {
                    if (imageIdIterator.hasNext()) {
                        imageIdIterator.next()
                    } else {
                        imageId
                    }
                },
                nextUnitDouble = {
                    require(unitDoubleIterator.hasNext()) {
                        "No more unit doubles configured for test"
                    }
                    unitDoubleIterator.next()
                },
                nextContentTypeIndex = { contentTypeIndex },
            ),
        )
    }
}
