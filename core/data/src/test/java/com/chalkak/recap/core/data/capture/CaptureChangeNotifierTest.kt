package com.chalkak.recap.core.data.capture

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CaptureChangeNotifierTest {
    private val notifier = CaptureChangeNotifier()

    @Test
    fun `emit publishes one typed change`() = runTest {
        notifier.changes.test {
            notifier.emit(CaptureChange.Deleted(setOf(1L, 2L)))

            assertEquals(CaptureChange.Deleted(setOf(1L, 2L)), awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
