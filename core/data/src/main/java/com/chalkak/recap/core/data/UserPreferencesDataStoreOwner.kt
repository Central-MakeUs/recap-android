package com.chalkak.recap.core.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.preferencesDataStore
import timber.log.Timber

internal const val USER_PREFERENCES_DATASTORE_NAME = "user_preferences"

internal val Context.userPreferencesDataStore by preferencesDataStore(
    name = USER_PREFERENCES_DATASTORE_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Timber.e(
            exception,
            "user_preferences DataStore corrupted; writing recovery marker",
        )
        UserPreferencesRecoveryMarker.createReplacementPreferences()
    },
)
