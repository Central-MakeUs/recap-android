package com.chalkak.recap.app

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecapStartupGateTest {
    @Test
    fun `animation incomplete and startup loading blocks entry`() {
        assertFalse(
            canEnterRecapApp(
                lottieSplashComplete = false,
                startupUiState = RecapStartupUiState.Loading,
            ),
        )
    }

    @Test
    fun `animation complete and startup loading blocks entry`() {
        assertFalse(
            canEnterRecapApp(
                lottieSplashComplete = true,
                startupUiState = RecapStartupUiState.Loading,
            ),
        )
    }

    @Test
    fun `animation incomplete and startup ready blocks entry`() {
        assertFalse(
            canEnterRecapApp(
                lottieSplashComplete = false,
                startupUiState = RecapStartupUiState.Ready(onboardingCompleted = true),
            ),
        )
    }

    @Test
    fun `animation complete and startup ready allows entry`() {
        assertTrue(
            canEnterRecapApp(
                lottieSplashComplete = true,
                startupUiState = RecapStartupUiState.Ready(onboardingCompleted = false),
            ),
        )
    }

    @Test
    fun `animation failure treated as complete and startup ready allows entry`() {
        assertTrue(
            canEnterRecapApp(
                lottieSplashComplete = true,
                startupUiState = RecapStartupUiState.Ready(onboardingCompleted = true),
            ),
        )
    }
}
