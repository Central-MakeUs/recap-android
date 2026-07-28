package com.chalkak.recap.core.data.search

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.UserPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RecentSearchStore @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    val recentSearches: Flow<List<String>> =
        dataStore.data.map { preferences ->
            decode(preferences[RECENT_SEARCHES])
        }

    suspend fun remember(term: String) {
        val normalized = term.trim()
        if (normalized.isEmpty()) {
            return
        }
        dataStore.edit { preferences ->
            val current = decode(preferences[RECENT_SEARCHES])
            val updated = (listOf(normalized) + current.filterNot {
                it.equals(normalized, ignoreCase = true)
            }).take(MAX_RECENT_SEARCHES)
            preferences[RECENT_SEARCHES] = encode(updated)
        }
    }

    suspend fun remove(term: String) {
        val normalized = term.trim()
        if (normalized.isEmpty()) {
            return
        }
        dataStore.edit { preferences ->
            val current = decode(preferences[RECENT_SEARCHES])
            val updated = current.filterNot { it.equals(normalized, ignoreCase = true) }
            if (updated.isEmpty()) {
                preferences.remove(RECENT_SEARCHES)
            } else {
                preferences[RECENT_SEARCHES] = encode(updated)
            }
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(RECENT_SEARCHES)
        }
    }

    private companion object {
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        const val MAX_RECENT_SEARCHES = 10
        private const val SEPARATOR = "\u001E"

        fun encode(terms: List<String>): String = terms.joinToString(SEPARATOR)

        fun decode(raw: String?): List<String> {
            if (raw.isNullOrEmpty()) {
                return emptyList()
            }
            return raw.split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
