package com.chalkak.recap.core.data.backend

import javax.inject.Provider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BackendSelectionTest {
    @Test
    fun `select returns mock and does not get remote when useMock is true`() {
        val mock = Any()
        val remote = Any()
        val mockProvider = CountingProvider(mock)
        val remoteProvider = CountingProvider(remote)

        val selected = BackendSelection.select(
            useMockBackend = true,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )

        assertSame(mock, selected)
        assertEquals(1, mockProvider.getCount)
        assertEquals(0, remoteProvider.getCount)
    }

    @Test
    fun `select returns remote and does not get mock when useMock is false`() {
        val mock = Any()
        val remote = Any()
        val mockProvider = CountingProvider(mock)
        val remoteProvider = CountingProvider(remote)

        val selected = BackendSelection.select(
            useMockBackend = false,
            mockProvider = mockProvider,
            remoteProvider = remoteProvider,
        )

        assertSame(remote, selected)
        assertEquals(0, mockProvider.getCount)
        assertEquals(1, remoteProvider.getCount)
    }

    @Test
    fun `backendModeLabel maps mock and remote`() {
        assertEquals("mock", BackendSelection.backendModeLabel(useMockBackend = true))
        assertEquals("remote", BackendSelection.backendModeLabel(useMockBackend = false))
    }

    private class CountingProvider<T>(private val value: T) : Provider<T> {
        var getCount: Int = 0
            private set

        override fun get(): T {
            getCount += 1
            return value
        }
    }
}

class UseMockBackendPropertyTest {
    @Test
    fun `parseOverride accepts true false and absent`() {
        assertEquals(true, UseMockBackendProperty.parseOverride("true"))
        assertEquals(false, UseMockBackendProperty.parseOverride("false"))
        assertEquals(null, UseMockBackendProperty.parseOverride(null))
    }

    @Test
    fun `parseOverride rejects invalid values`() {
        val error = assertThrows(IllegalStateException::class.java) {
            UseMockBackendProperty.parseOverride("invalid")
        }
        assertTrue(error.message!!.contains("Invalid USE_MOCK_BACKEND='invalid'"))
    }

    @Test
    fun `effectiveForDebug uses mock default and honors override`() {
        assertTrue(UseMockBackendProperty.effectiveForDebug(override = null))
        assertFalse(UseMockBackendProperty.effectiveForDebug(override = false))
        assertTrue(UseMockBackendProperty.effectiveForDebug(override = true))
    }

    @Test
    fun `effectiveForRelease is always remote`() {
        assertFalse(UseMockBackendProperty.effectiveForRelease())
    }
}
