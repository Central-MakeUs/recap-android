package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RecentSearchStoreTest {
    private lateinit var dataStore: InMemoryPreferencesDataStore
    private lateinit var store: RecentSearchStore

    @BeforeEach
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
        store = RecentSearchStore(dataStore)
    }

    @Test
    fun `remember trims and moves duplicate to front`() = runTest {
        store.remember(" 파스타 ")
        store.remember("숙소")
        store.remember("파스타")

        assertEquals(listOf("파스타", "숙소"), store.recentSearches.first())
    }

    @Test
    fun `remember ignores blank term`() = runTest {
        store.remember("   ")

        assertEquals(emptyList<String>(), store.recentSearches.first())
    }

    @Test
    fun `remember keeps at most ten terms`() = runTest {
        repeat(12) { index ->
            store.remember("term$index")
        }

        val terms = store.recentSearches.first()
        assertEquals(10, terms.size)
        assertEquals("term11", terms.first())
        assertEquals("term2", terms.last())
    }

    @Test
    fun `clearAll removes persisted terms`() = runTest {
        store.remember("숙소")
        store.clearAll()

        assertEquals(emptyList<String>(), store.recentSearches.first())
    }
}
