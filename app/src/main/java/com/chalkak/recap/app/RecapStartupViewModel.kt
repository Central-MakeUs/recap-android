package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.StartupDataRecoveryCoordinator
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.home.HomeRepository
import com.chalkak.recap.core.data.network.AuthSessionStateProvider
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.storage.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class RecapStartupViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionTokenStore: SessionTokenStore,
    private val authSessionStateProvider: AuthSessionStateProvider,
    private val homeRepository: HomeRepository,
    private val storageRepository: StorageRepository,
    private val startupDataRecoveryCoordinator: StartupDataRecoveryCoordinator,
) : ViewModel() {
    private val startupAttempts = MutableStateFlow(0)
    private val recoveryPhase = MutableStateFlow(StartupRecoveryPhase.Running)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RecapStartupUiState> = startupAttempts
        .flatMapLatest { observeStartupState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RecapStartupUiState.Loading,
        )

    private var startupJob: Job? = null

    init {
        observeMainEntry()
        startStartup()
    }

    fun retryStartup() {
        if (startupJob?.isActive == true) return
        recoveryPhase.value = StartupRecoveryPhase.Running
        startupAttempts.value += 1
        startStartup()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            // 온보딩 플래그를 먼저 내려야 세션만 사라진 순간의 Reauth 전환을 거치지 않는다.
            userPreferencesRepository.setOnboardingCompleted(false)
            sessionTokenStore.clear()
        }
    }

    private fun observeStartupState(): Flow<RecapStartupUiState> = combine(
        recoveryPhase,
        userPreferencesRepository.onboardingCompleted,
        authSessionStateProvider.hasSession,
    ) { phase, onboardingCompleted, hasSession ->
        when (phase) {
            StartupRecoveryPhase.Running -> RecapStartupUiState.Loading
            StartupRecoveryPhase.Failed -> RecapStartupUiState.ReadError
            StartupRecoveryPhase.Completed -> RecapStartupUiState.Ready(
                entryMode = resolveEntryMode(
                    onboardingCompleted = onboardingCompleted,
                    hasSession = hasSession,
                ),
            )
        }
    }.catch { error ->
        Timber.e(error, "App startup state stream failed")
        emit(RecapStartupUiState.ReadError)
    }

    private fun startStartup() {
        startupJob = viewModelScope.launch {
            try {
                startupDataRecoveryCoordinator.recoverIfNeeded()
                recoveryPhase.value = StartupRecoveryPhase.Completed
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Timber.e(error, "App startup data recovery failed")
                recoveryPhase.value = StartupRecoveryPhase.Failed
            }
        }
    }

    private fun observeMainEntry() {
        viewModelScope.launch {
            uiState
                .map { state ->
                    state is RecapStartupUiState.Ready && state.entryMode == RecapEntryMode.Main
                }
                .distinctUntilChanged()
                .filter { isMain -> isMain }
                .collect { prefetchMainTabs() }
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

private enum class StartupRecoveryPhase {
    Running,
    Failed,
    Completed,
}

/**
 * 온보딩 완료 여부와 세션 보유 여부로 결정되는 앱 진입 지점.
 *
 * 온보딩을 마친 사용자가 세션을 잃으면 온보딩을 처음부터 반복시키지 않고 [Reauth]로 보낸다.
 */
enum class RecapEntryMode {
    Onboarding,
    Reauth,
    Main,
}

internal fun resolveEntryMode(
    onboardingCompleted: Boolean,
    hasSession: Boolean,
): RecapEntryMode = when {
    !onboardingCompleted -> RecapEntryMode.Onboarding
    hasSession -> RecapEntryMode.Main
    else -> RecapEntryMode.Reauth
}

sealed interface RecapStartupUiState {
    data object Loading : RecapStartupUiState

    data object ReadError : RecapStartupUiState

    data class Ready(
        val entryMode: RecapEntryMode,
    ) : RecapStartupUiState
}
