package com.chalkak.recap.feature.settings.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<NotificationSettingsUiState> =
        userPreferencesRepository.organizeCompleteNotificationEnabled
            .map { enabled ->
                NotificationSettingsUiState(organizeCompleteNotificationEnabled = enabled)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = NotificationSettingsUiState(),
            )

    fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.NavigateBack,
            NotificationSettingsAction.RequestDeviceNotificationPermission,
            -> Unit

            is NotificationSettingsAction.OrganizeCompleteNotificationEnabledChanged -> {
                viewModelScope.launch {
                    userPreferencesRepository.setOrganizeCompleteNotificationEnabled(action.enabled)
                }
            }
        }
    }
}
