package com.chalkak.recap.core.data.auth

import android.content.Context
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
import com.chalkak.recap.core.model.auth.SocialAuthCredential
import io.mockk.coEvery
import io.mockk.coVerify
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

    private lateinit var repository: AuthRepository

    @BeforeEach
    fun setUp() {
        repository = AuthRepository(
            kakaoLoginClient = kakaoLoginClient,
            authApi = authApi,
            deviceIdProvider = deviceIdProvider,
            sessionTokenStore = sessionTokenStore,
        )
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
}
