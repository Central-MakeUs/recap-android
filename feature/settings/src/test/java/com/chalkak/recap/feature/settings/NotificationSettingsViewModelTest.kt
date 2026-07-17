package com.chalkak.recap.feature.settings

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationSettingsViewModelTest {
    @Test
    fun uiState_usesDefaultValues() {
        val viewModel = NotificationSettingsViewModel(SavedStateHandle())

        assertEquals(
            NotificationSettingsUiState(
                deviceNotificationsEnabled = true,
                organizeCompleteEnabled = true,
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun onAction_updatesToggleValuesAndSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = NotificationSettingsViewModel(savedStateHandle)

        viewModel.onAction(
            NotificationSettingsAction.OrganizeCompleteEnabledChanged(false),
        )

        assertEquals(
            NotificationSettingsUiState(
                deviceNotificationsEnabled = true,
                organizeCompleteEnabled = false,
            ),
            viewModel.uiState.value,
        )
        assertEquals(
            false,
            savedStateHandle.get<Boolean>(SETTINGS_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY),
        )
    }

    @Test
    fun recreatedViewModel_restoresToggleValuesFromSameSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        NotificationSettingsViewModel(savedStateHandle).apply {
            onAction(NotificationSettingsAction.OrganizeCompleteEnabledChanged(false))
        }

        val recreatedViewModel = NotificationSettingsViewModel(savedStateHandle)

        assertEquals(
            NotificationSettingsUiState(
                deviceNotificationsEnabled = true,
                organizeCompleteEnabled = false,
            ),
            recreatedViewModel.uiState.value,
        )
    }
}
