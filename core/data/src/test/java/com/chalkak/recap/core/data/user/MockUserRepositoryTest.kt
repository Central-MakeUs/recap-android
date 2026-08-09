package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.screenshot.backend.MockScreenshotDataResetter
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MockUserRepositoryTest {
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val mockScreenshotDataResetter = mockk<MockScreenshotDataResetter>()
    private val changeNotifier = mockk<RemoteCaptureChangeNotifier>(relaxed = true)
    private val remoteAuthRepository = mockk<RemoteUserRepository>()

    private lateinit var repository: MockUserRepository

    @BeforeEach
    fun setUp() {
        coEvery { mockScreenshotDataResetter.reset() } just runs
        every { changeNotifier.notifyCaptureChanged() } just runs
        repository = MockUserRepository(
            screenshotCardRepository = screenshotCardRepository,
            userPreferencesRepository = userPreferencesRepository,
            mockScreenshotDataResetter = mockScreenshotDataResetter,
            changeNotifier = changeNotifier,
            remoteAuthRepository = remoteAuthRepository,
        )
    }

    @Test
    fun `getDataSummary returns room card count`() = runTest {
        every { screenshotCardRepository.observeStoredCards() } returns flowOf(
            listOf(mockk<StoredScreenshotCard>(), mockk<StoredScreenshotCard>(), mockk()),
        )

        val result = repository.getDataSummary()

        assertEquals(3L, result.getOrNull()?.capturedCount)
    }

    @Test
    fun `getConsentStatus reads local preferences`() = runTest {
        coEvery { userPreferencesRepository.getAiDataTransferConsentStatus() } returns ConsentStatus(
            consented = true,
            consentedAt = "2026-07-27T00:00:00Z",
        )

        val result = repository.getConsentStatus()

        assertEquals(true, result.getOrNull()?.consented)
        assertEquals("2026-07-27T00:00:00Z", result.getOrNull()?.consentedAt)
    }

    @Test
    fun `giveConsent persists consented status with timestamp`() = runTest {
        coEvery {
            userPreferencesRepository.setAiDataTransferConsent(consented = true, consentedAt = any())
        } just runs

        val result = repository.giveConsent()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            userPreferencesRepository.setAiDataTransferConsent(
                consented = true,
                consentedAt = match { it.isNotBlank() },
            )
        }
    }

    @Test
    fun `withdrawConsent clears local consent`() = runTest {
        coEvery {
            userPreferencesRepository.setAiDataTransferConsent(consented = false, consentedAt = null)
        } just runs

        val result = repository.withdrawConsent()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            userPreferencesRepository.setAiDataTransferConsent(
                consented = false,
                consentedAt = null,
            )
        }
    }

    @Test
    fun `deleteAccountData resets mock screenshot data without session clear`() = runTest {
        val result = repository.deleteAccountData()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockScreenshotDataResetter.reset() }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `getAccountInfo delegates to remote auth`() = runTest {
        val accountInfo = AccountInfo(platform = "KAKAO", createdAt = "2026-07-01T00:00:00Z")
        coEvery { remoteAuthRepository.getAccountInfo() } returns Result.success(accountInfo)

        val result = repository.getAccountInfo()

        assertEquals(accountInfo, result.getOrNull())
        coVerify(exactly = 1) { remoteAuthRepository.getAccountInfo() }
    }

    @Test
    fun `getAccountInfo propagates remote auth failure`() = runTest {
        val failure = IllegalStateException("auth failed")
        coEvery { remoteAuthRepository.getAccountInfo() } returns Result.failure(failure)

        val result = repository.getAccountInfo()

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }

    @Test
    fun `withdraw delegates to remote auth`() = runTest {
        coEvery { remoteAuthRepository.withdraw() } returns Result.success(Unit)

        val result = repository.withdraw()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { remoteAuthRepository.withdraw() }
    }

    @Test
    fun `withdraw propagates remote auth failure`() = runTest {
        val failure = IllegalStateException("withdraw failed")
        coEvery { remoteAuthRepository.withdraw() } returns Result.failure(failure)

        val result = repository.withdraw()

        assertTrue(result.isFailure)
        assertEquals(failure, result.exceptionOrNull())
    }

    @Test
    fun `local data paths do not touch remote auth repository`() = runTest {
        every { screenshotCardRepository.observeStoredCards() } returns flowOf(emptyList())
        coEvery { userPreferencesRepository.getAiDataTransferConsentStatus() } returns ConsentStatus(
            consented = false,
            consentedAt = null,
        )
        coEvery {
            userPreferencesRepository.setAiDataTransferConsent(consented = any(), consentedAt = any())
        } just runs

        repository.getDataSummary()
        repository.getConsentStatus()
        repository.giveConsent()
        repository.withdrawConsent()
        repository.deleteAccountData()

        coVerify(exactly = 0) { remoteAuthRepository.getAccountInfo() }
        coVerify(exactly = 0) { remoteAuthRepository.withdraw() }
        coVerify(exactly = 0) { remoteAuthRepository.getDataSummary() }
        coVerify(exactly = 0) { remoteAuthRepository.getConsentStatus() }
        coVerify(exactly = 0) { remoteAuthRepository.deleteAccountData() }
    }

    @Test
    fun `consent defaults path can report not consented`() = runTest {
        coEvery { userPreferencesRepository.getAiDataTransferConsentStatus() } returns ConsentStatus(
            consented = false,
            consentedAt = null,
        )

        val result = repository.getConsentStatus()

        assertFalse(result.getOrNull()!!.consented)
        assertNull(result.getOrNull()!!.consentedAt)
        assertNotNull(result.getOrNull())
    }
}
