package com.chalkak.recap.feature.mypage

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class MyPageNotificationSettingsViewModelTest {
    @Test
    fun uiState_usesDefaultValues() {
        val viewModel = MyPageNotificationSettingsViewModel(SavedStateHandle())

        assertEquals(
            MyPageNotificationSettingsUiState(
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
        val viewModel = MyPageNotificationSettingsViewModel(savedStateHandle)

        viewModel.onAction(
            MyPageNotificationSettingsAction.OrganizeCompleteEnabledChanged(false),
        )
        viewModel.onAction(
            MyPageNotificationSettingsAction.ReviewRequiredEnabledChanged(false),
        )
        viewModel.onAction(
            MyPageNotificationSettingsAction.MarketingEnabledChanged(true),
        )

        assertEquals(
            MyPageNotificationSettingsUiState(
                organizeCompleteEnabled = false,
                reviewRequiredEnabled = false,
                marketingEnabled = true,
            ),
            viewModel.uiState.value,
        )
        assertEquals(
            false,
            savedStateHandle.get<Boolean>(MY_PAGE_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY),
        )
        assertEquals(
            false,
            savedStateHandle.get<Boolean>(MY_PAGE_NOTIFICATION_REVIEW_REQUIRED_ENABLED_KEY),
        )
        assertEquals(
            true,
            savedStateHandle.get<Boolean>(MY_PAGE_NOTIFICATION_MARKETING_ENABLED_KEY),
        )
    }

    @Test
    fun recreatedViewModel_restoresToggleValuesFromSameSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        MyPageNotificationSettingsViewModel(savedStateHandle).apply {
            onAction(MyPageNotificationSettingsAction.OrganizeCompleteEnabledChanged(false))
            onAction(MyPageNotificationSettingsAction.ReviewRequiredEnabledChanged(false))
            onAction(MyPageNotificationSettingsAction.MarketingEnabledChanged(true))
        }

        val recreatedViewModel = MyPageNotificationSettingsViewModel(savedStateHandle)

        assertEquals(
            MyPageNotificationSettingsUiState(
                organizeCompleteEnabled = false,
                reviewRequiredEnabled = false,
                marketingEnabled = true,
            ),
            recreatedViewModel.uiState.value,
        )
    }
}
