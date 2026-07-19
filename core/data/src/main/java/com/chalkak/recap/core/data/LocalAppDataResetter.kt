package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.network.SessionTokenStore
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
    private val sessionTokenStore: SessionTokenStore,
) {
    /**
     * 로그아웃/계정 초기화: 로컬 스크린샷 DB·이미지·세션·온보딩 진행 상태를 모두 지운다.
     * 호출 후 앱은 Onboarding Landing부터 다시 시작한다.
     */
    suspend fun resetDatabaseAndOnboarding() {
        withContext(Dispatchers.IO) {
            recapDatabase.clearAllTables()
            screenshotImageStorage.clearStoredImages()
        }
        sessionTokenStore.clear()
        userPreferencesRepository.clearOnboardingStep()
        userPreferencesRepository.setOnboardingCompleted(false)
    }
}
