package com.chalkak.recap.feature.organize

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
class OrganizeViewModel @Inject constructor(
    private val localScreenshotDataSource: LocalScreenshotDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrganizeUiState())
    val uiState: StateFlow<OrganizeUiState> = _uiState.asStateFlow()

    fun onAction(action: OrganizeAction) {
        when (action) {
            is OrganizeAction.ToggleSelection -> toggleSelection(action.uri)
            is OrganizeAction.RemoveSelection -> removeSelection(action.uri)
            OrganizeAction.ClearSelection -> clearSelection()
            OrganizeAction.DismissMaxSelectionMessage -> {
                _uiState.update { it.copy(showMaxSelectionReached = false) }
            }
        }
    }

    fun refreshScreenshots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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

    private fun clearSelection() {
        _uiState.update { state ->
            state.copy(
                selectedUris = emptyList(),
                showMaxSelectionReached = false,
            )
        }
    }
}
