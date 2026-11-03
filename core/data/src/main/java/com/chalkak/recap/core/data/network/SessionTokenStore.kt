package com.chalkak.recap.core.data.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.USER_PREFERENCES_DATASTORE_NAME
import com.chalkak.recap.core.data.UserPreferencesDataStore
import com.chalkak.recap.core.data.safeData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

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

    val refreshToken: Flow<String?> =
        dataStore.safeData(USER_PREFERENCES_DATASTORE_NAME)
            .map { preferences -> preferences[REFRESH_TOKEN] }
            .distinctUntilChanged()

    /** Stable across token refreshes, replaced by a new login and removed on logout. */
    val sessionId: Flow<String?> =
        dataStore.safeData(USER_PREFERENCES_DATASTORE_NAME)
            .map { preferences ->
                preferences[REFRESH_TOKEN]?.let { preferences[SESSION_ID] ?: it }
            }
            .distinctUntilChanged()

    @Volatile
    private var cached: SessionTokens? = null

    private var hydrated: Boolean = false

    suspend fun save(tokens: SessionTokens) = mutex.withLock {
        saveLocked(tokens, newSession = true)
    }

    suspend fun clear() = mutex.withLock {
        clearLocked()
    }

    /** A refresh response must not restore a session that was cleared or replaced while it ran. */
    suspend fun saveIfRefreshTokenMatches(expected: String, tokens: SessionTokens): Boolean =
        mutex.withLock {
            ensureHydratedLocked()
            if (cached?.refreshToken != expected) return@withLock false
            saveLocked(tokens, newSession = false)
            true
        }

    suspend fun clearIfRefreshTokenMatches(expected: String): Boolean = mutex.withLock {
        ensureHydratedLocked()
        if (cached?.refreshToken != expected) return@withLock false
        clearLocked()
        true
    }

    suspend fun getTokens(): SessionTokens? = mutex.withLock {
        ensureHydratedLocked()
        cached
    }

    suspend fun getAccessToken(): String? = getTokens()?.accessToken

    suspend fun getRefreshToken(): String? = getTokens()?.refreshToken

    fun peekAccessToken(): String? = peekTokens()?.accessToken

    fun peekTokens(): SessionTokens? {
        cached?.let { return it }
        return runBlocking { getTokens() }
    }

    private suspend fun saveLocked(tokens: SessionTokens, newSession: Boolean) {
        dataStore.edit { preferences ->
            // Preserve the pre-migration identity when refreshing an existing installation.
            preferences[SESSION_ID] = if (newSession) {
                UUID.randomUUID().toString()
            } else {
                preferences[SESSION_ID] ?: preferences[REFRESH_TOKEN] ?: UUID.randomUUID().toString()
            }
            preferences[ACCESS_TOKEN] = tokens.accessToken
            preferences[REFRESH_TOKEN] = tokens.refreshToken
            preferences[ACCESS_TOKEN_EXPIRES_AT] = tokens.accessTokenExpiresAt
        }
        cached = tokens
        hydrated = true
    }

    private suspend fun clearLocked() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(ACCESS_TOKEN_EXPIRES_AT)
            preferences.remove(SESSION_ID)
        }
        cached = null
        hydrated = true
    }

    private suspend fun ensureHydratedLocked() {
        if (hydrated) return
        val preferences = dataStore.safeData(USER_PREFERENCES_DATASTORE_NAME).first()
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

    private companion object {
        val SESSION_ID = stringPreferencesKey("session_id")
        val ACCESS_TOKEN = stringPreferencesKey("session_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("session_refresh_token")
        val ACCESS_TOKEN_EXPIRES_AT = stringPreferencesKey("session_access_token_expires_at")
    }
}
