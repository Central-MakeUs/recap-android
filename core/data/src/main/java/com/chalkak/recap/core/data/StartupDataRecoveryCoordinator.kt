package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Runs once at app startup before navigation/prefetch.
 * Only clears local DB/images/session when the corruption recovery marker is present.
 */
@Singleton
class StartupDataRecoveryCoordinator @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
    private val localAppDataResetter: LocalAppDataResetter,
) {
    suspend fun recoverIfNeeded() {
        val preferences = dataStore.safeData(USER_PREFERENCES_DATASTORE_NAME).first()
        if (!UserPreferencesRecoveryMarker.isRequired(preferences)) {
            return
        }

        Timber.w("user_preferences recovery marker present; resetting local app data")
        localAppDataResetter.resetDatabaseAndOnboarding()
        dataStore.edit { mutablePreferences ->
            mutablePreferences.remove(UserPreferencesRecoveryMarker.KEY)
        }
    }
}
