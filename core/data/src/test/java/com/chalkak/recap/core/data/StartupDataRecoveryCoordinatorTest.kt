package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StartupDataRecoveryCoordinatorTest {
    private val localAppDataResetter = mockk<LocalAppDataResetter>(relaxed = true)

    @Test
    fun `does not reset when recovery marker is absent`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val coordinator = StartupDataRecoveryCoordinator(dataStore, localAppDataResetter)

        coordinator.recoverIfNeeded()

        coVerify(exactly = 0) { localAppDataResetter.resetDatabaseAndOnboarding() }
        assertFalse(UserPreferencesRecoveryMarker.isRequired(dataStore.current()))
    }

    @Test
    fun `resets local data and clears marker when marker is present`() = runTest {
        val dataStore = InMemoryPreferencesDataStore(
            UserPreferencesRecoveryMarker.createReplacementPreferences(),
        )
        val coordinator = StartupDataRecoveryCoordinator(dataStore, localAppDataResetter)

        coordinator.recoverIfNeeded()

        coVerify(exactly = 1) { localAppDataResetter.resetDatabaseAndOnboarding() }
        assertFalse(UserPreferencesRecoveryMarker.isRequired(dataStore.current()))
    }

    @Test
    fun `keeps marker when reset fails`() = runTest {
        val dataStore = InMemoryPreferencesDataStore(
            UserPreferencesRecoveryMarker.createReplacementPreferences(),
        )
        coEvery { localAppDataResetter.resetDatabaseAndOnboarding() } throws IllegalStateException("reset failed")
        val coordinator = StartupDataRecoveryCoordinator(dataStore, localAppDataResetter)

        assertThrows<IllegalStateException> {
            coordinator.recoverIfNeeded()
        }

        assertTrue(UserPreferencesRecoveryMarker.isRequired(dataStore.current()))
    }

    @Test
    fun `completes recovery on retry after previous reset failure`() = runTest {
        val dataStore = InMemoryPreferencesDataStore(
            UserPreferencesRecoveryMarker.createReplacementPreferences(),
        )
        coEvery {
            localAppDataResetter.resetDatabaseAndOnboarding()
        } throws IllegalStateException("reset failed") andThen Unit
        val coordinator = StartupDataRecoveryCoordinator(dataStore, localAppDataResetter)

        assertThrows<IllegalStateException> {
            coordinator.recoverIfNeeded()
        }
        assertTrue(UserPreferencesRecoveryMarker.isRequired(dataStore.current()))

        coordinator.recoverIfNeeded()

        coVerify(exactly = 2) { localAppDataResetter.resetDatabaseAndOnboarding() }
        assertFalse(UserPreferencesRecoveryMarker.isRequired(dataStore.current()))
    }
}
