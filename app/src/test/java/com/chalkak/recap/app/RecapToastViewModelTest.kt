package com.chalkak.recap.app

import app.cash.turbine.test
import com.chalkak.recap.core.design.component.toast.RecapToastExitAnimationDurationMillis
import com.chalkak.recap.core.design.component.toast.RecapToastRequest
import com.chalkak.recap.core.design.component.toast.RecapToastType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RecapToastViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `enqueued toast is cleared after duration`() = runTest(testDispatcher) {
        val viewModel = RecapToastViewModel()

        viewModel.enqueue(
            RecapToastRequest(
                message = "saved",
                type = RecapToastType.Success,
                durationMillis = 2_000L,
            ),
        )
        runCurrent()

        assertEquals("saved", viewModel.currentToast.value?.message)

        advanceTimeBy(2_000L.milliseconds)
        runCurrent()

        assertNull(viewModel.currentToast.value)
    }

    @Test
    fun `duplicate messages stay in fifo and wait for exit animation`() = runTest(testDispatcher) {
        val viewModel = RecapToastViewModel()

        viewModel.enqueue(request("same", durationMillis = 1_000L))
        viewModel.enqueue(request("same", durationMillis = 1_000L))
        viewModel.enqueue(request("next", durationMillis = 1_000L))
        runCurrent()

        assertEquals("same", viewModel.currentToast.value?.message)

        advanceTimeBy(1_000L.milliseconds)
        runCurrent()
        assertNull(viewModel.currentToast.value)

        advanceTimeBy(RecapToastExitAnimationDurationMillis.toLong().milliseconds)
        runCurrent()
        assertEquals("same", viewModel.currentToast.value?.message)

        advanceTimeBy(1_000L.milliseconds)
        runCurrent()
        assertNull(viewModel.currentToast.value)

        advanceTimeBy(RecapToastExitAnimationDurationMillis.toLong().milliseconds)
        runCurrent()
        assertEquals("next", viewModel.currentToast.value?.message)
    }

    @Test
    fun `producer coroutine cancel does not clear enqueued toast`() = runTest(testDispatcher) {
        val viewModel = RecapToastViewModel()

        val producer = launch {
            viewModel.enqueue(request("keep", durationMillis = 2_000L))
        }
        runCurrent()
        producer.cancelAndJoin()

        assertEquals("keep", viewModel.currentToast.value?.message)

        advanceTimeBy(2_000L.milliseconds)
        runCurrent()

        assertNull(viewModel.currentToast.value)
    }

    @Test
    fun `collector replacement does not restart current toast duration`() = runTest(testDispatcher) {
        val viewModel = RecapToastViewModel()
        viewModel.enqueue(request("stable", durationMillis = 2_000L))
        runCurrent()

        viewModel.currentToast.test {
            assertEquals("stable", awaitItem()?.message)

            advanceTimeBy(1_000L.milliseconds)
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.currentToast.test {
            assertEquals("stable", awaitItem()?.message)

            advanceTimeBy(1_000L.milliseconds)
            assertNull(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toast expires without collectors when virtual time advances`() = runTest(testDispatcher) {
        val viewModel = RecapToastViewModel()
        viewModel.enqueue(request("background", durationMillis = 2_000L))
        runCurrent()

        assertEquals("background", viewModel.currentToast.value?.message)

        advanceTimeBy(2_000L.milliseconds)
        advanceUntilIdle()

        assertNull(viewModel.currentToast.value)
    }

    @Test
    fun `new viewModel does not restore previous queue`() = runTest(testDispatcher) {
        val first = RecapToastViewModel()
        first.enqueue(request("old", durationMillis = 5_000L))
        runCurrent()
        assertEquals("old", first.currentToast.value?.message)

        val second = RecapToastViewModel()
        runCurrent()

        assertNull(second.currentToast.value)
    }

    private fun request(
        message: String,
        durationMillis: Long,
        type: RecapToastType = RecapToastType.Success,
    ): RecapToastRequest = RecapToastRequest(
        message = message,
        type = type,
        durationMillis = durationMillis,
    )
}
