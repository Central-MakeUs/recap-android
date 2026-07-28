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

    val organizeCompleteNotificationEnabled: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[ORGANIZE_COMPLETE_NOTIFICATION_ENABLED]
                ?: preferences[LEGACY_ORGANIZE_COMPLETE_ENABLED]
                ?: false
        }

    suspend fun setOrganizeCompleteNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ORGANIZE_COMPLETE_NOTIFICATION_ENABLED] = enabled
            preferences.remove(LEGACY_ORGANIZE_COMPLETE_ENABLED)
        }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ORGANIZE_COMPLETE_NOTIFICATION_ENABLED =
            booleanPreferencesKey("organize_complete_notification_enabled")
        val LEGACY_ORGANIZE_COMPLETE_ENABLED =
            booleanPreferencesKey("organize_complete_enabled")
    }
}
