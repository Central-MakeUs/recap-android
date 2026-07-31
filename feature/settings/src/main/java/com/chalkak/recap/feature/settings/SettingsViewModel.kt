package com.chalkak.recap.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    fun prefetchDataManagement() {
        viewModelScope.launch {
            userRepository.prefetchDataSummary()
                .onFailure { error ->
                    Timber.w(error, "Data summary prefetch failed")
                }
        }
        viewModelScope.launch {
            userRepository.prefetchConsentStatus()
                .onFailure { error ->
                    Timber.w(error, "Consent status prefetch failed")
                }
        }
    }
}
