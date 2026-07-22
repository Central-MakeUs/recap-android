package com.chalkak.recap.feature.settings.notification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(savedStateHandle.restoreNotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.NavigateBack,
            NotificationSettingsAction.OpenDeviceNotificationSettings,
            -> Unit

            is NotificationSettingsAction.OrganizeCompleteEnabledChanged -> {
                updateOrganizeCompleteEnabled(action.enabled)
            }
        }
    }

    private fun updateOrganizeCompleteEnabled(enabled: Boolean) {
        savedStateHandle[SETTINGS_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY] = enabled
        _uiState.update { current ->
            current.copy(organizeCompleteEnabled = enabled)
        }
    }
}

internal const val SETTINGS_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY =
    "settings_notification_organize_complete_enabled"

private fun SavedStateHandle.restoreNotificationSettingsUiState(): NotificationSettingsUiState =
    NotificationSettingsUiState(
        organizeCompleteEnabled = get<Boolean>(
            SETTINGS_NOTIFICATION_ORGANIZE_COMPLETE_ENABLED_KEY,
        ) ?: true,
    )
