package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.search.RecentSearchStore
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
    private val recentSearchStore: RecentSearchStore,
) {
    /**
     * 로그아웃/탈퇴 시작 신호.
     *
     * 세션이 사라지기 전에 온보딩 완료 플래그를 먼저 내려야, 루트 라우팅이 Reauth를 거치지 않고
     * 곧바로 Onboarding으로 간다.
     */
    suspend fun prepareSignOut() {
        userPreferencesRepository.setOnboardingCompleted(false)
    }

    /**
     * 로그아웃/계정 초기화: 로컬 스크린샷 DB·이미지·세션·온보딩 진행 상태를 모두 지운다.
     * 호출 후 앱은 Onboarding Landing부터 다시 시작한다.
     */
    suspend fun resetDatabaseAndOnboarding() {
        userPreferencesRepository.setOnboardingCompleted(false)
        withContext(Dispatchers.IO) {
            recapDatabase.clearAllTables()
            screenshotImageStorage.clearStoredImages()
        }
        sessionTokenStore.clear()
        recentSearchStore.clearAll()
    }
}
