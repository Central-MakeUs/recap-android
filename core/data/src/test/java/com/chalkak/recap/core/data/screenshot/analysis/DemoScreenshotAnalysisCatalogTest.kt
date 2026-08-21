package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class DemoScreenshotAnalysisCatalogTest {
    @Test
    fun `catalog covers twenty unique demo file names`() {
        val fileNames = DemoScreenshotAnalysisCatalog.fileNames

        assertEquals(EXPECTED_FILE_NAMES, fileNames)
        assertEquals(20, DemoScreenshotAnalysisCatalog.results.size)
    }

    @Test
    fun `catalog capture ids are unique and stable per file`() {
        val results = DemoScreenshotAnalysisCatalog.results
        val ids = results.map { it.captureId }

        assertEquals(20, ids.toSet().size)
        assertEquals(
            (2_026_000_001L..2_026_000_020L).toList(),
            ids,
        )
        EXPECTED_FILE_NAMES.forEachIndexed { index, fileName ->
            val result = DemoScreenshotAnalysisCatalog.resultForFileName(fileName)
            assertNotNull(result)
            assertEquals(2_026_000_001L + index, result?.captureId)
        }
    }

    @Test
    fun `catalog results fill required analysis fields`() {
        DemoScreenshotAnalysisCatalog.results.forEach { result ->
            assertTrue(result.title.isNotBlank(), "blank title for ${result.captureId}")
            assertTrue(result.summary.isNotBlank(), "blank summary for ${result.captureId}")
            assertTrue(result.body.isNotBlank(), "blank body for ${result.captureId}")
            assertFalse(result.body.startsWith("\n"), "leading newline for ${result.captureId}")
            result.body.lineSequence().forEach { line ->
                if (line.isEmpty()) return@forEach
                assertFalse(
                    line.startsWith(" ") || line.startsWith("\t"),
                    "indented body line for ${result.captureId}: $line",
                )
            }
            assertFalse(result.isFavorite)
            assertEquals("mock://captures/${result.captureId}", result.originalImageUrl)
            assertTrue(result.organizedAt.atZone(ZoneOffset.UTC).year >= 2025)
        }
    }

    @Test
    fun `catalog type codes match intended demo mix`() {
        val byFile = EXPECTED_FILE_NAMES.associateWith { fileName ->
            DemoScreenshotAnalysisCatalog.resultForFileName(fileName)?.typeCode
        }

        assertEquals(ScreenshotContentType.SCHEDULE, byFile["demo_1.jpeg"])
        assertEquals(ScreenshotContentType.KNOWLEDGE, byFile["demo_2.png"])
        assertEquals(ScreenshotContentType.KNOWLEDGE, byFile["demo_3.png"])
        assertEquals(ScreenshotContentType.SCHEDULE, byFile["demo_4.png"])
        assertEquals(ScreenshotContentType.PLACE, byFile["demo_5.png"])
        assertEquals(ScreenshotContentType.KNOWLEDGE, byFile["demo_6.png"])
        assertEquals(ScreenshotContentType.PLACE, byFile["demo_7.png"])
        assertEquals(ScreenshotContentType.JOB, byFile["demo_8.jpg"])
        assertEquals(ScreenshotContentType.KNOWLEDGE, byFile["demo_9.jpg"])
        assertEquals(ScreenshotContentType.KNOWLEDGE, byFile["demo_10.png"])
        assertEquals(ScreenshotContentType.SCHEDULE, byFile["demo_11.jpg"])
        assertEquals(ScreenshotContentType.PLACE, byFile["demo_12.jpg"])
        assertEquals(ScreenshotContentType.CONTENT, byFile["demo_13.jpg"])
        assertEquals(ScreenshotContentType.PLACE, byFile["demo_14.png"])
        assertEquals(ScreenshotContentType.JOB, byFile["demo_15.png"])
        assertEquals(ScreenshotContentType.BENEFIT, byFile["demo_16.png"])
        assertEquals(ScreenshotContentType.RECORD, byFile["demo_17.jpg"])
        assertEquals(ScreenshotContentType.CONTENT, byFile["demo_18.jpg"])
        assertEquals(ScreenshotContentType.SCHEDULE, byFile["demo_19.jpeg"])
        assertEquals(ScreenshotContentType.JOB, byFile["demo_20.jpeg"])
    }

    @Test
    fun `resultForFileName ignores directory prefixes`() {
        val direct = DemoScreenshotAnalysisCatalog.resultForFileName("demo_11.jpg")
        val prefixed = DemoScreenshotAnalysisCatalog.resultForFileName(
            "C:/Users/ddddd/Downloads/recap-demo-sample-20/demo_11.jpg",
        )

        assertEquals(direct, prefixed)
        assertEquals("극장판 귀멸의 칼날: 무한성편 예매 완료", direct?.title)
    }

    private companion object {
        val EXPECTED_FILE_NAMES = setOf(
            "demo_1.jpeg",
            "demo_2.png",
            "demo_3.png",
            "demo_4.png",
            "demo_5.png",
            "demo_6.png",
            "demo_7.png",
            "demo_8.jpg",
            "demo_9.jpg",
            "demo_10.png",
            "demo_11.jpg",
            "demo_12.jpg",
            "demo_13.jpg",
            "demo_14.png",
            "demo_15.png",
            "demo_16.png",
            "demo_17.jpg",
            "demo_18.jpg",
            "demo_19.jpeg",
            "demo_20.jpeg",
        )
    }
}
