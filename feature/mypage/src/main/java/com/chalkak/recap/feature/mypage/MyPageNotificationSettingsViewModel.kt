package com.chalkak.recap.feature.mypage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class MyPageNotificationSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(savedStateHandle.restoreNotificationSettingsUiState())
    val uiState: StateFlow<MyPageNotificationSettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: MyPageNotificationSettingsAction) {
        when (action) {
            MyPageNotificationSettingsAction.NavigateBack -> Unit
            is MyPageNotificationSettingsAction.OrganizeCompleteEnabledChanged -> {
                updateOrganizeCompleteEnabled(action.enabled)
            }

            is MyPageNotificationSettingsAction.ReviewRequiredEnabledChanged -> {
                updateReviewRequiredEnabled(action.enabled)
            }

            is MyPageNotificationSettingsAction.MarketingEnabledChanged -> {
                updateMarketingEnabled(action.enabled)
            }
        }
    }

    private fun updateOrganizeCompleteEnabled(enabled: Boolean) {
        savedStateHandle[MY_PAGE_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY] = enabled
        _uiState.update { current ->
            current.copy(organizeCompleteEnabled = enabled)
        }
    }

    private fun updateReviewRequiredEnabled(enabled: Boolean) {
        savedStateHandle[MY_PAGE_NOTIFICATION_REVIEW_REQUIRED_ENABLED_KEY] = enabled
        _uiState.update { current ->
            current.copy(reviewRequiredEnabled = enabled)
        }
    }

    private fun updateMarketingEnabled(enabled: Boolean) {
        savedStateHandle[MY_PAGE_NOTIFICATION_MARKETING_ENABLED_KEY] = enabled
        _uiState.update { current ->
            current.copy(marketingEnabled = enabled)
        }
    }
}

internal const val MY_PAGE_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY =
    "my_page_notification_organize_complete_enabled"
internal const val MY_PAGE_NOTIFICATION_REVIEW_REQUIRED_ENABLED_KEY =
    "my_page_notification_review_required_enabled"
internal const val MY_PAGE_NOTIFICATION_MARKETING_ENABLED_KEY =
    "my_page_notification_marketing_enabled"

private fun SavedStateHandle.restoreNotificationSettingsUiState(): MyPageNotificationSettingsUiState =
    MyPageNotificationSettingsUiState(
        organizeCompleteEnabled = get<Boolean>(
            MY_PAGE_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY,
        ) ?: true,
        reviewRequiredEnabled = get<Boolean>(
            MY_PAGE_NOTIFICATION_REVIEW_REQUIRED_ENABLED_KEY,
        ) ?: true,
        marketingEnabled = get<Boolean>(
            MY_PAGE_NOTIFICATION_MARKETING_ENABLED_KEY,
        ) ?: false,
    )
