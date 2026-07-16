package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class LocalAppDataResetter @Inject constructor(
    private val recapDatabase: RecapDatabase,
    private val screenshotImageStorage: ScreenshotImageStorage,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    suspend fun resetDatabaseAndOnboarding() {
        withContext(Dispatchers.IO) {
            recapDatabase.clearAllTables()
            screenshotImageStorage.clearStoredImages()
        }
        userPreferencesRepository.setOnboardingCompleted(false)
    }
}
