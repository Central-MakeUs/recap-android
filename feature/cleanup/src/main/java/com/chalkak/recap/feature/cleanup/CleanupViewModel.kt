package com.chalkak.recap.feature.cleanup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.LocalScreenshotDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CleanupViewModel @Inject constructor(
    private val localScreenshotDataSource: LocalScreenshotDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CleanupUiState())
    val uiState: StateFlow<CleanupUiState> = _uiState.asStateFlow()

    init {
        loadScreenshots()
    }

    fun onAction(action: CleanupAction) {
        when (action) {
            is CleanupAction.ToggleSelection -> toggleSelection(action.uri)
            is CleanupAction.RemoveSelection -> removeSelection(action.uri)
            CleanupAction.DismissMaxSelectionMessage -> {
                _uiState.update { it.copy(showMaxSelectionReached = false) }
            }
        }
    }

    private fun loadScreenshots() {
        viewModelScope.launch {
            val screenshots = localScreenshotDataSource.queryAllScreenshots()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    availableScreenshots = screenshots,
                )
            }
        }
    }

    private fun toggleSelection(uri: String) {
        _uiState.update { state ->
            val currentSelection = state.selectedUris
            when {
                uri in currentSelection -> {
                    state.copy(selectedUris = currentSelection.filterNot { it == uri })
                }

                currentSelection.size >= MAX_SELECTION_COUNT -> {
                    state.copy(showMaxSelectionReached = true)
                }

                else -> {
                    state.copy(selectedUris = currentSelection + uri)
                }
            }
        }
    }

    private fun removeSelection(uri: String) {
        _uiState.update { state ->
            state.copy(selectedUris = state.selectedUris.filterNot { it == uri })
        }
    }
}
