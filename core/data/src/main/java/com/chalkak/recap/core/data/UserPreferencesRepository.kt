package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val organizeCompleteEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ORGANIZE_COMPLETE_ENABLED] ?: true
        }

    suspend fun setOrganizeCompleteEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ORGANIZE_COMPLETE_ENABLED] = enabled
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ORGANIZE_COMPLETE_ENABLED = booleanPreferencesKey("organize_complete_enabled")
    }
}
