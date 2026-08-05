package com.chalkak.recap.core.data.network

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthSessionStateProviderTest {
    private val refreshToken = MutableStateFlow<String?>(null)
    private val sessionTokenStore = mockk<SessionTokenStore>().also { store ->
        every { store.refreshToken } returns refreshToken
    }
    private val provider = AuthSessionStateProvider(sessionTokenStore)

    @Test
    fun `hasSession is false when refresh token is absent`() = runTest {
        provider.hasSession.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasSession is false when refresh token is blank`() = runTest {
        refreshToken.value = "  "

        provider.hasSession.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasSession is true when refresh token is present`() = runTest {
        refreshToken.value = "refresh-token"

        provider.hasSession.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing refresh token emits signed out`() = runTest {
        refreshToken.value = "refresh-token"

        provider.hasSession.test {
            assertTrue(awaitItem())

            refreshToken.value = null

            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rotating refresh token does not emit duplicate session state`() = runTest {
        refreshToken.value = "first-token"

        provider.hasSession.test {
            assertTrue(awaitItem())

            refreshToken.value = "second-token"

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
