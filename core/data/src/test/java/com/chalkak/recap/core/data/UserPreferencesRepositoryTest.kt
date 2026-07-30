package com.chalkak.recap.core.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserPreferencesRepositoryTest {
    private lateinit var dataStore: InMemoryPreferencesDataStore
    private lateinit var repository: UserPreferencesRepository

    @BeforeEach
    fun setUp() {
        dataStore = InMemoryPreferencesDataStore()
        repository = UserPreferencesRepository(dataStore)
    }

    @Test
    fun `onboardingCompleted defaults to false`() = runTest {
        assertFalse(repository.onboardingCompleted.first())
    }

    @Test
    fun `setOnboardingCompleted updates onboardingCompleted flow`() = runTest {
        repository.setOnboardingCompleted(true)

        assertTrue(repository.onboardingCompleted.first())
    }

    @Test
    fun `organizeCompleteNotificationEnabled defaults to false`() = runTest {
        assertFalse(repository.organizeCompleteNotificationEnabled.first())
    }

    @Test
    fun `setOrganizeCompleteNotificationEnabled updates flow`() = runTest {
        repository.setOrganizeCompleteNotificationEnabled(true)

        assertTrue(repository.organizeCompleteNotificationEnabled.first())
    }

    @Test
    fun `organizeCompleteNotificationEnabled migrates legacy key`() = runTest {
        val legacyKey = booleanPreferencesKey("organize_complete_enabled")
        dataStore = InMemoryPreferencesDataStore(
            mutablePreferencesOf(legacyKey to true),
        )
        repository = UserPreferencesRepository(dataStore)

        assertTrue(repository.organizeCompleteNotificationEnabled.first())
    }

    @Test
    fun `getAiDataTransferConsentStatus defaults to not consented`() = runTest {
        val status = repository.getAiDataTransferConsentStatus()

        assertFalse(status.consented)
        assertEquals(null, status.consentedAt)
    }

    @Test
    fun `setAiDataTransferConsent persists consented status`() = runTest {
        repository.setAiDataTransferConsent(
            consented = true,
            consentedAt = "2026-07-27T00:00:00Z",
        )

        val status = repository.getAiDataTransferConsentStatus()

        assertTrue(status.consented)
        assertEquals("2026-07-27T00:00:00Z", status.consentedAt)
    }

    @Test
    fun `setAiDataTransferConsent clears consentedAt when withdrawn`() = runTest {
        repository.setAiDataTransferConsent(
            consented = true,
            consentedAt = "2026-07-27T00:00:00Z",
        )

        repository.setAiDataTransferConsent(consented = false, consentedAt = null)

        val status = repository.getAiDataTransferConsentStatus()

        assertFalse(status.consented)
        assertEquals(null, status.consentedAt)
    }
}
