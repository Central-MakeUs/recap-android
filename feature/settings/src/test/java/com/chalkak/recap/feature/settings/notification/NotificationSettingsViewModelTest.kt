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
    private val organizeCompleteNotificationEnabled = MutableStateFlow(false)
    private val userPreferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every {
            userPreferencesRepository.organizeCompleteNotificationEnabled
        } returns organizeCompleteNotificationEnabled
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
                organizeCompleteNotificationEnabled = false,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun onAction_updatesToggleValuesInRepository() = runTest(testDispatcher) {
        val viewModel = NotificationSettingsViewModel(userPreferencesRepository)

        viewModel.onAction(
            NotificationSettingsAction.OrganizeCompleteNotificationEnabledChanged(true),
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            userPreferencesRepository.setOrganizeCompleteNotificationEnabled(true)
        }
    }

    @Test
    fun uiState_reflectsRepositoryFlowUpdates() = runTest(testDispatcher) {
        val viewModel = NotificationSettingsViewModel(userPreferencesRepository)
        advanceUntilIdle()

        organizeCompleteNotificationEnabled.value = true
        advanceUntilIdle()

        assertEquals(
            NotificationSettingsUiState(
                deviceNotificationsEnabled = true,
                organizeCompleteNotificationEnabled = true,
            ),
            viewModel.uiState.value,
        )
    }
}
