package com.chalkak.recap.core.data.account

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AccountOwnerStoreTest {
    private lateinit var dataStore: InMemoryPreferencesDataStore
    private lateinit var store: AccountOwnerStore

    @BeforeEach
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
        store = AccountOwnerStore(dataStore)
    }

    @Test
    fun `getHash returns null when empty`() = runTest {
        assertNull(store.getHash())
    }

    @Test
    fun `setHash persists value for getHash`() = runTest {
        store.setHash(HASH)

        assertEquals(HASH, store.getHash())
    }

    @Test
    fun `setHash rejects blank hash`() = runTest {
        assertThrows<IllegalArgumentException> {
            store.setHash(" ")
        }
    }

    @Test
    fun `getHash returns null for blank persisted hash`() = runTest {
        val blankStore = AccountOwnerStore(
            InMemoryPreferencesDataStore(
                mutablePreferencesOf(stringPreferencesKey("owner_hash") to " "),
            ),
        )

        assertNull(blankStore.getHash())
    }

    @Test
    fun `getOrCreateSalt returns the same salt on repeated calls`() = runTest {
        val first = store.getOrCreateSalt()
        val second = store.getOrCreateSalt()

        assertEquals(first, second)
        assertTrue(first.isNotBlank())
    }

    @Test
    fun `clear removes persisted hash and salt`() = runTest {
        store.setHash(HASH)
        val salt = store.getOrCreateSalt()

        store.clear()

        assertNull(store.getHash())
        assertNotEquals(salt, store.getOrCreateSalt())
    }

    private companion object {
        const val HASH = "0f1e2d3c4b5a"
    }
}
