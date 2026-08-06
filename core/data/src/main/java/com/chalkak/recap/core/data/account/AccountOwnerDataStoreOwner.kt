package com.chalkak.recap.core.data.account

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import timber.log.Timber

internal const val ACCOUNT_OWNER_DATASTORE_NAME = "account_owner"

internal val Context.accountOwnerDataStore by preferencesDataStore(
    name = ACCOUNT_OWNER_DATASTORE_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler { exception ->
        Timber.e(exception, "account_owner DataStore corrupted; clearing owner hash")
        emptyPreferences()
    },
)
