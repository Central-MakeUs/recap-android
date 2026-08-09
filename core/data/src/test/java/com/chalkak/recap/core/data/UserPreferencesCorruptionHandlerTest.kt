package com.chalkak.recap.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class UserPreferencesCorruptionHandlerTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `corruption handler produceNewData returns recovery marker`() = runTest {
        val handler = ReplaceFileCorruptionHandler {
            UserPreferencesRecoveryMarker.createReplacementPreferences()
        }

        val preferences = handler.handleCorruption(CorruptionException("corrupted"))

        assertTrue(UserPreferencesRecoveryMarker.isRequired(preferences))
        assertEquals(true, preferences[UserPreferencesRecoveryMarker.KEY])
    }

    @Test
    fun `recovery marker round trips through DataStore file`() = runTest {
        val file = File(tempDir, "user_preferences_marker.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file },
        )

        dataStore.updateData { UserPreferencesRecoveryMarker.createReplacementPreferences() }

        val preferences = dataStore.data.first()
        assertTrue(UserPreferencesRecoveryMarker.isRequired(preferences))
    }

    @Test
    fun `repository reads defaults when only recovery marker is present`() = runTest {
        val file = File(tempDir, "user_preferences_repo.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file },
        )
        dataStore.updateData { UserPreferencesRecoveryMarker.createReplacementPreferences() }
        val repository = UserPreferencesRepository(dataStore)

        assertTrue(UserPreferencesRecoveryMarker.isRequired(dataStore.data.first()))
        assertFalse(repository.onboardingCompleted.first())
        assertFalse(repository.organizeCompleteNotificationEnabled.first())
    }
}
