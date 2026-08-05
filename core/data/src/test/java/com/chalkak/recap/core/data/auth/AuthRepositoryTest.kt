package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.data.LocalAppDataResetter
import com.chalkak.recap.core.data.account.AccountOwnerHasher
import com.chalkak.recap.core.data.account.AccountOwnerStore
import com.chalkak.recap.core.data.auth.remote.AuthApi
import com.chalkak.recap.core.data.auth.remote.AuthPlatformDto
import com.chalkak.recap.core.data.auth.remote.AuthTokenApiResponse
import com.chalkak.recap.core.data.auth.remote.AuthVoidApiResponse
import com.chalkak.recap.core.data.auth.remote.LogoutRequestDto
import com.chalkak.recap.core.data.auth.remote.OAuthLoginRequestDto
import com.chalkak.recap.core.data.auth.remote.TokenRefreshRequestDto
import com.chalkak.recap.core.data.auth.remote.TokenResponseDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.network.SessionTokens
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthProvider
import com.chalkak.recap.core.model.auth.AuthSignInResult
import com.chalkak.recap.core.model.auth.KakaoUserProfile
import com.chalkak.recap.core.model.auth.SocialAuthCredential
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response

class AuthRepositoryTest {
    private val context = mockk<Context>(relaxed = true)
    private val kakaoLoginClient = mockk<KakaoLoginClient>()
    private val authApi = mockk<AuthApi>()
    private val deviceIdProvider = mockk<DeviceIdProvider>()
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
    private val accountOwnerStore = mockk<AccountOwnerStore>(relaxed = true)
    private val accountOwnerHasher = mockk<AccountOwnerHasher>()
    private val localAppDataResetter = mockk<LocalAppDataResetter>(relaxed = true)

    private lateinit var repository: AuthRepository

    @BeforeEach
    fun setUp() {
        repository = AuthRepository(
            kakaoLoginClient = kakaoLoginClient,
            authApi = authApi,
            deviceIdProvider = deviceIdProvider,
            sessionTokenStore = sessionTokenStore,
            accountOwnerStore = accountOwnerStore,
            accountOwnerHasher = accountOwnerHasher,
            localAppDataResetter = localAppDataResetter,
            crashReporter = com.chalkak.recap.core.model.observability.CrashReporter.NoOp,
        )
        coEvery { accountOwnerHasher.hashKakaoUserId(any()) } coAnswers {
            ownerHashOf(firstArg())
        }
        stubMatchingAccountOwner(KAKAO_USER_ID)
    }

    @Test
    fun `signInWithKakao sends deviceId and kakao access token to auth api`() = runTest {
        val requestSlot = slot<OAuthLoginRequestDto>()
        coEvery { kakaoLoginClient.login(context) } returns Result.success(
            SocialAuthCredential(
                provider = AuthProvider.Kakao,
                accessToken = "kakao-access-token",
            ),
        )
        coEvery { deviceIdProvider.getOrCreate() } returns "device-uuid-1"
        coEvery {
            authApi.login(provider = "kakao", body = capture(requestSlot))
        } returns AuthTokenApiResponse(
            success = true,
            data = TokenResponseDto(
                accessToken = "app-access",
                refreshToken = "app-refresh",
                accessTokenExpiresAt = "2026-07-10T13:00:00Z",
            ),
        )

        val result = repository.signInWithKakao(context)

        assertEquals(
            AuthSignInResult.Success(
                accessToken = "app-access",
                refreshToken = "app-refresh",
                accessTokenExpiresAt = "2026-07-10T13:00:00Z",
            ),
            result.getOrNull(),
        )
        assertEquals("device-uuid-1", requestSlot.captured.deviceId)
        assertEquals("kakao-access-token", requestSlot.captured.providerToken)
        assertEquals(AuthPlatformDto.ANDROID, requestSlot.captured.platform)
        coVerify(exactly = 1) { deviceIdProvider.getOrCreate() }
        coVerify(exactly = 0) { localAppDataResetter.wipeAndRebindOwner(any()) }
        coVerify(exactly = 1) {
            sessionTokenStore.save(
                SessionTokens(
                    accessToken = "app-access",
                    refreshToken = "app-refresh",
                    accessTokenExpiresAt = "2026-07-10T13:00:00Z",
                ),
            )
        }
    }

