package com.chalkak.recap.feature.settings.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
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
import kotlinx.coroutines.launch

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : ViewModel() {
    private val showDeleteConfirmDialog = MutableStateFlow(false)
    private val _events = MutableSharedFlow<DataManagementEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DataManagementEvent> = _events.asSharedFlow()

    val uiState: StateFlow<DataManagementUiState> = combine(
        screenshotCardRepository.observeStoredCards(),
        showDeleteConfirmDialog,
    ) { cards, showDialog ->
        DataManagementUiState(
            organizedCount = cards.size,
            showDeleteConfirmDialog = showDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DataManagementUiState(),
    )

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

    private fun deleteAllData(deletedCount: Int) {
        viewModelScope.launch {
            val result = runCatching {
                screenshotCardRepository.deleteAllCards()
                screenshotImageStorage.clearStoredImages()
            }
            if (result.isSuccess) {
                _events.emit(DataManagementEvent.ShowDeleteSuccessToast(deletedCount))
            }
        }
    }
}
