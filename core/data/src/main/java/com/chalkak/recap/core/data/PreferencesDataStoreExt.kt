package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

internal fun DataStore<Preferences>.safeData(): Flow<Preferences> =
    data.catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "user_preferences DataStore read failed; emitting empty preferences")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
