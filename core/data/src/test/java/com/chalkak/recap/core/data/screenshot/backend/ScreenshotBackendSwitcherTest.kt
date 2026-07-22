package com.chalkak.recap.core.data.screenshot.backend

import com.chalkak.recap.core.data.screenshot.analysis.ScreenshotAnalysisRunState

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class ScreenshotBackendSwitcherTest {
    private val modeFlow = MutableStateFlow(ScreenshotBackendMode.MOCK)
    private val modeStore = mockk<ScreenshotBackendModeStore>()
    private val resetter = mockk<MockScreenshotDataResetter>()
    private val analysisRunState = ScreenshotAnalysisRunState()

    @Test
    fun `same mode switch is no-op success without reset`() = runTest {
        every { modeStore.mode } returns modeFlow
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        val switcher = createSwitcher()

        val result = switcher.switchTo(ScreenshotBackendMode.MOCK)

        assertEquals(ScreenshotBackendSwitchResult.Success, result)
        coVerify(exactly = 0) { resetter.reset() }
        coVerify(exactly = 0) { modeStore.setMode(any()) }
    }

    @Test
    fun `successful switch resets mock data then saves mode`() = runTest {
        every { modeStore.mode } returns modeFlow
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { resetter.reset() } returns Unit
        coEvery { modeStore.setMode(ScreenshotBackendMode.REMOTE) } coAnswers {
            modeFlow.value = ScreenshotBackendMode.REMOTE
        }
        val switcher = createSwitcher()

        val result = switcher.switchTo(ScreenshotBackendMode.REMOTE)

        assertEquals(ScreenshotBackendSwitchResult.Success, result)
        coVerifyOrder {
            resetter.reset()
            modeStore.setMode(ScreenshotBackendMode.REMOTE)
        }
        assertFalse(switcher.isSwitching.value)
    }

    @Test
    fun `analysis running rejects switch without changing mode`() = runTest {
        every { modeStore.mode } returns modeFlow
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        analysisRunState.beginRun()
        val switcher = createSwitcher()

        val result = switcher.switchTo(ScreenshotBackendMode.REMOTE)

        assertEquals(ScreenshotBackendSwitchResult.RejectedBusy, result)
        coVerify(exactly = 0) { resetter.reset() }
        coVerify(exactly = 0) { modeStore.setMode(any()) }
    }

    @Test
    fun `reset failure keeps previous mode`() = runTest {
        every { modeStore.mode } returns modeFlow
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { resetter.reset() } throws RuntimeException("reset failed")
        val switcher = createSwitcher()

        val result = switcher.switchTo(ScreenshotBackendMode.REMOTE)

        assertEquals(ScreenshotBackendSwitchResult.Failure, result)
        coVerify(exactly = 0) { modeStore.setMode(any()) }
        assertFalse(switcher.isSwitching.value)
    }

    @Test
    fun `setMode failure keeps previous mode after reset`() = runTest {
        every { modeStore.mode } returns modeFlow
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { resetter.reset() } returns Unit
        coEvery { modeStore.setMode(ScreenshotBackendMode.REMOTE) } throws RuntimeException("save failed")
        val switcher = createSwitcher()

        val result = switcher.switchTo(ScreenshotBackendMode.REMOTE)

        assertEquals(ScreenshotBackendSwitchResult.Failure, result)
        coVerify(exactly = 1) { resetter.reset() }
        assertEquals(ScreenshotBackendMode.MOCK, modeFlow.value)
        assertFalse(switcher.isSwitching.value)
    }

    @Test
    fun `concurrent switch is rejected while first switch is in progress`() = runTest {
        every { modeStore.mode } returns modeFlow
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        coEvery { resetter.reset() } coAnswers {
            delay(100.milliseconds)
        }
        coEvery { modeStore.setMode(ScreenshotBackendMode.REMOTE) } coAnswers {
            modeFlow.value = ScreenshotBackendMode.REMOTE
        }
        val switcher = createSwitcher()

        val first = async { switcher.switchTo(ScreenshotBackendMode.REMOTE) }
        launch {
            delay(10.milliseconds)
            val second = switcher.switchTo(ScreenshotBackendMode.REMOTE)
            assertEquals(ScreenshotBackendSwitchResult.RejectedBusy, second)
        }
        assertEquals(ScreenshotBackendSwitchResult.Success, first.await())
        assertTrue(switcher.isSwitching.value.not())
    }

    private fun createSwitcher(): ScreenshotBackendSwitcher {
        return ScreenshotBackendSwitcher(
            modeStore = modeStore,
            mockScreenshotDataResetter = resetter,
            screenshotAnalysisRunState = analysisRunState,
        )
    }
}
