package com.chalkak.recap.feature.settings.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    private val organizedCount = MutableStateFlow<Int?>(null)
    private val showDeleteConfirmDialog = MutableStateFlow(false)
    private val showWithdrawConsentDialog = MutableStateFlow(false)
    private val showAiDataTransferConsentSheet = MutableStateFlow(false)
    private val isAiDataTransferConsented = MutableStateFlow<Boolean?>(null)
    private val aiDataTransferConsentDate = MutableStateFlow("")
    private val dataSummaryFetchError = MutableStateFlow(false)
    private val consentStatusFetchError = MutableStateFlow(false)
    private val _events = MutableSharedFlow<DataManagementEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DataManagementEvent> = _events.asSharedFlow()

    private val dialogVisibility = combine(
        showDeleteConfirmDialog,
        showWithdrawConsentDialog,
        showAiDataTransferConsentSheet,
    ) { showDeleteDialog, showWithdrawDialog, showConsentSheet ->
        DialogVisibility(
            showDeleteConfirmDialog = showDeleteDialog,
            showWithdrawConsentDialog = showWithdrawDialog,
            showAiDataTransferConsentSheet = showConsentSheet,
        )
    }

    private val hasFetchError = combine(
        dataSummaryFetchError,
        consentStatusFetchError,
    ) { summaryError, consentError ->
        summaryError || consentError
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )

    val uiState: StateFlow<DataManagementUiState> = combine(
        organizedCount,
        dialogVisibility,
        isAiDataTransferConsented,
        aiDataTransferConsentDate,
        hasFetchError,
    ) { count, dialogs, isConsented, consentDate, fetchError ->
        DataManagementUiState(
            organizedCount = count,
            showDeleteConfirmDialog = dialogs.showDeleteConfirmDialog,
            showWithdrawConsentDialog = dialogs.showWithdrawConsentDialog,
            showAiDataTransferConsentSheet = dialogs.showAiDataTransferConsentSheet,
            isAiDataTransferConsented = isConsented,
            aiDataTransferConsentDate = consentDate,
            hasFetchError = fetchError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DataManagementUiState(),
    )

    init {
        observeDataSummary()
        observeConsentStatus()
    }

    fun onAction(action: DataManagementAction) {
        when (action) {
            DataManagementAction.NavigateBack -> Unit
            DataManagementAction.DeleteDataClick -> {
                if (hasFetchError.value) return
                when (organizedCount.value) {
                    null -> Unit
                    0 -> {
                        viewModelScope.launch {
                            _events.emit(DataManagementEvent.ShowNoDataToDeleteToast)
                        }
                    }
                    else -> showDeleteConfirmDialog.value = true
                }
            }
            DataManagementAction.DismissDeleteConfirmDialog -> {
                showDeleteConfirmDialog.value = false
            }
            DataManagementAction.ConfirmDeleteData -> {
                showDeleteConfirmDialog.value = false
                if (hasFetchError.value) return
                deleteAllData(deletedCount = uiState.value.organizedCount ?: 0)
            }
            DataManagementAction.AiDataTransferConsentClick -> {
                if (hasFetchError.value) return
                when (isAiDataTransferConsented.value) {
                    true -> showWithdrawConsentDialog.value = true
                    false -> showAiDataTransferConsentSheet.value = true
                    null -> Unit
                }
            }
            DataManagementAction.AgreeAiDataTransferConsent -> {
                if (hasFetchError.value) return
                if (!showAiDataTransferConsentSheet.value) return
                giveConsent()
            }
            DataManagementAction.DismissAiDataTransferConsent -> {
                showAiDataTransferConsentSheet.value = false
            }
            DataManagementAction.DismissWithdrawConsentDialog -> {
                showWithdrawConsentDialog.value = false
            }
            DataManagementAction.ConfirmWithdrawConsent -> {
                showWithdrawConsentDialog.value = false
                if (hasFetchError.value) return
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
            // UI가 sheetState.hide()를 돌리도록 먼저 consented로 반영한다.
            isAiDataTransferConsented.value = true
        }
    }

    private fun withdrawConsent() {
        viewModelScope.launch {
            val result = userRepository.withdrawConsent()
            if (result.isFailure) {
                return@launch
            }
            _events.emit(DataManagementEvent.ShowConsentWithdrawnToast)
        }
    }

    private fun observeDataSummary() {
        viewModelScope.launch {
            userRepository.observeDataSummary().collect { result ->
                result
                    .onSuccess { summary ->
                        organizedCount.value = summary.capturedCount.toInt().coerceAtLeast(0)
                        dataSummaryFetchError.value = false
                    }
                    .onFailure {
                        dataSummaryFetchError.value = true
                    }
            }
        }
    }

    private fun observeConsentStatus() {
        viewModelScope.launch {
            userRepository.observeConsentStatus().collect { result ->
                result
                    .onSuccess { status ->
                        isAiDataTransferConsented.value = status.consented
                        aiDataTransferConsentDate.value = formatConsentDateFromIso(status.consentedAt)
                        consentStatusFetchError.value = false
                    }
                    .onFailure {
                        consentStatusFetchError.value = true
                    }
            }
        }
    }

    private fun deleteAllData(deletedCount: Int) {
        viewModelScope.launch {
            val result = userRepository.deleteAccountData()
            if (result.isFailure) {
                return@launch
            }
            organizedCount.update { 0 }
            _events.emit(DataManagementEvent.ShowDeleteSuccessToast(deletedCount))
        }
    }

    private data class DialogVisibility(
        val showDeleteConfirmDialog: Boolean,
        val showWithdrawConsentDialog: Boolean,
        val showAiDataTransferConsentSheet: Boolean,
    )
}
