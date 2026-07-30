package com.chalkak.recap.feature.screenshot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotDetailRepository
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.capture.ReportReason
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
    private val screenshotDetailRepository: ScreenshotDetailRepository,
    private val captureMutationRepository: CaptureMutationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScreenshotUiState>(ScreenshotUiState.Loading)
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<ScreenshotEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    private var boundCaptureId: Long? = null
    private var observeJob: Job? = null
    private var favoriteJob: Job? = null
    private var saveJob: Job? = null
    private var deleteJob: Job? = null
    private var reportJob: Job? = null

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    fun bind(captureId: Long) {
        require(captureId > 0) { "captureId must be positive" }
        if (boundCaptureId == captureId && observeJob?.isActive == true) {
            return
        }
        boundCaptureId = captureId
        observeCard(captureId)
    }

    fun onAction(action: ScreenshotAction) {
        when (action) {
            ScreenshotAction.RetryLoad -> {
                val captureId = boundCaptureId ?: return
                observeCard(captureId)
            }

            ScreenshotAction.ToggleFavorite -> toggleFavorite()
            ScreenshotAction.PrepareEditDraft -> prepareEditDraft()
            ScreenshotAction.ClearActionError -> clearActionError()
            is ScreenshotAction.UpdateEditTitle -> {
                updateEditDraft { it.copy(title = sanitizeEditTitleInput(action.title)) }
            }
            is ScreenshotAction.UpdateEditSummary -> {
                updateEditDraft { it.copy(summary = sanitizeEditSummaryInput(action.summary)) }
            }
            is ScreenshotAction.UpdateEditBody -> updateEditDraft { it.copy(body = action.body) }
            is ScreenshotAction.UpdateEditContentType -> {
                updateEditDraft { it.copy(contentType = action.contentType) }
            }

            ScreenshotAction.DiscardEditDraft -> discardEditDraft()
            ScreenshotAction.SaveEdit -> saveEdit()
            ScreenshotAction.ShowDiscardEditConfirmDialog -> showDiscardEditConfirmDialog()
            ScreenshotAction.DismissDiscardEditConfirmDialog -> dismissDiscardEditConfirmDialog()
            ScreenshotAction.ShowDeleteConfirmDialog -> showDeleteConfirmDialog()
            ScreenshotAction.DismissDeleteConfirmDialog -> dismissDeleteConfirmDialog()
            ScreenshotAction.DeleteScreenshot -> deleteScreenshot()
            is ScreenshotAction.SubmitReport -> submitReport(
                reason = action.reason,
                detail = action.detail,
            )
        }
    }

    private fun showDiscardEditConfirmDialog() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isSaving || !content.hasUnsavedEditChanges()) return
        _uiState.updateContent { it.copy(showDiscardEditConfirmDialog = true) }
    }

    private fun dismissDiscardEditConfirmDialog() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (!content.showDiscardEditConfirmDialog || content.isSaving) return
        _uiState.updateContent { it.copy(showDiscardEditConfirmDialog = false) }
    }

    private fun showDeleteConfirmDialog() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isDeleting || content.isSaving) return
        _uiState.updateContent { it.copy(showDeleteConfirmDialog = true) }
    }

    private fun dismissDeleteConfirmDialog() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (!content.showDeleteConfirmDialog || content.isDeleting) return
        _uiState.updateContent { it.copy(showDeleteConfirmDialog = false) }
    }

    private fun observeCard(captureId: Long) {
        observeJob?.cancel()
        _uiState.value = ScreenshotUiState.Loading
        observeJob = viewModelScope.launch {
            try {
                screenshotDetailRepository.observeCard(captureId).collect { card ->
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
        val captureId = content.card.analysisResult.captureId
        val previousFavorite = content.card.analysisResult.isFavorite
        val nextFavorite = !previousFavorite
        favoriteJob?.cancel()
        _uiState.updateContent { current ->
            current.copy(
                isFavoriteUpdating = true,
                actionErrorMessageResId = null,
                card = current.card.copy(
                    analysisResult = current.card.analysisResult.copy(isFavorite = nextFavorite),
                ),
            )
        }
        favoriteJob = viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                captureMutationRepository.updateFavorite(
                    captureId = captureId,
                    isFavorite = nextFavorite,
                )
            }
            if (result.isFailure) {
                _uiState.updateContent {
                    it.copy(
                        isFavoriteUpdating = false,
                        actionErrorMessageResId = R.string.screenshot_detail_favorite_error,
                        card = it.card.copy(
                            analysisResult = it.card.analysisResult.copy(isFavorite = previousFavorite),
                        ),
                    )
                }
                return@launch
            }
            _uiState.updateContent { it.copy(isFavoriteUpdating = false) }
            eventChannel.send(ScreenshotEvent.ShowFavoriteToast(isFavorite = nextFavorite))
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
                showDiscardEditConfirmDialog = false,
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
        if (content.isSaving || content.isDeleting || !content.hasUnsavedEditChanges()) return
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
        val captureId = content.card.analysisResult.captureId
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
                    captureMutationRepository.updateCapture(
                        captureId = captureId,
                        title = normalized.title,
                        summary = normalized.summary,
                        body = normalized.body,
                        typeCode = normalized.contentType,
                    )
                }
            } catch (cancellation: CancellationException) {
                _uiState.updateContent { it.copy(isSaving = false) }
                throw cancellation
            } catch (_: Exception) {
                _uiState.updateContent { it.copy(isSaving = false) }
                eventChannel.send(
                    ScreenshotEvent.SaveFailed(R.string.screenshot_edit_save_error),
                )
                return@launch
            }
            if (updated.isFailure) {
                _uiState.updateContent { it.copy(isSaving = false) }
                eventChannel.send(
                    ScreenshotEvent.SaveFailed(R.string.screenshot_edit_save_error),
                )
                return@launch
            }
            _uiState.updateContent { it.copy(isSaving = false) }
            eventChannel.send(ScreenshotEvent.SaveSucceeded)
        }
    }

    private fun deleteScreenshot() {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isDeleting || content.isSaving) return
        val captureId = content.card.analysisResult.captureId
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            _uiState.updateContent {
                it.copy(
                    isDeleting = true,
                    showDeleteConfirmDialog = false,
                    actionErrorMessageResId = null,
                )
            }
            val result = try {
                withContext(ioDispatcher) {
                    captureMutationRepository.deleteCaptures(setOf(captureId))
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
            val deleteResult = result.getOrNull()
            if (result.isFailure ||
                deleteResult == null ||
                !deleteResult.deletedIds.contains(captureId)
            ) {
                _uiState.updateContent {
                    it.copy(
                        isDeleting = false,
                        actionErrorMessageResId = R.string.screenshot_detail_delete_error,
                    )
                }
                return@launch
            }
            eventChannel.send(ScreenshotEvent.DeleteSucceeded)
        }
    }

    private fun submitReport(
        reason: ReportReason,
        detail: String?,
    ) {
        val content = _uiState.value as? ScreenshotUiState.Content ?: return
        if (content.isReporting || content.isDeleting || content.isSaving) return
        val captureId = content.card.analysisResult.captureId
        val normalizedDetail = detail?.trim()?.takeIf { it.isNotEmpty() }
        reportJob?.cancel()
        reportJob = viewModelScope.launch {
            _uiState.updateContent {
                it.copy(
                    isReporting = true,
                    actionErrorMessageResId = null,
                )
            }
            val result = try {
                withContext(ioDispatcher) {
                    captureMutationRepository.report(
                        captureId = captureId,
                        reason = reason,
                        detail = normalizedDetail,
                    )
                }
            } catch (cancellation: CancellationException) {
                _uiState.updateContent { it.copy(isReporting = false) }
                throw cancellation
            } catch (_: Exception) {
                _uiState.updateContent { it.copy(isReporting = false) }
                eventChannel.send(
                    ScreenshotEvent.ReportFailed(R.string.screenshot_report_error_toast),
                )
                return@launch
            }
            _uiState.updateContent { it.copy(isReporting = false) }
            if (result.isSuccess) {
                eventChannel.send(ScreenshotEvent.ReportSucceeded)
                return@launch
            }
            val apiCode = (result.exceptionOrNull() as? RemoteApiException)?.code
            val messageResId = when (apiCode) {
                "ALREADY_REPORTED" -> R.string.screenshot_report_already_reported_toast
                else -> R.string.screenshot_report_error_toast
            }
            eventChannel.send(
                ScreenshotEvent.ReportFailed(
                    messageResId = messageResId,
                    dismissSheet = apiCode == "ALREADY_REPORTED",
                ),
            )
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
