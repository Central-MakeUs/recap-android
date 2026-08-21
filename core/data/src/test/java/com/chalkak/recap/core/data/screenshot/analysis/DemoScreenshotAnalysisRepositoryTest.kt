package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.data.screenshot.image.ScreenshotUploadPreparer
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DemoScreenshotAnalysisRepositoryTest {
    @Test
    fun `analyze returns catalog result after injected delay`() = runTest {
        val repository = repository(nextDelayMillis = { 900L })

        val result = repository.analyze(catalogInput("demo_1.jpeg"))

        assertEquals(DemoScreenshotAnalysisCatalog.resultForFileName("demo_1.jpeg"), result)
        assertEquals(900L, currentTime)
    }

    @Test
    fun `analyze throws for unknown file name without delaying`() = runTest {
        val repository = repository(nextDelayMillis = { 900L })

        val error = assertThrows<DemoScreenshotAnalysisException> {
            repository.analyze(ScreenshotAnalysisInput(fileName = "other.png"))
        }

        assertEquals("other.png", error.fileName)
        assertEquals(0L, currentTime)
    }

    @Test
    fun `organize returns catalog results and advances progress for known files`() = runTest {
        val repository = repository(nextDelayMillis = { 800L })
        val progress = mutableListOf<Pair<Int, Int>>()

        val outcome = repository.organize(
            inputs = listOf(
                catalogInput("demo_1.jpeg"),
                catalogInput("demo_8.jpg"),
            ),
            onProgress = { completed, total -> progress += completed to total },
        )

        val local = outcome as ScreenshotOrganizeOutcome.LocalResults
        assertEquals(
            listOf(
                DemoScreenshotAnalysisCatalog.resultForFileName("demo_1.jpeg"),
                DemoScreenshotAnalysisCatalog.resultForFileName("demo_8.jpg"),
            ),
            local.results,
        )
        assertEquals(0, local.analysisFailCount)
        assertEquals(0, local.preparationFailCount)
        assertEquals(listOf(1 to 2, 2 to 2), progress)
        assertEquals(
            DemoScreenshotAnalysisRepository.INITIAL_ANALYSIS_DELAY_MILLIS + 1_600L,
            currentTime,
        )
    }

    @Test
    fun `organize counts unknown file names as analysis failures`() = runTest {
        val repository = repository(nextDelayMillis = { 1_000L })
        val progress = mutableListOf<Pair<Int, Int>>()

        val outcome = repository.organize(
            inputs = listOf(
                catalogInput("demo_1.jpeg"),
                ScreenshotAnalysisInput(fileName = "unknown.png"),
            ),
            onProgress = { completed, total -> progress += completed to total },
        )

        val local = outcome as ScreenshotOrganizeOutcome.LocalResults
        assertEquals(
            listOf(DemoScreenshotAnalysisCatalog.resultForFileName("demo_1.jpeg")),
            local.results,
        )
        assertEquals(1, local.analysisFailCount)
        assertEquals(0, local.preparationFailCount)
        assertEquals(listOf(1 to 2, 2 to 2), progress)
        assertEquals(
            DemoScreenshotAnalysisRepository.INITIAL_ANALYSIS_DELAY_MILLIS + 1_000L,
            currentTime,
        )
    }

    @Test
    fun `organize emits all analysis failures when no catalog files are present`() = runTest {
        val repository = repository(nextDelayMillis = { 900L })

        val outcome = repository.organize(
            inputs = listOf(
                ScreenshotAnalysisInput(fileName = "a.png"),
                ScreenshotAnalysisInput(fileName = "b.png"),
            ),
        )

        val local = outcome as ScreenshotOrganizeOutcome.LocalResults
        assertTrue(local.results.isEmpty())
        assertEquals(2, local.analysisFailCount)
        assertEquals(DemoScreenshotAnalysisRepository.INITIAL_ANALYSIS_DELAY_MILLIS, currentTime)
    }

    @Test
    fun `organize counts preparation failure for catalog files without a readable image`() =
        runTest {
            val repository = repository(nextDelayMillis = { 800L })

            val outcome = repository.organize(
                inputs = listOf(
                    ScreenshotAnalysisInput(fileName = "demo_1.jpeg"),
                ),
            )

            val local = outcome as ScreenshotOrganizeOutcome.LocalResults
            assertTrue(local.results.isEmpty())
            assertEquals(1, local.preparationFailCount)
            assertEquals(0, local.analysisFailCount)
            assertEquals(
                DemoScreenshotAnalysisRepository.INITIAL_ANALYSIS_DELAY_MILLIS,
                currentTime
            )
        }

    @Test
    fun `random analysis delay stays inclusive between 800 and 1000 ms`() {
        val samples = List(40) { DemoScreenshotAnalysisRepository.randomAnalysisDelayMillis() }
        val range =
            DemoScreenshotAnalysisRepository.MIN_ANALYSIS_DELAY_MILLIS..
                    DemoScreenshotAnalysisRepository.MAX_ANALYSIS_DELAY_MILLIS

        assertTrue(samples.all { it in range })
    }

    @Test
    fun `analyze batch keeps catalog order`() = runTest {
        val repository = repository(nextDelayMillis = { 0L })

        val results = repository.analyze(
            listOf(
                catalogInput("demo_2.png"),
                catalogInput("demo_20.jpeg"),
            ),
        )

        assertSame(
            DemoScreenshotAnalysisCatalog.resultForFileName("demo_2.png"),
            results[0],
        )
        assertSame(
            DemoScreenshotAnalysisCatalog.resultForFileName("demo_20.jpeg"),
            results[1],
        )
    }

    private fun repository(
        nextDelayMillis: () -> Long,
        screenshotUploadPreparer: ScreenshotUploadPreparer = mockk {
            coEvery { prepare(any()) } throws IllegalStateException("unused")
        },
    ): DemoScreenshotAnalysisRepository {
        return DemoScreenshotAnalysisRepository(
            screenshotUploadPreparer = screenshotUploadPreparer,
            nextDelayMillis = nextDelayMillis,
        )
    }

    private fun catalogInput(fileName: String): ScreenshotAnalysisInput {
        val image = LocalImage(
            uri = "content://$fileName",
            displayName = fileName,
            dateAddedMillis = 1L,
        )
        return ScreenshotAnalysisInput(
            fileName = fileName,
            uri = image.uri,
            jpegBytes = byteArrayOf(1, 2, 3),
            contentType = PreparedScreenshot.MIME_TYPE_JPEG,
            localImage = image,
        )
    }
}
