package com.chalkak.recap.core.data.auth

import android.content.Context
import com.chalkak.recap.core.data.auth.remote.ApiErrorDto
import com.chalkak.recap.core.data.auth.remote.AuthApi
import com.chalkak.recap.core.data.auth.remote.AuthPlatformDto
import com.chalkak.recap.core.data.auth.remote.AuthTokenApiResponse
import com.chalkak.recap.core.data.auth.remote.OAuthLoginRequestDto
import com.chalkak.recap.core.data.auth.remote.TokenResponseDto
import com.chalkak.recap.core.model.auth.AuthError
import com.chalkak.recap.core.model.auth.AuthProvider
import com.chalkak.recap.core.model.auth.AuthSignInResult
import com.chalkak.recap.core.model.auth.SocialAuthCredential
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthRepositoryTest {
    private val context = mockk<Context>(relaxed = true)
    private val kakaoLoginClient = mockk<KakaoLoginClient>()
    private val authApi = mockk<AuthApi>()
    private val deviceIdProvider = mockk<DeviceIdProvider>()

    private lateinit var repository: AuthRepository

    @BeforeEach
    fun setUp() {
        repository = AuthRepository(
            kakaoLoginClient = kakaoLoginClient,
            authApi = authApi,
            deviceIdProvider = deviceIdProvider,
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
}
