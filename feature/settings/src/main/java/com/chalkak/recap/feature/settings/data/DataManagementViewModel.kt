package com.chalkak.recap.feature.settings.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : ViewModel() {
    private val organizedCount = MutableStateFlow(0)
    private val showDeleteConfirmDialog = MutableStateFlow(false)
    private val showWithdrawConsentDialog = MutableStateFlow(false)
    private val isAiDataTransferConsented = MutableStateFlow(false)
    private val aiDataTransferConsentDate = MutableStateFlow("")
    private val _events = MutableSharedFlow<DataManagementEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DataManagementEvent> = _events.asSharedFlow()

    val uiState: StateFlow<DataManagementUiState> = combine(
        organizedCount,
        showDeleteConfirmDialog,
        showWithdrawConsentDialog,
        isAiDataTransferConsented,
        aiDataTransferConsentDate,
    ) { count, showDeleteDialog, showWithdrawDialog, isConsented, consentDate ->
        DataManagementUiState(
            organizedCount = count,
            showDeleteConfirmDialog = showDeleteDialog,
            showWithdrawConsentDialog = showWithdrawDialog,
            isAiDataTransferConsented = isConsented,
            aiDataTransferConsentDate = consentDate,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DataManagementUiState(),
    )

    init {
        refreshDataSummary()
        refreshConsentStatus()
    }

    fun onAction(action: DataManagementAction) {
        when (action) {
            DataManagementAction.NavigateBack -> Unit
            DataManagementAction.DeleteDataClick -> {
                showDeleteConfirmDialog.value = true
            }
            DataManagementAction.DismissDeleteConfirmDialog -> {
                showDeleteConfirmDialog.value = false
            }
            DataManagementAction.ConfirmDeleteData -> {
                showDeleteConfirmDialog.value = false
                deleteAllData(deletedCount = uiState.value.organizedCount)
            }
            DataManagementAction.AiDataTransferConsentClick -> {
                if (isAiDataTransferConsented.value) {
                    showWithdrawConsentDialog.value = true
                } else {
                    giveConsent()
                }
            }
            DataManagementAction.DismissWithdrawConsentDialog -> {
                showWithdrawConsentDialog.value = false
            }
            DataManagementAction.ConfirmWithdrawConsent -> {
                showWithdrawConsentDialog.value = false
                withdrawConsent()
            }
        }
    }

    private fun giveConsent() {
        viewModelScope.launch {
            val result = userRepository.giveConsent()
            if (result.isFailure) {
                return@launch
            }
            refreshConsentStatus()
        }
    }

    private fun withdrawConsent() {
        viewModelScope.launch {
            val result = userRepository.withdrawConsent()
            if (result.isFailure) {
                return@launch
            }
            refreshConsentStatus()
            _events.emit(DataManagementEvent.ShowConsentWithdrawnToast)
        }
    }

    private fun refreshDataSummary() {
        viewModelScope.launch {
            val summary = userRepository.getDataSummary().getOrNull() ?: return@launch
            organizedCount.value = summary.capturedCount.toInt().coerceAtLeast(0)
        }
    }

    private fun refreshConsentStatus() {
        viewModelScope.launch {
            val status = userRepository.getConsentStatus().getOrNull() ?: return@launch
            isAiDataTransferConsented.value = status.consented
            aiDataTransferConsentDate.value = formatConsentDateFromIso(status.consentedAt)
        }
    }

    private fun deleteAllData(deletedCount: Int) {
        viewModelScope.launch {
            val remoteResult = userRepository.deleteAccountData()
            if (remoteResult.isFailure) {
                return@launch
            }
            runCatching {
                screenshotCardRepository.deleteAllCards()
                thumbnailCache.clearAll()
                changeNotifier.notifyCaptureChanged()
            }
            organizedCount.update { 0 }
            _events.emit(DataManagementEvent.ShowDeleteSuccessToast(deletedCount))
            refreshDataSummary()
        }
    }
}
