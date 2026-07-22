package com.chalkak.recap.core.data.screenshot

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScreenshotBackendModeStoreTest {
    private lateinit var dataStore: InMemoryPreferencesDataStore
    private lateinit var store: DataStoreScreenshotBackendModeStore

    @BeforeEach
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
        store = DataStoreScreenshotBackendModeStore(dataStore).apply {
            isDebugBuild = true
        }
    }

    @Test
    fun `mode defaults to MOCK`() = runTest {
        assertEquals(ScreenshotBackendMode.MOCK, store.mode.first())
        assertEquals(ScreenshotBackendMode.MOCK, store.currentMode())
    }

    @Test
    fun `setMode persists REMOTE in debug`() = runTest {
        store.setMode(ScreenshotBackendMode.REMOTE)

        assertEquals(ScreenshotBackendMode.REMOTE, store.mode.first())
        assertEquals(ScreenshotBackendMode.REMOTE, store.currentMode())
    }

    @Test
    fun `unknown stored mode recovers to MOCK`() = runTest {
        dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(stringPreferencesKey("screenshot_backend_mode") to "UNKNOWN"),
        )
        store = DataStoreScreenshotBackendModeStore(dataStore).apply {
            isDebugBuild = true
        }

        assertEquals(ScreenshotBackendMode.MOCK, store.currentMode())
    }

    @Test
    fun `non debug effective mode stays MOCK even when REMOTE is stored`() = runTest {
        store.setMode(ScreenshotBackendMode.REMOTE)
        store.isDebugBuild = false

        assertEquals(ScreenshotBackendMode.MOCK, store.currentMode())
    }

    @Test
    fun `non debug rejects REMOTE mode writes`() = runTest {
        store.isDebugBuild = false
        store.setMode(ScreenshotBackendMode.REMOTE)

        store.isDebugBuild = true
        assertEquals(ScreenshotBackendMode.MOCK, store.currentMode())
    }

    @Test
    fun `legacy analysis key is used as fallback when new key is absent`() = runTest {
        dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(stringPreferencesKey("analysis_data_source_mode") to "REMOTE"),
        )
        store = DataStoreScreenshotBackendModeStore(dataStore).apply {
            isDebugBuild = true
        }

        assertEquals(ScreenshotBackendMode.REMOTE, store.currentMode())
    }

    @Test
    fun `new key takes priority over legacy key`() = runTest {
        dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(
                stringPreferencesKey("screenshot_backend_mode") to "MOCK",
                stringPreferencesKey("analysis_data_source_mode") to "REMOTE",
            ),
        )
        store = DataStoreScreenshotBackendModeStore(dataStore).apply {
            isDebugBuild = true
        }

        assertEquals(ScreenshotBackendMode.MOCK, store.currentMode())
    }

    @Test
    fun `setMode writes new key and removes legacy key`() = runTest {
        dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(stringPreferencesKey("analysis_data_source_mode") to "MOCK"),
        )
        store = DataStoreScreenshotBackendModeStore(dataStore).apply {
            isDebugBuild = true
        }

        store.setMode(ScreenshotBackendMode.REMOTE)

        val preferences = dataStore.current()
        assertEquals(
            "REMOTE",
            preferences[stringPreferencesKey("screenshot_backend_mode")],
        )
        assertNull(preferences[stringPreferencesKey("analysis_data_source_mode")])
    }
}
