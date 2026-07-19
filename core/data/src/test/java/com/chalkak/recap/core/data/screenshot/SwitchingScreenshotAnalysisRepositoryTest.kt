package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.data.UserPreferencesRepository
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
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val mockRepository = mockk<MockScreenshotAnalysisRepository>()
    private val remoteRepository = mockk<RemoteScreenshotAnalysisRepository>()
    private val switchingRepository = SwitchingScreenshotAnalysisRepository(
        userPreferencesRepository = userPreferencesRepository,
        mockScreenshotAnalysisRepository = mockRepository,
        remoteScreenshotAnalysisRepository = remoteRepository,
    )

    @Test
    fun `delegates single analyze to mock when mode is MOCK`() = runTest {
        val input = ScreenshotAnalysisInput(fileName = "a.png")
        val expected = analysisResult(1L)
        coEvery { userPreferencesRepository.getAnalysisDataSourceMode() } returns AnalysisDataSourceMode.MOCK
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
            userPreferencesRepository.getAnalysisDataSourceMode()
        } returns AnalysisDataSourceMode.REMOTE
        coEvery { remoteRepository.analyze(input) } throws RemoteAnalysisNotWiredException()

        val result = runCatching { switchingRepository.analyze(input) }

        assertTrue(result.exceptionOrNull() is RemoteAnalysisNotWiredException)
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
        coEvery { userPreferencesRepository.getAnalysisDataSourceMode() } returns AnalysisDataSourceMode.MOCK
        coEvery { mockRepository.analyze(inputs) } returns expected

        val results = switchingRepository.analyze(inputs)

        assertEquals(expected, results)
        coVerify(exactly = 1) { mockRepository.analyze(inputs) }
        coVerify(exactly = 1) { userPreferencesRepository.getAnalysisDataSourceMode() }
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
