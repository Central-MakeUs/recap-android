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
    private val _events = MutableSharedFlow<DataManagementEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DataManagementEvent> = _events.asSharedFlow()

    val uiState: StateFlow<DataManagementUiState> = combine(
        organizedCount,
        showDeleteConfirmDialog,
    ) { count, showDialog ->
        DataManagementUiState(
            organizedCount = count,
            showDeleteConfirmDialog = showDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DataManagementUiState(),
    )

    init {
        refreshDataSummary()
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
        }
    }

    private fun refreshDataSummary() {
        viewModelScope.launch {
            val summary = userRepository.getDataSummary().getOrNull() ?: return@launch
            organizedCount.value = summary.capturedCount.toInt().coerceAtLeast(0)
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
