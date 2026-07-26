package com.chalkak.recap.feature.settings.notification

import com.chalkak.recap.core.data.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationSettingsViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val organizeCompleteEnabled = MutableStateFlow(true)
    private val userPreferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.organizeCompleteEnabled } returns organizeCompleteEnabled
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_usesDefaultValues() = runTest(testDispatcher) {
        val viewModel = NotificationSettingsViewModel(userPreferencesRepository)
        advanceUntilIdle()

        assertEquals(
            NotificationSettingsUiState(
                deviceNotificationsEnabled = true,
                organizeCompleteEnabled = true,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun onAction_updatesToggleValuesInRepository() = runTest(testDispatcher) {
        val viewModel = NotificationSettingsViewModel(userPreferencesRepository)

        viewModel.onAction(
            NotificationSettingsAction.OrganizeCompleteEnabledChanged(false),
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            userPreferencesRepository.setOrganizeCompleteEnabled(false)
        }
    }

    @Test
    fun uiState_reflectsRepositoryFlowUpdates() = runTest(testDispatcher) {
        val viewModel = NotificationSettingsViewModel(userPreferencesRepository)
        advanceUntilIdle()

        organizeCompleteEnabled.value = false
        advanceUntilIdle()

        assertEquals(
            NotificationSettingsUiState(
                deviceNotificationsEnabled = true,
                organizeCompleteEnabled = false,
            ),
            viewModel.uiState.value,
        )
    }
}
