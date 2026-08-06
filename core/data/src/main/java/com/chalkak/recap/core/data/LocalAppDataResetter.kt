package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.account.AccountOwnerStore
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.search.RecentSearchStore
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class LocalAppDataResetter @Inject constructor(
    private val recapDatabase: RecapDatabase,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionTokenStore: SessionTokenStore,
    private val recentSearchStore: RecentSearchStore,
    private val accountOwnerStore: AccountOwnerStore,
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
     * 계정 전환: 계정 종속 로컬 데이터를 지운 뒤에만 새 소유자 해시를 확정한다.
     * 온보딩 완료 플래그·세션·deviceId는 유지한다.
     *
     * wipe가 완전히 끝나지 않으면 예외를 던지고 해시를 갱신하지 않는다.
     * 이전 해시가 남으므로 다음 로그인에서 wipe를 다시 시도한다.
     */
    suspend fun wipeAndRebindOwner(ownerHash: String) {
        val imagesCleared = withContext(Dispatchers.IO) {
            recapDatabase.clearAllTables()
            thumbnailCache.clearAll()
        }
        recentSearchStore.clearAll()
        userPreferencesRepository.clearAccountScopedPreferences()
        if (!imagesCleared) {
            throw IOException("Failed to clear stored screenshot images during account switch")
        }
        accountOwnerStore.setHash(ownerHash)
    }

    /**
     * 로그아웃/계정 초기화: 로컬 스크린샷 DB·이미지·세션·온보딩 진행 상태를 모두 지운다.
     * 호출 후 앱은 Onboarding Landing부터 다시 시작한다.
     *
     * 계정 전환과 달리 세션 정리를 끝까지 마쳐야 하므로 이미지 삭제 실패는 로그만 남기고 진행한다.
     */
    suspend fun resetDatabaseAndOnboarding() {
        userPreferencesRepository.setOnboardingCompleted(false)
        val imagesCleared = withContext(Dispatchers.IO) {
            recapDatabase.clearAllTables()
            thumbnailCache.clearAll()
        }
        if (!imagesCleared) {
            Timber.w("Failed to clear stored screenshot images during sign-out reset")
        }
        sessionTokenStore.clear()
        recentSearchStore.clearAll()
        userPreferencesRepository.clearAccountScopedPreferences()
        accountOwnerStore.clear()
    }
}
