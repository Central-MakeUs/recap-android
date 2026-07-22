package com.chalkak.recap.core.data.network

import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthSignInResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Provider
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds

class TokenRefreshCoordinatorTest {
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
    private val authRepository = mockk<AuthRepository>()
    private val authRepositoryProvider = mockk<Provider<AuthRepository>>()
    private val fixedNow = Instant.parse("2026-07-20T12:00:00Z")
    private val clock = Clock.fixed(fixedNow, ZoneOffset.UTC)

    private lateinit var coordinator: TokenRefreshCoordinator

    @BeforeEach
    fun setUp() {
        every { authRepositoryProvider.get() } returns authRepository
        coordinator = TokenRefreshCoordinator(
            sessionTokenStore = sessionTokenStore,
            authRepositoryProvider = authRepositoryProvider,
            clock = clock,
        )
    }

    @Test
    fun `needsRefresh is true within skew window`() {
        val expiresAt = fixedNow.plusSeconds(30).toString()
        assertTrue(coordinator.needsRefresh(expiresAt))
    }

    @Test
    fun `needsRefresh is false when expiry is beyond skew`() {
        val expiresAt = fixedNow.plusSeconds(120).toString()
        assertFalse(coordinator.needsRefresh(expiresAt))
    }

    @Test
    fun `needsRefresh is true when already expired`() {
        val expiresAt = fixedNow.minusSeconds(1).toString()
        assertTrue(coordinator.needsRefresh(expiresAt))
    }

    @Test
    fun `refreshIfNeeded skips refresh when token has remaining lifetime`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns tokens(
            accessTokenExpiresAt = fixedNow.plusSeconds(120).toString(),
        )

        val result = coordinator.refreshIfNeeded(force = false)

        assertTrue(result)
        coVerify(exactly = 0) { authRepository.refresh() }
    }

    @Test
    fun `refreshIfNeeded refreshes when token is near expiry`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns tokens(
            accessTokenExpiresAt = fixedNow.plusSeconds(30).toString(),
        )
        coEvery { authRepository.refresh() } returns Result.success(
            AuthSignInResult.Success(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                accessTokenExpiresAt = fixedNow.plusSeconds(3600).toString(),
            ),
        )

        val result = coordinator.refreshIfNeeded(force = false)

        assertTrue(result)
        coVerify(exactly = 1) { authRepository.refresh() }
    }

    @Test
    fun `refreshIfNeeded force refreshes even when token is still valid`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns tokens(
            accessTokenExpiresAt = fixedNow.plusSeconds(120).toString(),
        )
        coEvery { authRepository.refresh() } returns Result.success(
            AuthSignInResult.Success(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                accessTokenExpiresAt = fixedNow.plusSeconds(3600).toString(),
            ),
        )

        val result = coordinator.refreshIfNeeded(force = true)

        assertTrue(result)
        coVerify(exactly = 1) { authRepository.refresh() }
    }

    @Test
    fun `refreshIfNeeded single-flight shares one refresh across concurrent callers`() = runTest {
        var stored = tokens(accessTokenExpiresAt = fixedNow.plusSeconds(30).toString())
        coEvery { sessionTokenStore.getTokens() } coAnswers { stored }
        coEvery { authRepository.refresh() } coAnswers {
            delay(50.milliseconds)
            stored = tokens(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                accessTokenExpiresAt = fixedNow.plusSeconds(3600).toString(),
            )
            Result.success(
                AuthSignInResult.Success(
                    accessToken = "new-access",
                    refreshToken = "new-refresh",
                    accessTokenExpiresAt = fixedNow.plusSeconds(3600).toString(),
                ),
            )
        }

        val first = async { coordinator.refreshIfNeeded(force = false) }
        val second = async { coordinator.refreshIfNeeded(force = false) }

        assertTrue(first.await())
        assertTrue(second.await())
        coVerify(exactly = 1) { authRepository.refresh() }
    }

    @Test
    fun `refreshIfNeeded clears tokens when refresh token is invalid`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns tokens(
            accessTokenExpiresAt = fixedNow.plusSeconds(30).toString(),
        )
        coEvery { authRepository.refresh() } returns Result.failure(
            AuthException(
                AuthError.Server(
                    code = "INVALID_REFRESH_TOKEN",
                    message = "invalid",
                ),
            ),
        )

        val result = coordinator.refreshIfNeeded(force = false)

        assertFalse(result)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `refreshIfNeeded clears tokens when refresh token is expired`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns tokens(
            accessTokenExpiresAt = fixedNow.plusSeconds(30).toString(),
        )
        coEvery { authRepository.refresh() } returns Result.failure(
            AuthException(
                AuthError.Server(
                    code = "EXPIRED_REFRESH_TOKEN",
                    message = "expired",
                ),
            ),
        )

        val result = coordinator.refreshIfNeeded(force = true)

        assertFalse(result)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `refreshIfNeeded does not clear tokens on network failure`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns tokens(
            accessTokenExpiresAt = fixedNow.plusSeconds(30).toString(),
        )
        coEvery { authRepository.refresh() } returns Result.failure(
            AuthException(AuthError.Network),
        )

        val result = coordinator.refreshIfNeeded(force = false)

        assertFalse(result)
        coVerify(exactly = 0) { sessionTokenStore.clear() }
    }

    @Test
    fun `refreshIfNeeded returns false when no tokens are stored`() = runTest {
        coEvery { sessionTokenStore.getTokens() } returns null

        val result = coordinator.refreshIfNeeded(force = true)

        assertFalse(result)
        coVerify(exactly = 0) { authRepository.refresh() }
    }

    @Test
    fun `parseExpiresAt accepts offset date time`() {
        val parsed = TokenRefreshCoordinator.parseExpiresAt("2026-07-20T12:00:00+09:00")
        assertEquals(Instant.parse("2026-07-20T03:00:00Z"), parsed)
    }

    private fun tokens(
        accessToken: String = "access",
        refreshToken: String = "refresh",
        accessTokenExpiresAt: String,
    ) = SessionTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpiresAt = accessTokenExpiresAt,
    )
}
