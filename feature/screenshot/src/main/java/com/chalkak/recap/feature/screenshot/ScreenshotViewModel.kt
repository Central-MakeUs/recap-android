package com.chalkak.recap.feature.screenshot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.design.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScreenshotUiState>(ScreenshotUiState.Loading)
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<ScreenshotEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var boundImageId: String? = null
    private var observeJob: Job? = null
    private var favoriteJob: Job? = null
    private var saveJob: Job? = null
    private var deleteJob: Job? = null

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun bind(imageId: String) {
        require(imageId.isNotBlank()) { "imageId must not be blank" }
        if (boundImageId == imageId && observeJob?.isActive == true) {
            return
        }
        boundImageId = imageId
        observeCard(imageId)
    }

    fun onAction(action: ScreenshotAction) {
        when (action) {
            ScreenshotAction.RetryLoad -> {
                val imageId = boundImageId ?: return
                observeCard(imageId)
            }

            ScreenshotAction.ToggleFavorite -> toggleFavorite()
            ScreenshotAction.PrepareEditDraft -> prepareEditDraft()
            ScreenshotAction.ClearActionError -> clearActionError()
            is ScreenshotAction.UpdateEditTitle -> updateEditDraft { it.copy(title = action.title) }
            is ScreenshotAction.UpdateEditSummary -> updateEditDraft { it.copy(summary = action.summary) }
            is ScreenshotAction.UpdateEditBody -> updateEditDraft { it.copy(body = action.body) }
            is ScreenshotAction.UpdateEditContentType -> {
                updateEditDraft { it.copy(contentType = action.contentType) }
            }

            ScreenshotAction.DiscardEditDraft -> discardEditDraft()
            ScreenshotAction.SaveEdit -> saveEdit()
            ScreenshotAction.DeleteScreenshot -> deleteScreenshot()
        }
    }

    private fun observeCard(imageId: String) {
        observeJob?.cancel()
        _uiState.value = ScreenshotUiState.Loading
        observeJob = viewModelScope.launch {
            try {
                screenshotCardRepository.observeCard(imageId).collect { card ->
                    if (card == null) {
                        val current = _uiState.value
                        if (current is ScreenshotUiState.Content && current.isDeleting) {
                            return@collect
                        }
                        _uiState.value = ScreenshotUiState.NotFound()
                        return@collect
                    }
                    _uiState.update { current ->
                        when (current) {
                            is ScreenshotUiState.Content -> current.copy(
                                card = card,
                                editDraft = current.editDraft,
                            )

                            else -> ScreenshotUiState.Content(
                                card = card,
                                editDraft = card.toEditDraft(),
                            )
                        }
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.value = ScreenshotUiState.LoadError()
            }
        }
    }

    private fun toggleFavorite() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isFavoriteUpdating || content.isDeleting) return
        val imageId = content.card.analysisResult.imageId
        val nextFavorite = !content.card.analysisResult.isFavorite
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            _uiState.updateContent { it.copy(isFavoriteUpdating = true, actionErrorMessageResId = null) }
            try {
                withContext(ioDispatcher) {
                    screenshotCardRepository.updateFavorite(imageId, nextFavorite)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.updateContent {
                    it.copy(
                        isFavoriteUpdating = false,
                        actionErrorMessageResId = R.string.screenshot_detail_favorite_error,
                    )
                }
                return@launch
            }
            _uiState.updateContent { it.copy(isFavoriteUpdating = false) }
        }
    }

    private fun prepareEditDraft() {
        _uiState.updateContent { content ->
            content.copy(
                editDraft = content.card.toEditDraft(),
                titleError = false,
                actionErrorMessageResId = null,
            )
        }
    }

    private fun discardEditDraft() {
        saveJob?.cancel()
        saveJob = null
        _uiState.updateContent { content ->
            content.copy(
                editDraft = content.card.toEditDraft(),
                titleError = false,
                isSaving = false,
                actionErrorMessageResId = null,
            )
        }
    }

    private fun updateEditDraft(transform: (ScreenshotEditDraft) -> ScreenshotEditDraft) {
        _uiState.updateContent { content ->
            if (content.isSaving) return@updateContent content
            val nextDraft = transform(content.editDraft)
            content.copy(
                editDraft = nextDraft,
                titleError = nextDraft.title.trim().isEmpty(),
                actionErrorMessageResId = null,
            )
        }
    }

    private fun clearActionError() {
        when (val state = _uiState.value) {
            is ScreenshotUiState.Content -> {
                _uiState.value = state.copy(actionErrorMessageResId = null)
            }

            is ScreenshotUiState.NotFound -> {
                _uiState.value = state.copy(actionErrorMessageResId = null)
            }

            is ScreenshotUiState.LoadError -> {
                _uiState.value = state.copy(actionErrorMessageResId = null)
            }

            ScreenshotUiState.Loading -> Unit
        }
    }

    private fun saveEdit() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isSaving || content.isDeleting) return
        val normalized = content.editDraft.normalizedForSave()
        if (!normalized.isTitleValid()) {
            _uiState.updateContent {
                it.copy(
                    editDraft = normalized,
                    titleError = true,
                )
            }
            return
        }
        val imageId = content.card.analysisResult.imageId
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            _uiState.updateContent {
                it.copy(
                    editDraft = normalized,
                    isSaving = true,
                    titleError = false,
                    actionErrorMessageResId = null,
                )
            }
            val updated = try {
                withContext(ioDispatcher) {
                    screenshotCardRepository.updateCardContent(
                        imageId = imageId,
                        title = normalized.title,
                        summary = normalized.summary,
                        body = normalized.body,
                        primaryContentType = normalized.contentType,
                    )
                }
            } catch (cancellation: CancellationException) {
                _uiState.updateContent { it.copy(isSaving = false) }
                throw cancellation
            } catch (_: Exception) {
                _uiState.updateContent {
                    it.copy(
                        isSaving = false,
                        actionErrorMessageResId = R.string.screenshot_edit_save_error,
                    )
                }
                return@launch
            }
            if (!updated) {
                _uiState.value = ScreenshotUiState.NotFound()
                return@launch
            }
            _uiState.updateContent { it.copy(isSaving = false) }
            eventChannel.send(ScreenshotEvent.SaveSucceeded)
        }
    }

    private fun deleteScreenshot() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isDeleting || content.isSaving) return
        val imageId = content.card.analysisResult.imageId
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            _uiState.updateContent {
                it.copy(isDeleting = true, actionErrorMessageResId = null)
            }
            try {
                withContext(ioDispatcher) {
                    screenshotCardRepository.deleteCard(imageId)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.updateContent {
                    it.copy(
                        isDeleting = false,
                        actionErrorMessageResId = R.string.screenshot_detail_delete_error,
                    )
                }
                return@launch
            }
            try {
                withContext(ioDispatcher) {
                    screenshotImageStorage.deleteStoredImages(setOf(imageId))
                }
            } catch (_: Exception) {
                // Best-effort private file cleanup; DB delete already succeeded.
            }
            eventChannel.send(ScreenshotEvent.DeleteSucceeded)
        }
    }

    private fun MutableStateFlow<ScreenshotUiState>.updateContent(
        transform: (ScreenshotUiState.Content) -> ScreenshotUiState.Content,
    ) {
        update { current ->
            if (current is ScreenshotUiState.Content) transform(current) else current
        }
    }
}
