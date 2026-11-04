package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MockUsageStoreTest {
    private lateinit var store: MockUsageStore

    @BeforeEach
    fun setUp() {
        store = MockUsageStore(InMemoryPreferencesDataStore())
    }

    @Test
    fun `current defaults to zero used count`() = runTest {
        val usage = store.current()

        assertEquals(0, usage.usedCount)
        assertEquals(20, usage.limit)
        assertEquals(20, usage.remaining)
        assertEquals("", usage.resetAt)
    }

    @Test
    fun `recordSuccessfulSheets adds the success count`() = runTest {
        store.recordSuccessfulSheets(3)

        val usage = store.current()

        assertEquals(3, usage.usedCount)
        assertEquals(20, usage.limit)
        assertEquals(17, usage.remaining)
    }

    @Test
    fun `recordSuccessfulSheets keeps used count above the limit`() = runTest {
        store.recordSuccessfulSheets(25)

        val usage = store.current()

        assertEquals(25, usage.usedCount)
        assertEquals(20, usage.limit)
        assertEquals(0, usage.remaining)
    }

    @Test
    fun `recordSuccessfulSheets ignores non positive count`() = runTest {
        store.recordSuccessfulSheets(0)
        store.recordSuccessfulSheets(-2)

        assertEquals(0, store.current().usedCount)
    }

    @Test
    fun `reset sets used count back to zero`() = runTest {
        store.recordSuccessfulSheets(12)

        store.reset()

        val usage = store.current()
        assertEquals(0, usage.usedCount)
        assertEquals(20, usage.remaining)
    }
}
