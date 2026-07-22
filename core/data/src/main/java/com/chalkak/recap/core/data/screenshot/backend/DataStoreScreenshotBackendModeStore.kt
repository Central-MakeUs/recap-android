package com.chalkak.recap.core.data.screenshot.backend

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chalkak.recap.core.data.BuildConfig
import com.chalkak.recap.core.data.UserPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class DataStoreScreenshotBackendModeStore @Inject constructor(
    @param:UserPreferencesDataStore private val dataStore: DataStore<Preferences>,
) : ScreenshotBackendModeStore {
    @VisibleForTesting
    internal var isDebugBuild: Boolean = BuildConfig.DEBUG

    override val mode: Flow<ScreenshotBackendMode> =
        dataStore.data.map { preferences ->
            resolveEffectiveMode(readStoredValue(preferences))
        }

    override suspend fun currentMode(): ScreenshotBackendMode {
        return mode.first()
    }

    override suspend fun setMode(mode: ScreenshotBackendMode) {
        if (!isDebugBuild && mode == ScreenshotBackendMode.REMOTE) {
            return
        }
        dataStore.edit { preferences ->
            preferences[SCREENSHOT_BACKEND_MODE] = mode.name
            preferences.remove(LEGACY_ANALYSIS_DATA_SOURCE_MODE)
        }
    }

    private fun readStoredValue(preferences: Preferences): String? {
        return preferences[SCREENSHOT_BACKEND_MODE]
            ?: preferences[LEGACY_ANALYSIS_DATA_SOURCE_MODE]
    }

    private fun resolveEffectiveMode(storedValue: String?): ScreenshotBackendMode {
        if (!isDebugBuild) {
            return ScreenshotBackendMode.MOCK
        }
        return ScreenshotBackendMode.fromStoredValue(storedValue)
    }

    private companion object {
        val SCREENSHOT_BACKEND_MODE = stringPreferencesKey("screenshot_backend_mode")
        val LEGACY_ANALYSIS_DATA_SOURCE_MODE = stringPreferencesKey("analysis_data_source_mode")
    }
}
