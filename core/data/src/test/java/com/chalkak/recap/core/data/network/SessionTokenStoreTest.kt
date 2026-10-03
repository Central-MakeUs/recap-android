package com.chalkak.recap.core.data.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTokenStoreTest {
    @Test
    fun `refresh cannot restore a cleared session`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val store = SessionTokenStore(dataStore)
        store.save(tokens("old"))
        store.clear()

        assertFalse(store.saveIfRefreshTokenMatches("old-refresh", tokens("rotated")))
        assertNull(store.getTokens())
        assertNull(SessionTokenStore(dataStore).getTokens())
    }

    @Test
    fun `refresh cannot overwrite a different account session`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val store = SessionTokenStore(dataStore)
        store.save(tokens("new-account"))

        assertFalse(store.saveIfRefreshTokenMatches("old-refresh", tokens("rotated")))
        assertEquals(tokens("new-account"), store.getTokens())
        assertEquals(tokens("new-account"), SessionTokenStore(dataStore).getTokens())
    }

    @Test
    fun `invalid old refresh cannot clear a replacement session`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        val store = SessionTokenStore(dataStore)
        store.save(tokens("new-account"))

        assertFalse(store.clearIfRefreshTokenMatches("old-refresh"))
        assertEquals(tokens("new-account"), SessionTokenStore(dataStore).getTokens())
    }

    @Test
    fun `matching refresh rotates the persisted session`() = runTest {
        val dataStore = InMemoryPreferencesDataStore()
        SessionTokenStore(dataStore).save(tokens("old"))
        val store = SessionTokenStore(dataStore)

        assertTrue(store.saveIfRefreshTokenMatches("old-refresh", tokens("rotated")))
        assertEquals(tokens("rotated"), store.getTokens())
        assertEquals(tokens("rotated"), SessionTokenStore(dataStore).getTokens())
    }

    @Test
    fun `clear waits for an earlier save cache publication`() = runTest {
        val delegate = InMemoryPreferencesDataStore()
        val firstWriteCommitted = CompletableDeferred<Unit>()
        val releaseFirstWrite = CompletableDeferred<Unit>()
        var writeCount = 0
        val dataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = delegate.data

            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences,
            ): Preferences {
                val updated = delegate.updateData(transform)
                if (++writeCount == 1) {
                    firstWriteCommitted.complete(Unit)
                    releaseFirstWrite.await()
                }
                return updated
            }
        }
        val store = SessionTokenStore(dataStore)
        val save = async { store.save(tokens("old")) }
        firstWriteCommitted.await()
        val clear = async { store.clear() }
        runCurrent()

        assertFalse(clear.isCompleted)
        releaseFirstWrite.complete(Unit)
        save.await()
        clear.await()

        assertNull(store.getTokens())
        assertNull(SessionTokenStore(delegate).getTokens())
    }

    private fun tokens(prefix: String) = SessionTokens(
        accessToken = "$prefix-access",
        refreshToken = "$prefix-refresh",
        accessTokenExpiresAt = "2026-09-20T12:00:00Z",
    )
}
