package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.user.remote.AccountInfoResponseDto
import com.chalkak.recap.core.data.user.remote.ConsentStatusResponseDto
import com.chalkak.recap.core.data.user.remote.DataSummaryResponseDto
import com.chalkak.recap.core.data.user.remote.UserApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoteUserRepositoryTest {
    private val userApi = mockk<UserApi>()
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private val changeNotifier = mockk<RemoteCaptureChangeNotifier>(relaxed = true)

    private lateinit var repository: RemoteUserRepository

    @BeforeEach
    fun setUp() {
        coEvery { screenshotCardRepository.deleteAllCards() } just runs
        every { thumbnailCache.clearAll() } just runs
        every { changeNotifier.notifyCaptureChanged() } just runs
        repository = RemoteUserRepository(
            userApi = userApi,
            sessionTokenStore = sessionTokenStore,
            screenshotCardRepository = screenshotCardRepository,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
        )
    }

    @Test
    fun `getAccountInfo maps success response`() = runTest {
        coEvery { userApi.getAccountInfo() } returns ApiResponseDto(
            success = true,
            data = AccountInfoResponseDto(
                platform = "KAKAO",
                createdAt = "2026-07-01T00:00:00Z",
            ),
        )

        val result = repository.getAccountInfo()

        assertEquals("KAKAO", result.getOrNull()?.platform)
        assertEquals("2026-07-01T00:00:00Z", result.getOrNull()?.createdAt)
    }

    @Test
    fun `getDataSummary maps captured count`() = runTest {
        coEvery { userApi.getDataSummary() } returns ApiResponseDto(
            success = true,
            data = DataSummaryResponseDto(capturedCount = 12L),
        )

        val result = repository.getDataSummary()

        assertEquals(12L, result.getOrNull()?.capturedCount)
    }

    @Test
    fun `withdraw clears session even when api fails`() = runTest {
        coEvery { userApi.withdraw() } throws RemoteApiException(code = "ERR", message = "fail")

        val result = repository.withdraw()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `withdraw clears session on success`() = runTest {
        coEvery { userApi.withdraw() } returns Unit

        val result = repository.withdraw()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `deleteAccountData clears local cache and does not clear session`() = runTest {
        coEvery { userApi.deleteAccountData() } returns Unit

        val result = repository.deleteAccountData()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionTokenStore.clear() }
        coVerify(exactly = 1) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 1) { thumbnailCache.clearAll() }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `deleteAccountData skips local cleanup when api fails`() = runTest {
        coEvery { userApi.deleteAccountData() } throws RemoteApiException(code = "ERR", message = "fail")

        val result = repository.deleteAccountData()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 0) { thumbnailCache.clearAll() }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `getConsentStatus maps consented fields`() = runTest {
        coEvery { userApi.getConsentStatus() } returns ApiResponseDto(
            success = true,
            data = ConsentStatusResponseDto(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )

        val result = repository.getConsentStatus()

        assertEquals(true, result.getOrNull()?.consented)
        assertEquals("2026-07-27T00:00:00Z", result.getOrNull()?.consentedAt)
    }

    @Test
    fun `giveConsent returns success`() = runTest {
        coEvery { userApi.giveConsent() } returns Unit

        val result = repository.giveConsent()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userApi.giveConsent() }
    }

    @Test
    fun `withdrawConsent returns success`() = runTest {
        coEvery { userApi.withdrawConsent() } returns Unit

        val result = repository.withdrawConsent()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userApi.withdrawConsent() }
    }
}
