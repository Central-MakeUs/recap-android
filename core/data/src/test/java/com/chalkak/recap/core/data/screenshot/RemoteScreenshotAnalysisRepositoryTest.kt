package com.chalkak.recap.core.data.screenshot

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RemoteScreenshotAnalysisRepositoryTest {
    private val repository = RemoteScreenshotAnalysisRepository()

    @Test
    fun `analyze throws RemoteAnalysisNotWiredException`() = runTest {
        val result = runCatching {
            repository.analyze(ScreenshotAnalysisInput(fileName = "a.png"))
        }

        assertTrue(result.exceptionOrNull() is RemoteAnalysisNotWiredException)
    }

    @Test
    fun `analyze list throws RemoteAnalysisNotWiredException`() = runTest {
        val result = runCatching {
            repository.analyze(listOf(ScreenshotAnalysisInput(fileName = "a.png")))
        }

        assertTrue(result.exceptionOrNull() is RemoteAnalysisNotWiredException)
    }
}

class ScreenshotAnalysisRunStateTest {
    @Test
    fun `begin and end restore idle state`() {
        val runState = ScreenshotAnalysisRunState()

        assertFalse(runState.isRunning.value)
        runState.beginRun()
        assertTrue(runState.isRunning.value)
        runState.endRun()
        assertFalse(runState.isRunning.value)
    }

    @Test
    fun `overlapping runs stay running until last end`() {
        val runState = ScreenshotAnalysisRunState()

        runState.beginRun()
        runState.beginRun()
        runState.endRun()
        assertTrue(runState.isRunning.value)

        runState.endRun()
        assertFalse(runState.isRunning.value)
    }

    @Test
    fun `extra endRun keeps idle without going negative`() {
        val runState = ScreenshotAnalysisRunState()

        runState.endRun()
        assertFalse(runState.isRunning.value)

        runState.beginRun()
        runState.endRun()
        runState.endRun()
        assertFalse(runState.isRunning.value)
    }
}
