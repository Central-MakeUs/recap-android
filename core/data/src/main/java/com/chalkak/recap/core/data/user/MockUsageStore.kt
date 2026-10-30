package com.chalkak.recap.core.data.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.chalkak.recap.core.data.USER_PREFERENCES_DATASTORE_NAME
import com.chalkak.recap.core.data.UserPreferencesDataStore
import com.chalkak.recap.core.data.safeData
import com.chalkak.recap.core.model.user.Usage
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockUsageStore @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    suspend fun current(): Usage {
        val preferences = dataStore.safeData(USER_PREFERENCES_DATASTORE_NAME).first()
        return usage(preferences[USED_COUNT] ?: 0)
    }

    suspend fun recordSuccessfulSheets(count: Int) {
        if (count <= 0) {
            return
        }
        dataStore.edit { preferences ->
            val usedCount = preferences[USED_COUNT] ?: 0
            preferences[USED_COUNT] = usedCount + count
        }
    }

    suspend fun reset() {
        dataStore.edit { preferences ->
            preferences[USED_COUNT] = 0
        }
    }

    private fun usage(usedCount: Int): Usage =
        Usage(
            usedCount = usedCount,
            limit = LIMIT,
            remaining = (LIMIT - usedCount).coerceAtLeast(0),
            resetAt = "",
        )

    private companion object {
        val USED_COUNT = intPreferencesKey("mock_usage_used_count")
        const val LIMIT = 20
    }
}
