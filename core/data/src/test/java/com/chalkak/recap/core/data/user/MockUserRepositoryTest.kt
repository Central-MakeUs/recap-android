package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.screenshot.backend.MockScreenshotDataResetter
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
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
    fun `getAccountInfo is unsupported`() = runTest {
        val result = repository.getAccountInfo()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnsupportedOperationException)
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
