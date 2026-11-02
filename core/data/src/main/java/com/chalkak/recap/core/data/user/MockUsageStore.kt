package com.chalkak.recap.core.data.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.chalkak.recap.core.data.USER_PREFERENCES_DATASTORE_NAME
import com.chalkak.recap.core.data.UserPreferencesDataStore
import com.chalkak.recap.core.data.safeData
import com.chalkak.recap.core.model.user.Usage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockUsageStore @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    fun observe(): Flow<Usage> =
        dataStore.safeData(USER_PREFERENCES_DATASTORE_NAME)
            .map { preferences -> usage(preferences[USED_COUNT] ?: 0) }
            .distinctUntilChanged()

    suspend fun current(): Usage = observe().first()

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
