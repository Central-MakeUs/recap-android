package com.chalkak.recap.core.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import timber.log.Timber

internal val Context.userPreferencesDataStore by preferencesDataStore(
    name = "user_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Timber.e(exception, "user_preferences DataStore corrupted; resetting to empty preferences")
        emptyPreferences()
    },
)
