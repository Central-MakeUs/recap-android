package com.chalkak.recap.core.data

import androidx.datastore.preferences.core.stringPreferencesKey
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
    fun `onboardingStep defaults to null`() = runTest {
        assertEquals(null, repository.getOnboardingStep())
    }

    @Test
    fun `setOnboardingStep persists and clearOnboardingStep removes value`() = runTest {
        repository.setOnboardingStep("PermissionGuide")

        assertEquals("PermissionGuide", repository.getOnboardingStep())

        repository.clearOnboardingStep()

        assertEquals(null, repository.getOnboardingStep())
        assertEquals(null, dataStore.current()[stringPreferencesKey("onboarding_step")])
    }
}
