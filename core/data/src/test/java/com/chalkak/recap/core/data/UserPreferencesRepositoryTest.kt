package com.chalkak.recap.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.screenshot.AnalysisDataSourceMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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
        repository = UserPreferencesRepository(dataStore).apply {
            isDebugBuild = true
        }
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

    @Test
    fun `analysisDataSourceMode defaults to MOCK`() = runTest {
        assertEquals(AnalysisDataSourceMode.MOCK, repository.analysisDataSourceMode.first())
        assertEquals(AnalysisDataSourceMode.MOCK, repository.getAnalysisDataSourceMode())
    }

    @Test
    fun `setAnalysisDataSourceMode persists REMOTE in debug`() = runTest {
        repository.setAnalysisDataSourceMode(AnalysisDataSourceMode.REMOTE)

        assertEquals(AnalysisDataSourceMode.REMOTE, repository.analysisDataSourceMode.first())
        assertEquals(AnalysisDataSourceMode.REMOTE, repository.getAnalysisDataSourceMode())
    }

    @Test
    fun `unknown stored analysis mode recovers to MOCK`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempDir, "unknown_mode.preferences_pb") },
        )
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("analysis_data_source_mode")] = "UNKNOWN"
        }
        val unknownModeRepository = UserPreferencesRepository(dataStore).apply {
            isDebugBuild = true
        }

        assertEquals(AnalysisDataSourceMode.MOCK, unknownModeRepository.getAnalysisDataSourceMode())
    }

    @Test
    fun `non debug effective mode stays MOCK even when REMOTE is stored`() = runTest {
        repository.setAnalysisDataSourceMode(AnalysisDataSourceMode.REMOTE)
        repository.isDebugBuild = false

        assertEquals(AnalysisDataSourceMode.MOCK, repository.getAnalysisDataSourceMode())
    }

    @Test
    fun `non debug rejects REMOTE mode writes`() = runTest {
        repository.isDebugBuild = false
        repository.setAnalysisDataSourceMode(AnalysisDataSourceMode.REMOTE)

        repository.isDebugBuild = true
        assertEquals(AnalysisDataSourceMode.MOCK, repository.getAnalysisDataSourceMode())
    }
}
