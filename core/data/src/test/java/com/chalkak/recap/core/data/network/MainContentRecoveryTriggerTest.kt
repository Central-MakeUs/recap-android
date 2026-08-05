package com.chalkak.recap.core.data.network

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainContentRecoveryTriggerTest {
    private val networkConnectivityMonitor = mockk<NetworkConnectivityMonitor>()
    private val validatedInternet = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    private val processLifecycle = mockk<Lifecycle>()
    private val lifecycleObserver = slot<DefaultLifecycleObserver>()

    @Test
    fun `emits recovery when validated internet flips from false to true`() =
        runTest(UnconfinedTestDispatcher()) {
            every { networkConnectivityMonitor.validatedInternet } returns validatedInternet
            every { networkConnectivityMonitor.isInternetValidated() } returns false
            every { processLifecycle.addObserver(capture(lifecycleObserver)) } just Runs

            val trigger = createTrigger()
            trigger.recoveries.test {
                validatedInternet.emit(false)
                expectNoEvents()

                validatedInternet.emit(true)
                awaitItem()

                validatedInternet.emit(true)
                expectNoEvents()

                validatedInternet.emit(false)
                expectNoEvents()

                validatedInternet.emit(true)
                awaitItem()

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emits recovery on foreground start only when internet is validated`() =
        runTest(UnconfinedTestDispatcher()) {
            every { networkConnectivityMonitor.validatedInternet } returns validatedInternet
            every { networkConnectivityMonitor.isInternetValidated() } returns false
            every { processLifecycle.addObserver(capture(lifecycleObserver)) } just Runs

            val trigger = createTrigger()
            val owner = mockk<LifecycleOwner>()
            trigger.recoveries.test {
                lifecycleObserver.captured.onStart(owner)
                expectNoEvents()

                every { networkConnectivityMonitor.isInternetValidated() } returns true
                lifecycleObserver.captured.onStart(owner)
                awaitItem()

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun TestScope.createTrigger(): MainContentRecoveryTrigger {
        return MainContentRecoveryTrigger(
            networkConnectivityMonitor = networkConnectivityMonitor,
            processLifecycle = processLifecycle,
            scope = backgroundScope,
        )
    }
}
