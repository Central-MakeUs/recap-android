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
        userPreferencesRepository.organizeCompleteEnabled
            .map { enabled ->
                NotificationSettingsUiState(organizeCompleteEnabled = enabled)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = NotificationSettingsUiState(),
            )

    fun onAction(action: NotificationSettingsAction) {
        when (action) {
            NotificationSettingsAction.NavigateBack,
            NotificationSettingsAction.OpenDeviceNotificationSettings,
            -> Unit

            is NotificationSettingsAction.OrganizeCompleteEnabledChanged -> {
                viewModelScope.launch {
                    userPreferencesRepository.setOrganizeCompleteEnabled(action.enabled)
                }
            }
        }
    }
}
