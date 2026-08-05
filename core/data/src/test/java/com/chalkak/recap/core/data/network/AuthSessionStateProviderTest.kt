package com.chalkak.recap.core.data.network

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthSessionStateProviderTest {
    private val refreshToken = MutableStateFlow<String?>(null)
    private val sessionTokenStore = mockk<SessionTokenStore>().also { store ->
        every { store.refreshToken } returns refreshToken
    }

    @Test
    fun `has no session when refresh token is absent`() = runTest {
        AuthSessionStateProvider(sessionTokenStore).hasSession.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `has no session when refresh token is blank`() = runTest {
        refreshToken.value = "  "

        AuthSessionStateProvider(sessionTokenStore).hasSession.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `has session when refresh token is present`() = runTest {
        refreshToken.value = "refresh-token"

        AuthSessionStateProvider(sessionTokenStore).hasSession.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cleared refresh token drops the session`() = runTest {
        refreshToken.value = "refresh-token"

        AuthSessionStateProvider(sessionTokenStore).hasSession.test {
            assertTrue(awaitItem())

            refreshToken.value = null

            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rotated refresh token does not re-emit session state`() = runTest {
        refreshToken.value = "first-token"

        AuthSessionStateProvider(sessionTokenStore).hasSession.test {
            assertEquals(true, awaitItem())

            refreshToken.value = "second-token"

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
