package com.chalkak.recap.feature.organize

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.data.screenshot.image.ScreenshotUploadPreparer
import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OrganizeViewModel @Inject constructor(
    private val localScreenshotDataSource: LocalScreenshotDataSource,
    private val userRepository: UserRepository,
    private val screenshotUploadPreparer: ScreenshotUploadPreparer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val restoredShareState = restoreShareState()
    private val _uiState = MutableStateFlow(
        restoredShareState?.uiState ?: OrganizeUiState(),
    )
    val uiState: StateFlow<OrganizeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OrganizeEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<OrganizeEvent> = _events.asSharedFlow()

    private var seededShareSessionId: String? = restoredShareState?.sessionId
    private var sharedSourceImages: List<LocalImage> =
        restoredShareState?.sourceImages.orEmpty()
    private var refreshJob: Job? = null
    private var refreshGeneration = 0L

    private var confirmationGeneration = 0L
    private var isConfirmationActive = false
    private var consentFetchDeferred: CompletableDeferred<Boolean?>? = null
    private var consentFetchJob: Job? = null
    private var startOrganizingJob: Job? = null
    private var submitConsentJob: Job? = null

    private val preparedByUri = LinkedHashMap<String, PreparedScreenshot>()
    private val preparationAttemptsByUri = LinkedHashMap<String, Int>()
    private var preparationJob: Job? = null
    private var preparationGeneration = 0L

    fun onAction(action: OrganizeAction) {
        when (action) {
            is OrganizeAction.ToggleSelection -> toggleSelection(action.uri)
            is OrganizeAction.RemoveSelection -> removeSelection(action.uri)
            OrganizeAction.ClearSelection -> clearSelection()
            OrganizeAction.DismissMaxSelectionMessage -> {
                _uiState.update { it.copy(showMaxSelectionReached = false) }
            }
            OrganizeAction.StartOrganizing -> startOrganizing()
            OrganizeAction.AgreeAiDataTransferConsent -> agreeAiDataTransferConsent()
            OrganizeAction.DismissAiDataTransferConsent -> dismissAiDataTransferConsent()
        }
    }

    fun onConfirmationEntered() {
        confirmationGeneration += 1
        val generation = confirmationGeneration
        isConfirmationActive = true
        cancelPendingConsentWork()
        _uiState.update {
            it.copy(
                showAiDataTransferConsentSheet = false,
                isConsentSubmitting = false,
            )
        }

        val deferred = CompletableDeferred<Boolean?>()
        consentFetchDeferred = deferred
        consentFetchJob = viewModelScope.launch {
            val consented = userRepository.getConsentStatus()
                .getOrNull()
                ?.consented
            if (isConfirmationActive && generation == confirmationGeneration) {
                deferred.complete(consented)
            }
        }
        reconcilePreparation()
    }

    fun onConfirmationExited() {
        confirmationGeneration += 1
        isConfirmationActive = false
        cancelPendingConsentWork()
        cancelPreparation(clearCache = true)
        _uiState.update {
            it.copy(
                showAiDataTransferConsentSheet = false,
                isConsentSubmitting = false,
            )
        }
    }

    private fun startOrganizing() {
        if (!isConfirmationActive) return
        if (!_uiState.value.canProceed) return
        if (startOrganizingJob?.isActive == true) return
        if (snapshotCandidatesInSelectionOrder() == null) return
        val generation = confirmationGeneration
        val consentDeferred = consentFetchDeferred ?: return
        startOrganizingJob = viewModelScope.launch {
            if (consentDeferred.await() == true) {
                if (!isConfirmationActive || generation != confirmationGeneration) return@launch
                val candidates = takeCandidatesInSelectionOrder() ?: return@launch
                _events.emit(OrganizeEvent.ProceedToOrganize(candidates))
            } else {
                if (!isConfirmationActive || generation != confirmationGeneration) return@launch
                _uiState.update { it.copy(showAiDataTransferConsentSheet = true) }
            }
        }
    }

    private fun agreeAiDataTransferConsent() {
        if (!isConfirmationActive) return
        if (!_uiState.value.showAiDataTransferConsentSheet) return
        if (_uiState.value.isConsentSubmitting) return
        if (submitConsentJob?.isActive == true) return
        if (snapshotCandidatesInSelectionOrder() == null) return
        val generation = confirmationGeneration
        submitConsentJob = viewModelScope.launch {
            _uiState.update { it.copy(isConsentSubmitting = true) }
            val success = runCatching {
                userRepository.giveConsent().isSuccess
            }.getOrDefault(false)
            if (!isConfirmationActive || generation != confirmationGeneration) return@launch
            if (success) {
                consentFetchDeferred = CompletableDeferred(true)
                _uiState.update {
                    it.copy(
                        showAiDataTransferConsentSheet = false,
                        isConsentSubmitting = false,
                    )
                }
                val candidates = takeCandidatesInSelectionOrder() ?: return@launch
                _events.emit(OrganizeEvent.ProceedToOrganize(candidates))
            } else {
                _uiState.update { it.copy(isConsentSubmitting = false) }
            }
        }
    }

    private fun dismissAiDataTransferConsent() {
        if (!isConfirmationActive) return
        _uiState.update {
            it.copy(
                showAiDataTransferConsentSheet = false,
                isConsentSubmitting = false,
            )
        }
    }

    private fun cancelPendingConsentWork() {
        consentFetchJob?.cancel()
        consentFetchJob = null
        consentFetchDeferred?.cancel()
        consentFetchDeferred = null
        startOrganizingJob?.cancel()
        startOrganizingJob = null
        submitConsentJob?.cancel()
        submitConsentJob = null
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
        if (isConfirmationActive) {
            reconcilePreparation()
        }
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
            if (isConfirmationActive) {
                reconcilePreparation()
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
            if (isConfirmationActive) {
                reconcilePreparation()
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
        persistShareState()
        if (isConfirmationActive) {
            reconcilePreparation()
        }
    }

    private fun removeSelection(uri: String) {
        _uiState.update { state ->
            state.copy(selectedUris = state.selectedUris.filterNot { it == uri })
        }
        persistShareState()
        if (isConfirmationActive) {
            reconcilePreparation()
        }
    }

    private fun clearSelection() {
        invalidateRefresh()
        cancelPreparation(clearCache = true)
        _uiState.update { state ->
            state.copy(
                selectedUris = emptyList(),
                showMaxSelectionReached = false,
                showAiDataTransferConsentSheet = false,
                isConsentSubmitting = false,
            )
        }
        seededShareSessionId = null
        sharedSourceImages = emptyList()
        clearPersistedShareState()
    }

    private fun reconcilePreparation() {
        if (!isConfirmationActive) return
        val selectedUris = _uiState.value.selectedUris
        val selectedSet = selectedUris.toHashSet()
        preparedByUri.keys
            .filter { uri -> uri !in selectedSet }
            .forEach { uri -> preparedByUri.remove(uri) }
        preparationAttemptsByUri.keys
            .filter { uri -> uri !in selectedSet }
            .forEach { uri -> preparationAttemptsByUri.remove(uri) }

        val missingUris = selectedUris.filter { uri ->
            uri !in preparedByUri && preparationAttemptsByUri[uri] == null
        }
        if (missingUris.isEmpty()) {
            cancelPreparation(clearCache = false)
            return
        }

        preparationJob?.cancel()
        preparationGeneration += 1
        val generation = preparationGeneration
        preparationJob = viewModelScope.launch {
            for (uri in missingUris) {
                if (!isConfirmationActive || generation != preparationGeneration) return@launch
                val image = _uiState.value.availableScreenshots.find { screenshot ->
                    screenshot.uri == uri
                }
                if (image == null) {
                    preparationAttemptsByUri[uri] = 1
                    continue
                }
                preparationAttemptsByUri[uri] = 1
                try {
                    val prepared = screenshotUploadPreparer.prepare(image)
                    if (!isConfirmationActive || generation != preparationGeneration) return@launch
                    preparedByUri[uri] = prepared
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    if (!isConfirmationActive || generation != preparationGeneration) return@launch
                }
            }
        }
    }

    private fun cancelPreparation(clearCache: Boolean) {
        preparationJob?.cancel()
        preparationJob = null
        preparationGeneration += 1
        if (clearCache) {
            preparedByUri.clear()
            preparationAttemptsByUri.clear()
        }
    }

    private fun snapshotCandidatesInSelectionOrder(): List<ScreenshotUploadCandidate>? {
        val selectedUris = _uiState.value.selectedUris
        if (selectedUris.isEmpty()) return null
        val candidates = ArrayList<ScreenshotUploadCandidate>(selectedUris.size)
        for (uri in selectedUris) {
            val image = _uiState.value.availableScreenshots.find { it.uri == uri } ?: return null
            candidates += ScreenshotUploadCandidate(
                localImage = image,
                preparedScreenshot = preparedByUri[uri],
                completedPreparationAttempts = preparationAttemptsByUri[uri] ?: 0,
            )
        }
        return candidates
    }

    private fun takeCandidatesInSelectionOrder(): List<ScreenshotUploadCandidate>? {
        val candidates = snapshotCandidatesInSelectionOrder() ?: return null
        cancelPreparation(clearCache = false)
        return candidates
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
