package com.chalkak.recap.core.data.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.UserPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: String,
)

@Singleton
class SessionTokenStore @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()

    @Volatile
    private var cached: SessionTokens? = null

    @Volatile
    private var hydrated: Boolean = false

    suspend fun save(tokens: SessionTokens) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = tokens.accessToken
            preferences[REFRESH_TOKEN] = tokens.refreshToken
            preferences[ACCESS_TOKEN_EXPIRES_AT] = tokens.accessTokenExpiresAt
        }
        cached = tokens
        hydrated = true
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(ACCESS_TOKEN_EXPIRES_AT)
        }
        cached = null
        hydrated = true
    }

    suspend fun getTokens(): SessionTokens? {
        ensureHydrated()
        return cached
    }

    suspend fun getAccessToken(): String? = getTokens()?.accessToken

    suspend fun getRefreshToken(): String? = getTokens()?.refreshToken

    fun peekAccessToken(): String? {
        cached?.accessToken?.let { return it }
        return runBlocking { getAccessToken() }
    }

    private suspend fun ensureHydrated() {
        if (hydrated) return
        mutex.withLock {
            if (hydrated) return
            val preferences = dataStore.data.first()
            val accessToken = preferences[ACCESS_TOKEN]
            val refreshToken = preferences[REFRESH_TOKEN]
            val expiresAt = preferences[ACCESS_TOKEN_EXPIRES_AT]
            cached =
                if (accessToken != null && refreshToken != null && expiresAt != null) {
                    SessionTokens(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        accessTokenExpiresAt = expiresAt,
                    )
                } else {
                    null
                }
            hydrated = true
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("session_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("session_refresh_token")
        val ACCESS_TOKEN_EXPIRES_AT = stringPreferencesKey("session_access_token_expires_at")
    }
}
