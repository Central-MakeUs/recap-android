package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import app.cash.turbine.test
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PreferencesDataStoreExtTest {
    @Test
    fun `safeData emits preferences on immediate success`() = runTest {
        val expected = mutablePreferencesOf()
        val dataStore = SequencePreferencesDataStore(listOf { expected })

        dataStore.safeData().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `safeData succeeds after one IOException`() = runTest {
        val expected = emptyPreferences()
        val dataStore = SequencePreferencesDataStore(
            listOf(
                { throw IOException("temp 1") },
                { expected },
            ),
        )

        dataStore.safeData().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `safeData succeeds after two IOExceptions`() = runTest {
        val expected = emptyPreferences()
        val dataStore = SequencePreferencesDataStore(
            listOf(
                { throw IOException("temp 1") },
                { throw IOException("temp 2") },
                { expected },
            ),
        )

        dataStore.safeData().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `safeData throws after three IOExceptions`() = runTest {
        val dataStore = SequencePreferencesDataStore(
            listOf(
                { throw IOException("fail 1") },
                { throw IOException("fail 2") },
                { throw IOException("fail 3") },
            ),
        )

        val error = assertThrows<IOException> {
            dataStore.safeData().first()
        }
        assertEquals("fail 3", error.message)
    }

    @Test
    fun `safeData rethrows non-IOException immediately`() = runTest {
        val dataStore = SequencePreferencesDataStore(
            listOf { throw IllegalStateException("boom") },
        )

        val error = assertThrows<IllegalStateException> {
            dataStore.safeData().first()
        }
        assertEquals("boom", error.message)
    }

    private class SequencePreferencesDataStore(
        private val attempts: List<() -> Preferences>,
    ) : DataStore<Preferences> {
        private var index = 0

        override val data: Flow<Preferences> = flow {
            val attemptIndex = index
            index++
            val producer = attempts.getOrElse(attemptIndex) {
                error("unexpected extra read attempt $attemptIndex")
            }
            emit(producer())
        }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = error("updateData is not used in this test")
    }
}
