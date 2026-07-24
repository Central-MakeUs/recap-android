package com.chalkak.recap.feature.organize

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.model.LocalImage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OrganizeViewModel @Inject constructor(
    private val localScreenshotDataSource: LocalScreenshotDataSource,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val restoredShareState = restoreShareState()
    private val _uiState = MutableStateFlow(
        restoredShareState?.uiState ?: OrganizeUiState(),
    )
    val uiState: StateFlow<OrganizeUiState> = _uiState.asStateFlow()
    private var seededShareSessionId: String? = restoredShareState?.sessionId
    private var sharedSourceImages: List<LocalImage> =
        restoredShareState?.sourceImages.orEmpty()
    private var refreshJob: Job? = null
    private var refreshGeneration = 0L

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

    fun seedSharedImages(
        sessionId: String,
        images: List<LocalImage>,
    ) {
        if (seededShareSessionId == sessionId) return
        invalidateRefresh()
        seededShareSessionId = sessionId
        sharedSourceImages = images
        _uiState.update {
            it.copy(
                isLoading = false,
                availableScreenshots = images,
                selectedUris = images.map { image -> image.uri },
                showMaxSelectionReached = false,
            )
        }
        persistShareState()
    }

    fun refreshScreenshots() {
        val generation = invalidateRefresh()
        seededShareSessionId = null
        sharedSourceImages = emptyList()
        clearPersistedShareState()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val screenshots = localScreenshotDataSource.queryAllScreenshots()
            if (generation != refreshGeneration) return@launch
            _uiState.update {
                it.copy(
                    isLoading = false,
                    availableScreenshots = screenshots,
                )
            }
        }
    }

    fun refreshScreenshotsMergingSelected() {
        val generation = invalidateRefresh()
        val sessionId = seededShareSessionId
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val gallery = localScreenshotDataSource.queryAllScreenshots()
            if (generation != refreshGeneration || seededShareSessionId != sessionId) {
                return@launch
            }
            _uiState.update { state ->
                val galleryUris = gallery.map { image -> image.uri }.toHashSet()
                val orphanSelected = state.selectedUris.mapNotNull { uri ->
                    if (uri in galleryUris) {
                        null
                    } else {
                        state.availableScreenshots.find { image -> image.uri == uri }
                    }
                }
                state.copy(
                    isLoading = false,
                    availableScreenshots = orphanSelected + gallery,
                )
            }
            persistShareState()
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
        persistShareState()
    }

    private fun removeSelection(uri: String) {
        _uiState.update { state ->
            state.copy(selectedUris = state.selectedUris.filterNot { it == uri })
        }
        persistShareState()
    }

    private fun clearSelection() {
        invalidateRefresh()
        _uiState.update { state ->
            state.copy(
                selectedUris = emptyList(),
                showMaxSelectionReached = false,
            )
        }
        seededShareSessionId = null
        sharedSourceImages = emptyList()
        clearPersistedShareState()
    }

    private fun invalidateRefresh(): Long {
        refreshJob?.cancel()
        refreshGeneration += 1
        return refreshGeneration
    }

    private fun persistShareState() {
        val sessionId = seededShareSessionId ?: return
        val state = _uiState.value
        val selectedImages = state.selectedUris.mapNotNull { selectedUri ->
            state.availableScreenshots.find { image -> image.uri == selectedUri }
        }
        savedStateHandle[SHARE_SESSION_ID_KEY] = sessionId
        saveImages(SHARE_SOURCE_PREFIX, sharedSourceImages)
        saveImages(SHARE_SELECTED_PREFIX, selectedImages)
    }

    private fun clearPersistedShareState() {
        savedStateHandle.remove<String>(SHARE_SESSION_ID_KEY)
        clearImages(SHARE_SOURCE_PREFIX)
        clearImages(SHARE_SELECTED_PREFIX)
    }

    private fun restoreShareState(): RestoredShareState? {
        val sessionId = savedStateHandle.get<String>(SHARE_SESSION_ID_KEY) ?: return null
        val sourceImages = restoreImages(SHARE_SOURCE_PREFIX)
        if (sourceImages.isEmpty()) return null
        val selectedImages = restoreImages(SHARE_SELECTED_PREFIX)
        val availableImages = (sourceImages + selectedImages).distinctBy { image -> image.uri }
        return RestoredShareState(
            sessionId = sessionId,
            sourceImages = sourceImages,
            uiState = OrganizeUiState(
                isLoading = false,
                availableScreenshots = availableImages,
                selectedUris = selectedImages.map { image -> image.uri },
            ),
        )
    }

    private fun saveImages(
        prefix: String,
        images: List<LocalImage>,
    ) {
        savedStateHandle["${prefix}_uris"] = ArrayList(images.map { image -> image.uri })
        savedStateHandle["${prefix}_names"] =
            ArrayList(images.map { image -> image.displayName })
        savedStateHandle["${prefix}_dates"] =
            images.map { image -> image.dateAddedMillis }.toLongArray()
    }

    private fun restoreImages(prefix: String): List<LocalImage> {
        val uris = savedStateHandle.get<ArrayList<String>>("${prefix}_uris") ?: return emptyList()
        val names = savedStateHandle.get<ArrayList<String>>("${prefix}_names") ?: return emptyList()
        val dates = savedStateHandle.get<LongArray>("${prefix}_dates") ?: return emptyList()
        if (uris.size != names.size || uris.size != dates.size) return emptyList()
        return uris.indices.map { index ->
            LocalImage(
                uri = uris[index],
                displayName = names[index],
                dateAddedMillis = dates[index],
            )
        }
    }

    private fun clearImages(prefix: String) {
        savedStateHandle.remove<ArrayList<String>>("${prefix}_uris")
        savedStateHandle.remove<ArrayList<String>>("${prefix}_names")
        savedStateHandle.remove<LongArray>("${prefix}_dates")
    }
}

private data class RestoredShareState(
    val sessionId: String,
    val sourceImages: List<LocalImage>,
    val uiState: OrganizeUiState,
)

private const val SHARE_SESSION_ID_KEY = "organize_share_session_id"
private const val SHARE_SOURCE_PREFIX = "organize_share_source"
private const val SHARE_SELECTED_PREFIX = "organize_share_selected"
