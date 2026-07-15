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
                organizeCompleteEnabled = true,
                reviewRequiredEnabled = true,
                marketingEnabled = false,
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
        viewModel.onAction(
            NotificationSettingsAction.ReviewRequiredEnabledChanged(false),
        )
        viewModel.onAction(
            NotificationSettingsAction.MarketingEnabledChanged(true),
        )

        assertEquals(
            NotificationSettingsUiState(
                organizeCompleteEnabled = false,
                reviewRequiredEnabled = false,
                marketingEnabled = true,
            ),
            viewModel.uiState.value,
        )
        assertEquals(
            false,
            savedStateHandle.get<Boolean>(SETTINGS_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY),
        )
        assertEquals(
            false,
            savedStateHandle.get<Boolean>(SETTINGS_NOTIFICATION_REVIEW_REQUIRED_ENABLED_KEY),
        )
        assertEquals(
            true,
            savedStateHandle.get<Boolean>(SETTINGS_NOTIFICATION_MARKETING_ENABLED_KEY),
        )
    }

    @Test
    fun recreatedViewModel_restoresToggleValuesFromSameSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        NotificationSettingsViewModel(savedStateHandle).apply {
            onAction(NotificationSettingsAction.OrganizeCompleteEnabledChanged(false))
            onAction(NotificationSettingsAction.ReviewRequiredEnabledChanged(false))
            onAction(NotificationSettingsAction.MarketingEnabledChanged(true))
        }

        val recreatedViewModel = NotificationSettingsViewModel(savedStateHandle)

        assertEquals(
            NotificationSettingsUiState(
                organizeCompleteEnabled = false,
                reviewRequiredEnabled = false,
                marketingEnabled = true,
            ),
            recreatedViewModel.uiState.value,
        )
    }
}
