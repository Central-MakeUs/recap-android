package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.storage.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val storageRepository: StorageRepository,
) : ViewModel() {
    val uiState: StateFlow<RecapStartupUiState> =
        userPreferencesRepository.onboardingCompleted
            .onEach { onboardingCompleted ->
                if (onboardingCompleted) {
                    prefetchMainTabs()
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

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            sessionTokenStore.clear()
            userPreferencesRepository.setOnboardingCompleted(false)
        }
    }

    private fun prefetchMainTabs() {
        prefetchHomeSummary()
        prefetchCollectionOverview()
    }

    private fun prefetchHomeSummary() {
        viewModelScope.launch {
            homeRepository.prefetchSummary()
                .onFailure { error ->
                    Timber.w(error, "Home summary prefetch failed")
                }
        }
    }

    private fun prefetchCollectionOverview() {
        viewModelScope.launch {
            storageRepository.prefetchOverview()
                .onFailure { error ->
                    Timber.w(error, "Collection overview prefetch failed")
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
