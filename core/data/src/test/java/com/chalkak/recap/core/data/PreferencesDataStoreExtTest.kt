package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PreferencesDataStoreExtTest {
    @Test
    fun `safeData emits empty preferences when data throws IOException`() = runTest {
        val dataStore = FailingPreferencesDataStore(IOException("disk failed"))

        dataStore.safeData().test {
            assertEquals(emptyPreferences(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `safeData rethrows non-IOException`() = runTest {
        val dataStore = FailingPreferencesDataStore(IllegalStateException("boom"))

        val error = assertThrows<IllegalStateException> {
            dataStore.safeData().first()
        }
        assertEquals("boom", error.message)
    }

    @Test
    fun `UserPreferencesRepository onboardingCompleted falls back to false on IOException`() = runTest {
        val repository = UserPreferencesRepository(FailingPreferencesDataStore(IOException("disk failed")))

        assertFalse(repository.onboardingCompleted.first())
    }

    private class FailingPreferencesDataStore(
        private val error: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences = error("updateData is not used in this test")
    }
}
