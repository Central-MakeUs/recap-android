package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class SwitchingScreenshotAnalysisRepositoryTest {
    private val screenshotBackendModeStore = mockk<ScreenshotBackendModeStore>()
    private val mockRepository = mockk<MockScreenshotAnalysisRepository>()
    private val remoteRepository = mockk<RemoteScreenshotAnalysisRepository>()
    private val switchingRepository = SwitchingScreenshotAnalysisRepository(
        screenshotBackendModeStore = screenshotBackendModeStore,
        mockScreenshotAnalysisRepository = mockRepository,
        remoteScreenshotAnalysisRepository = remoteRepository,
    )

    @Test
    fun `delegates single analyze to mock when mode is MOCK`() = runTest {
        val input = ScreenshotAnalysisInput(fileName = "a.png")
        val expected = analysisResult(1L)
        coEvery { screenshotBackendModeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { mockRepository.analyze(input) } returns expected

        val result = switchingRepository.analyze(input)

        assertSame(expected, result)
        coVerify(exactly = 1) { mockRepository.analyze(input) }
        coVerify(exactly = 0) { remoteRepository.analyze(any<ScreenshotAnalysisInput>()) }
    }

    @Test
    fun `delegates single analyze to remote when mode is REMOTE`() = runTest {
        val input = ScreenshotAnalysisInput(fileName = "a.png")
        coEvery {
            screenshotBackendModeStore.currentMode()
        } returns ScreenshotBackendMode.REMOTE
        coEvery { remoteRepository.analyze(input) } throws UnsupportedOperationException("remote")

        val result = runCatching { switchingRepository.analyze(input) }

        assertTrue(result.exceptionOrNull() is UnsupportedOperationException)
        coVerify(exactly = 1) { remoteRepository.analyze(input) }
        coVerify(exactly = 0) { mockRepository.analyze(any<ScreenshotAnalysisInput>()) }
    }

    @Test
    fun `list overload delegates once to selected repository`() = runTest {
        val inputs = listOf(
            ScreenshotAnalysisInput(fileName = "a.png"),
            ScreenshotAnalysisInput(fileName = "b.png"),
        )
        val expected = listOf(analysisResult(1L), analysisResult(2L))
        coEvery { screenshotBackendModeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { mockRepository.analyze(inputs) } returns expected

        val results = switchingRepository.analyze(inputs)

        assertEquals(expected, results)
        coVerify(exactly = 1) { mockRepository.analyze(inputs) }
        coVerify(exactly = 1) { screenshotBackendModeStore.currentMode() }
    }

    @Test
    fun `organize delegates once to selected repository`() = runTest {
        val inputs = listOf(ScreenshotAnalysisInput(fileName = "a.png", uri = "content://1"))
        val expected = ScreenshotOrganizeOutcome.LocalResults(listOf(analysisResult(1L)))
        coEvery { screenshotBackendModeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { mockRepository.organize(inputs, any()) } returns expected

        val outcome = switchingRepository.organize(inputs)

        assertEquals(expected, outcome)
        coVerify(exactly = 1) { mockRepository.organize(inputs, any()) }
        coVerify(exactly = 0) { remoteRepository.organize(any(), any()) }
        coVerify(exactly = 1) { screenshotBackendModeStore.currentMode() }
    }

    private fun analysisResult(captureId: Long): ScreenshotAnalysisResult {
        return ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = ScreenshotContentType.ETC,
            title = "title-$captureId",
            summary = "summary-$captureId",
            body = "body-$captureId",
            originalImageUrl = "mock://captures/$captureId",
            isFavorite = false,
            organizedAt = Instant.ofEpochMilli(1000L),
        )
    }
}
