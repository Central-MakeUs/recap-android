package com.chalkak.recap.core.data.network

import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.model.auth.AuthError
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class TokenRefreshCoordinator @Inject constructor(
    private val sessionTokenStore: SessionTokenStore,
    private val authRepositoryProvider: Provider<AuthRepository>,
    private val clock: Clock,
) {
    private val mutex = Mutex()

    /**
     * @return true if a usable access token is available after this call
     */
    suspend fun refreshIfNeeded(force: Boolean = false): Boolean =
        mutex.withLock {
            val tokens = sessionTokenStore.getTokens()
            if (tokens == null) return false

            if (!force && !needsRefresh(tokens.accessTokenExpiresAt)) {
                return true
            }

            val result = authRepositoryProvider.get().refresh()
            result.fold(
                onSuccess = { true },
                onFailure = { error ->
                    if (error.isInvalidOrExpiredRefresh()) {
                        sessionTokenStore.clear()
                    }
                    false
                },
            )
        }

    fun needsRefresh(accessTokenExpiresAt: String, now: Instant = clock.instant()): Boolean {
        val expiresAt = parseExpiresAt(accessTokenExpiresAt) ?: return true
        val refreshAt = expiresAt.minusSeconds(REFRESH_SKEW_SECONDS)
        return !now.isBefore(refreshAt)
    }

    private fun Throwable.isInvalidOrExpiredRefresh(): Boolean {
        val authError = (this as? AuthException)?.authError as? AuthError.Server ?: return false
        return authError.code in INVALID_REFRESH_TOKEN_CODES
    }

    companion object {
        const val REFRESH_SKEW_SECONDS = 60L

        val INVALID_REFRESH_TOKEN_CODES = setOf(
            "INVALID_REFRESH_TOKEN",
            "EXPIRED_REFRESH_TOKEN",
        )

        fun parseExpiresAt(value: String): Instant? =
            runCatching { Instant.parse(value) }.getOrNull()
                ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
    }
}
