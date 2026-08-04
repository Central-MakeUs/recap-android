package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.StartupDataRecoveryCoordinator
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.storage.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class RecapStartupViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionTokenStore: SessionTokenStore,
    private val homeRepository: HomeRepository,
    private val storageRepository: StorageRepository,
    private val startupDataRecoveryCoordinator: StartupDataRecoveryCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecapStartupUiState>(RecapStartupUiState.Loading)
    val uiState: StateFlow<RecapStartupUiState> = _uiState.asStateFlow()

    private var startupJob: Job? = null

    init {
        startStartup()
    }

    fun retryStartup() {
        if (startupJob?.isActive == true) return
        _uiState.value = RecapStartupUiState.Loading
        startStartup()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
            if (_uiState.value is RecapStartupUiState.Ready) {
                _uiState.value = RecapStartupUiState.Ready(onboardingCompleted = true)
                prefetchMainTabs()
            }
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            sessionTokenStore.clear()
            userPreferencesRepository.setOnboardingCompleted(false)
            if (_uiState.value is RecapStartupUiState.Ready) {
                _uiState.value = RecapStartupUiState.Ready(onboardingCompleted = false)
            }
        }
    }

    private fun startStartup() {
        startupJob = viewModelScope.launch {
            try {
                startupDataRecoveryCoordinator.recoverIfNeeded()
                val onboardingCompleted = userPreferencesRepository.onboardingCompleted.first()
                if (onboardingCompleted) {
                    prefetchMainTabs()
                }
                _uiState.value = RecapStartupUiState.Ready(onboardingCompleted = onboardingCompleted)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Timber.e(error, "App startup data recovery failed")
                _uiState.value = RecapStartupUiState.ReadError
            }
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

    data object ReadError : RecapStartupUiState

    data class Ready(
        val onboardingCompleted: Boolean,
    ) : RecapStartupUiState
}
