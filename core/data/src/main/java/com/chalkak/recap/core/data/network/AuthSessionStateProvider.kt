package com.chalkak.recap.core.data.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 앱 전역 세션 보유 여부.
 *
 * refresh token은 서버가 `INVALID_REFRESH_TOKEN`/`EXPIRED_REFRESH_TOKEN`으로 명시 거부할 때만
 * [TokenRefreshCoordinator]가 지우므로, 네트워크 실패나 5xx는 세션 상실로 판정되지 않는다.
 */
@Singleton
class AuthSessionStateProvider @Inject constructor(
    sessionTokenStore: SessionTokenStore,
) {
    val hasSession: Flow<Boolean> =
        sessionTokenStore.refreshToken
            .map { token -> !token.isNullOrBlank() }
            .distinctUntilChanged()
}
