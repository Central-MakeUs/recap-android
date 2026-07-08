package com.chalkak.recap.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UserPreferencesRepositoryTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var repository: UserPreferencesRepository

    @BeforeEach
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempDir, "user_preferences.preferences_pb") },
        )
        repository = UserPreferencesRepository(dataStore)
    }

    @Test
    fun `onboardingCompleted defaults to false`() = runTest {
        assertFalse(repository.onboardingCompleted.first())
    }

    @Test
    fun `setOnboardingCompleted updates onboardingCompleted flow`() = runTest {
        repository.setOnboardingCompleted(true)

        assertTrue(repository.onboardingCompleted.first())
    }
}
