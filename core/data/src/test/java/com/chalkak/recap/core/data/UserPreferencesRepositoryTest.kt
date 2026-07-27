package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.testdouble.InMemoryPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
    fun `organizeCompleteEnabled defaults to true`() = runTest {
        assertTrue(repository.organizeCompleteEnabled.first())
    }

    @Test
    fun `setOrganizeCompleteEnabled updates organizeCompleteEnabled flow`() = runTest {
        repository.setOrganizeCompleteEnabled(false)

        assertFalse(repository.organizeCompleteEnabled.first())
    }
}
