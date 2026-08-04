package com.chalkak.recap.core.data

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
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
    fun `corrupted preferences file is replaced with empty preferences`() = runTest {
        val file = File(tempDir, "user_preferences.preferences_pb")
        file.writeText("this is not a valid preferences protobuf")

        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler {
                emptyPreferences()
            },
            scope = backgroundScope,
            produceFile = { file },
        )

        val preferences = dataStore.data.first()

        assertTrue(preferences.asMap().isEmpty())
        assertEquals(emptyPreferences(), preferences)
    }

    @Test
    fun `repository reads defaults after corruption recovery`() = runTest {
        val file = File(tempDir, "user_preferences_repo.preferences_pb")
        file.writeText("corrupted")

        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler {
                emptyPreferences()
            },
            scope = backgroundScope,
            produceFile = { file },
        )
        val repository = UserPreferencesRepository(dataStore)

        assertFalse(repository.onboardingCompleted.first())
        assertFalse(repository.organizeCompleteNotificationEnabled.first())
    }
}
