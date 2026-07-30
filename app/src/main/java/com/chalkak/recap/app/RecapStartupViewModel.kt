package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class RecapStartupViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionTokenStore: SessionTokenStore,
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val _pendingOpenOrganize = MutableStateFlow(false)
    val pendingOpenOrganize: StateFlow<Boolean> = _pendingOpenOrganize.asStateFlow()

    val uiState: StateFlow<RecapStartupUiState> =
        userPreferencesRepository.onboardingCompleted
            .onEach { onboardingCompleted ->
                if (onboardingCompleted) {
                    prefetchHomeSummary()
                }
            }
            .map { onboardingCompleted ->
                RecapStartupUiState.Ready(onboardingCompleted = onboardingCompleted)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RecapStartupUiState.Loading,
            )

    fun completeOnboarding(openOrganize: Boolean = false) {
        viewModelScope.launch {
            _pendingOpenOrganize.value = openOrganize
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun consumePendingOpenOrganize() {
        _pendingOpenOrganize.value = false
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            _pendingOpenOrganize.value = false
            sessionTokenStore.clear()
            userPreferencesRepository.setOnboardingCompleted(false)
        }
    }

    private fun prefetchHomeSummary() {
        viewModelScope.launch {
            homeRepository.prefetchSummary()
                .onFailure { error ->
                    Timber.w(error, "Home summary prefetch failed")
                }
        }
    }
}

sealed interface RecapStartupUiState {
    data object Loading : RecapStartupUiState

    data class Ready(
        val onboardingCompleted: Boolean,
    ) : RecapStartupUiState
}
