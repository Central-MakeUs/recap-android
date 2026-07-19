package com.chalkak.recap.core.data

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.screenshot.AnalysisDataSourceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    @VisibleForTesting
    internal var isDebugBuild: Boolean = BuildConfig.DEBUG

    val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    val analysisDataSourceMode: Flow<AnalysisDataSourceMode> =
        dataStore.data.map { preferences ->
            resolveEffectiveMode(preferences[ANALYSIS_DATA_SOURCE_MODE])
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun getAnalysisDataSourceMode(): AnalysisDataSourceMode {
        return analysisDataSourceMode.first()
    }

    suspend fun setAnalysisDataSourceMode(mode: AnalysisDataSourceMode) {
        if (!isDebugBuild && mode == AnalysisDataSourceMode.REMOTE) {
            return
        }
        dataStore.edit { preferences ->
            preferences[ANALYSIS_DATA_SOURCE_MODE] = mode.name
        }
    }

    private fun resolveEffectiveMode(storedValue: String?): AnalysisDataSourceMode {
        if (!isDebugBuild) {
            return AnalysisDataSourceMode.MOCK
        }
        return AnalysisDataSourceMode.fromStoredValue(storedValue)
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ANALYSIS_DATA_SOURCE_MODE = stringPreferencesKey("analysis_data_source_mode")
    }
}
