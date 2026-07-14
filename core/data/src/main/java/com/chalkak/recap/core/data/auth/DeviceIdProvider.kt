package com.chalkak.recap.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.UserPreferencesDataStore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class DeviceIdProvider @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()

    suspend fun getOrCreate(): String =
        mutex.withLock {
            val existing = dataStore.data.first()[DEVICE_ID]
            if (!existing.isNullOrBlank()) {
                return@withLock existing
            }

            val created = UUID.randomUUID().toString()
            dataStore.edit { preferences ->
                if (preferences[DEVICE_ID].isNullOrBlank()) {
                    preferences[DEVICE_ID] = created
                }
            }
            dataStore.data.first()[DEVICE_ID]
                ?: error("device_id was not persisted")
        }

    private companion object {
        val DEVICE_ID = stringPreferencesKey("device_id")
    }
}