    @Test
    fun `signInWithKakao wipes and stores hash when owner hash is missing`() = runTest {
        coEvery { accountOwnerStore.getHash() } returns null
        stubSuccessfulServerLogin()

        val result = repository.signInWithKakao(context)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localAppDataResetter.wipeAndRebindOwner(ownerHashOf(KAKAO_USER_ID))
        }
        coVerify(exactly = 1) { authApi.login(any(), any()) }
    }

    @Test
    fun `signInWithKakao wipes and stores hash when owner hash differs`() = runTest {
        coEvery { accountOwnerStore.getHash() } returns ownerHashOf(999L)
        stubSuccessfulServerLogin()

        val result = repository.signInWithKakao(context)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            localAppDataResetter.wipeAndRebindOwner(ownerHashOf(KAKAO_USER_ID))
        }
    }

    @Test
    fun `signInWithKakao wipes before calling the auth api`() = runTest {
        coEvery { accountOwnerStore.getHash() } returns ownerHashOf(999L)
        stubSuccessfulServerLogin()

        repository.signInWithKakao(context)

        coVerifyOrder {
            localAppDataResetter.wipeAndRebindOwner(ownerHashOf(KAKAO_USER_ID))
            authApi.login(any(), any())
        }
    }

    @Test
    fun `signInWithKakao keeps wiped state when server login fails after account switch`() =
        runTest {
            coEvery { accountOwnerStore.getHash() } returns ownerHashOf(999L)
            stubSuccessfulServerLogin()
            coEvery { authApi.login(any(), any()) } throws IOException("offline")

            val result = repository.signInWithKakao(context)

            assertTrue(result.isFailure)
            coVerify(exactly = 1) {
                localAppDataResetter.wipeAndRebindOwner(ownerHashOf(KAKAO_USER_ID))
            }
        }

    @Test
    fun `signInWithKakao keeps local data when owner hash matches`() = runTest {
        stubSuccessfulServerLogin()

        val result = repository.signInWithKakao(context)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { localAppDataResetter.wipeAndRebindOwner(any()) }
        coVerify(exactly = 1) { authApi.login(any(), any()) }
    }

    @Test
    fun `signInWithKakao fails without wipe or server login when me fails`() = runTest {
        stubKakaoLoginSuccess()
        coEvery { kakaoLoginClient.fetchUserProfile() } returns Result.failure(
            AuthException(AuthError.ProviderUnavailable),
        )

        val result = repository.signInWithKakao(context)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { localAppDataResetter.wipeAndRebindOwner(any()) }
        coVerify(exactly = 0) { authApi.login(any(), any()) }
    }

    @Test
    fun `signInWithKakao fails without server login when owner hash read fails`() = runTest {
        stubKakaoLoginSuccess()
        coEvery { accountOwnerStore.getHash() } throws IOException("datastore unavailable")

        val result = repository.signInWithKakao(context)

        assertEquals(
            AuthError.Unknown,
            (result.exceptionOrNull() as AuthException).authError,
        )
        coVerify(exactly = 0) { localAppDataResetter.wipeAndRebindOwner(any()) }
        coVerify(exactly = 0) { authApi.login(any(), any()) }
    }

    @Test
    fun `signInWithKakao fails without server login when wipe fails`() = runTest {
        stubKakaoLoginSuccess()
        coEvery { accountOwnerStore.getHash() } returns null
        coEvery { localAppDataResetter.wipeAndRebindOwner(any()) } throws IOException("wipe failed")

        val result = repository.signInWithKakao(context)

        assertEquals(
            AuthError.Unknown,
            (result.exceptionOrNull() as AuthException).authError,
        )
        coVerify(exactly = 0) { authApi.login(any(), any()) }
    }

    @Test
    fun `signInWithKakao maps server error response`() = runTest {
        coEvery { kakaoLoginClient.login(context) } returns Result.success(
            SocialAuthCredential(
                provider = AuthProvider.Kakao,
                accessToken = "kakao-access-token",
            ),
        )
        coEvery { deviceIdProvider.getOrCreate() } returns "device-uuid-1"
        coEvery { authApi.login(any(), any()) } returns AuthTokenApiResponse(
            success = false,
            data = null,
            error = ApiErrorDto(
                code = "OAUTH_VERIFICATION_FAILED",
                message = "소셜 로그인 인증에 실패했습니다",
            ),
        )

        val result = repository.signInWithKakao(context)

        val exception = result.exceptionOrNull() as AuthException
        assertEquals(
            AuthError.Server(
                code = "OAUTH_VERIFICATION_FAILED",
                message = "소셜 로그인 인증에 실패했습니다",
            ),
            exception.authError,
        )
    }

    @Test
    fun `signInWithKakao maps network failure`() = runTest {
        coEvery { kakaoLoginClient.login(context) } returns Result.success(
            SocialAuthCredential(
                provider = AuthProvider.Kakao,
                accessToken = "kakao-access-token",
            ),
        )
        coEvery { deviceIdProvider.getOrCreate() } returns "device-uuid-1"
        coEvery { authApi.login(any(), any()) } throws IOException("offline")

        val result = repository.signInWithKakao(context)

        val exception = result.exceptionOrNull() as AuthException
        assertEquals(AuthError.Network, exception.authError)
    }

    @Test
    fun `signInWithKakao propagates kakao login failure without calling api`() = runTest {
        coEvery { kakaoLoginClient.login(context) } returns Result.failure(
            AuthException(AuthError.Cancelled),
        )

        val result = repository.signInWithKakao(context)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { kakaoLoginClient.fetchUserProfile() }
        coVerify(exactly = 0) { deviceIdProvider.getOrCreate() }
        coVerify(exactly = 0) { authApi.login(any(), any()) }
    }

    @Test
    fun `refresh stores new tokens from auth api`() = runTest {
        coEvery { sessionTokenStore.getRefreshToken() } returns "old-refresh"
        coEvery {
            authApi.refresh(TokenRefreshRequestDto(refreshToken = "old-refresh"))
        } returns AuthTokenApiResponse(
            success = true,
            data = TokenResponseDto(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                accessTokenExpiresAt = "2026-07-11T13:00:00Z",
            ),
        )

        val result = repository.refresh()

        assertEquals(
            AuthSignInResult.Success(
                accessToken = "new-access",
                refreshToken = "new-refresh",
                accessTokenExpiresAt = "2026-07-11T13:00:00Z",
            ),
            result.getOrNull(),
        )
        coVerify(exactly = 1) {
            sessionTokenStore.save(
                SessionTokens(
                    accessToken = "new-access",
                    refreshToken = "new-refresh",
                    accessTokenExpiresAt = "2026-07-11T13:00:00Z",
                ),
            )
        }
    }

    @Test
    fun `signInWithKakao maps HttpException error body to AuthError Server`() = runTest {
        coEvery { kakaoLoginClient.login(context) } returns Result.success(
            SocialAuthCredential(
                provider = AuthProvider.Kakao,
                accessToken = "kakao-access-token",
            ),
        )
        coEvery { deviceIdProvider.getOrCreate() } returns "device-uuid-1"
        val body =
            """
            {"success":false,"error":{"code":"OAUTH_VERIFICATION_FAILED","message":"invalid token"}}
            """.trimIndent().toResponseBody("application/json".toMediaType())
        coEvery { authApi.login(any(), any()) } throws HttpException(Response.error<Unit>(401, body))

        val result = repository.signInWithKakao(context)

        val exception = result.exceptionOrNull() as AuthException
        assertEquals(
            AuthError.Server(
                code = "OAUTH_VERIFICATION_FAILED",
                message = "invalid token",
            ),
            exception.authError,
        )
    }

    @Test
    fun `refresh maps HttpException error body to AuthError Server`() = runTest {
        coEvery { sessionTokenStore.getRefreshToken() } returns "old-refresh"
        val body =
            """
            {"success":false,"error":{"code":"INVALID_REFRESH_TOKEN","message":"expired"}}
            """.trimIndent().toResponseBody("application/json".toMediaType())
        coEvery { authApi.refresh(any()) } throws HttpException(Response.error<Unit>(401, body))

        val result = repository.refresh()

        val exception = result.exceptionOrNull() as AuthException
        assertEquals(
            AuthError.Server(
                code = "INVALID_REFRESH_TOKEN",
                message = "expired",
            ),
            exception.authError,
        )
    }

    @Test
    fun `refresh propagates CancellationException`() {
        coEvery { sessionTokenStore.getRefreshToken() } returns "old-refresh"
        coEvery { authApi.refresh(any()) } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.refresh()
            }
        }
    }

    @Test
    fun `logout clears session tokens after success`() = runTest {
        coEvery { sessionTokenStore.getRefreshToken() } returns "refresh-token"
        coEvery {
            authApi.logout(LogoutRequestDto(refreshToken = "refresh-token"))
        } returns AuthVoidApiResponse(success = true, data = null)

        val result = repository.logout()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `logout clears session tokens even when server fails`() = runTest {
        coEvery { sessionTokenStore.getRefreshToken() } returns "refresh-token"
        coEvery { authApi.logout(any()) } throws IOException("offline")

        val result = repository.logout()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    private fun stubMatchingAccountOwner(userId: Long) {
        coEvery { kakaoLoginClient.fetchUserProfile() } returns Result.success(
            KakaoUserProfile(
                id = userId,
                email = null,
                connectedAt = null,
            ),
        )
        coEvery { accountOwnerStore.getHash() } returns ownerHashOf(userId)
    }

    private fun stubKakaoLoginSuccess() {
        coEvery { kakaoLoginClient.login(context) } returns Result.success(
            SocialAuthCredential(
                provider = AuthProvider.Kakao,
                accessToken = "kakao-access-token",
            ),
        )
    }

    private fun stubSuccessfulServerLogin() {
        stubKakaoLoginSuccess()
        coEvery { deviceIdProvider.getOrCreate() } returns "device-uuid-1"
        coEvery { authApi.login(any(), any()) } returns AuthTokenApiResponse(
            success = true,
            data = TokenResponseDto(
                accessToken = "app-access",
                refreshToken = "app-refresh",
                accessTokenExpiresAt = "2026-07-10T13:00:00Z",
            ),
        )
    }

    private companion object {
        const val KAKAO_USER_ID = 4991360438L

        fun ownerHashOf(userId: Long): String = "owner-hash-$userId"
    }
}
