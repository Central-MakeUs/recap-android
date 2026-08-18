package com.chalkak.recap.feature.home.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.capture.CaptureThumbnailUpdates
import com.chalkak.recap.core.data.home.RecentCapturesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentOrganizedScreenshotsViewModel @Inject constructor(
    private val recentCapturesRepository: RecentCapturesRepository,
    private val captureMutationRepository: CaptureMutationRepository,
    private val thumbnailUpdates: CaptureThumbnailUpdates,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentOrganizedScreenshotsUiState())
    val uiState: StateFlow<RecentOrganizedScreenshotsUiState> = _uiState.asStateFlow()

    private val _events =
        MutableSharedFlow<RecentOrganizedScreenshotsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<RecentOrganizedScreenshotsEvent> = _events.asSharedFlow()

    private val refreshKey = MutableStateFlow(0)
    private var loadMoreJob: Job? = null

    init {
        observeFirstPage()
        viewModelScope.launch {
            thumbnailUpdates.thumbnailReady.collect { ready ->
                applyThumbnailReady(ready.captureId, ready.localPath)
            }
        }
    }

    fun onAction(action: RecentOrganizedScreenshotsAction) {
        when (action) {
            RecentOrganizedScreenshotsAction.LoadMore -> loadMore()
            RecentOrganizedScreenshotsAction.Retry -> retry()
            is RecentOrganizedScreenshotsAction.ToggleFavorite -> toggleFavorite(action.id)
            is RecentOrganizedScreenshotsAction.RequestDeleteItem -> requestDeleteItem(action.id)
            RecentOrganizedScreenshotsAction.ConfirmDeleteItem -> confirmDeleteItem()
            RecentOrganizedScreenshotsAction.DismissDeleteItem -> dismissDeleteItem()
            else -> Unit
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFirstPage() {
        viewModelScope.launch {
            refreshKey
                .flatMapLatest {
                    recentCapturesRepository.observeRecentCaptures(page = 0)
                }
                .collect { result ->
                    loadMoreJob?.cancel()
                    result.fold(
                        onSuccess = { page ->
                            val items = page.toRecentOrganizedScreenshotItems()
                            val pending = PendingRecentPage(
                                items = items,
                                resultCount = page.count,
                                hasNext = page.hasNext,
                            )
                            applyFirstPage(pending)
                        },
                        onFailure = {
                            _uiState.update { state ->
                                when (state.phase) {
                                    RecentOrganizedScreenshotsPhase.Content,
                                    RecentOrganizedScreenshotsPhase.Empty,
                                    -> state.copy(isLoadingMore = false)
                                    RecentOrganizedScreenshotsPhase.Loading,
                                    RecentOrganizedScreenshotsPhase.Error,
                                    -> state.copy(
                                        phase = RecentOrganizedScreenshotsPhase.Error,
                                        items = emptyList(),
                                        resultCount = 0L,
                                        hasNext = false,
                                        nextPage = 0,
                                        isLoadingMore = false,
                                    )
                                }
                            }
                        },
                    )
                }
        }
    }

    private fun retry() {
        loadMoreJob?.cancel()
        _uiState.update { state ->
            state.copy(
                phase = RecentOrganizedScreenshotsPhase.Loading,
                isLoadingMore = false,
                hasNext = false,
                nextPage = 0,
                pendingDeleteCaptureId = null,
            )
        }
        refreshKey.update { value -> value + 1 }
    }

    private fun loadMore() {
        val state = _uiState.value
        if (
            state.phase != RecentOrganizedScreenshotsPhase.Content ||
            !state.hasNext ||
            state.isLoadingMore
        ) {
            return
        }
        if (loadMoreJob?.isActive == true) {
            return
        }

        val pageToLoad = state.nextPage
        loadMoreJob = viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoadingMore = true) }

            val result = recentCapturesRepository.getRecentCaptures(page = pageToLoad)

            result.fold(
                onSuccess = { page ->
                    val newItems = page.toRecentOrganizedScreenshotItems()
                    _uiState.update { current ->
                        val merged = (current.items + newItems)
                            .distinctBy { item -> item.id }
                        current.copy(
                            items = merged,
                            resultCount = page.count,
                            hasNext = page.hasNext,
                            nextPage = pageToLoad + 1,
                            isLoadingMore = false,
                        )
                    }
                    reconcileThumbnails(newItems.map { item -> item.id })
                },
                onFailure = {
                    _uiState.update { current ->
                        current.copy(isLoadingMore = false)
                    }
                },
            )
        }
    }

    private fun reconcileThumbnails(captureIds: Iterable<Long>) {
        captureIds.forEach { captureId ->
            thumbnailUpdates.resolveLocalPath(captureId)?.let { path ->
                applyThumbnailReady(captureId, path)
            }
        }
    }

    private fun applyThumbnailReady(
        captureId: Long,
        localPath: String,
    ) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == captureId) {
                        item.copy(thumbnailModel = localPath)
                    } else {
                        item
                    }
                },
            )
        }
    }

    private fun requestDeleteItem(id: Long) {
        if (_uiState.value.items.none { item -> item.id == id }) {
            return
        }
        _uiState.update { state -> state.copy(pendingDeleteCaptureId = id) }
    }

    private fun dismissDeleteItem() {
        if (_uiState.value.pendingDeleteCaptureId == null) {
            return
        }
        _uiState.update { state -> state.copy(pendingDeleteCaptureId = null) }
    }

    private fun confirmDeleteItem() {
        val captureId = _uiState.value.pendingDeleteCaptureId ?: return
        _uiState.update { state -> state.copy(pendingDeleteCaptureId = null) }
        viewModelScope.launch {
            val result = captureMutationRepository.deleteCaptures(setOf(captureId))
            val deleteResult = result.getOrNull()
            if (result.isFailure || deleteResult == null || captureId !in deleteResult.deletedIds) {
                _events.emit(RecentOrganizedScreenshotsEvent.ShowDeleteFailureToast)
                return@launch
            }
            _uiState.update { state ->
                val remaining = state.items.filterNot { item -> item.id == captureId }
                val removedCount = state.items.size - remaining.size
                if (removedCount == 0) {
                    return@update state
                }
                state.copy(
                    items = remaining,
                    resultCount = (state.resultCount - removedCount).coerceAtLeast(0L),
                    phase = recentPhaseAfterRemoval(
                        remainingIsEmpty = remaining.isEmpty(),
                        currentPhase = state.phase,
                    ),
                )
            }
            _events.emit(RecentOrganizedScreenshotsEvent.ShowDeleteSuccessToast)
        }
    }

    private fun toggleFavorite(id: Long) {
        val currentItem = _uiState.value.items.firstOrNull { item ->
            item.id == id
        } ?: return
        val nextFavorite = !currentItem.isFavorite

        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == id) {
                        item.copy(isFavorite = nextFavorite)
                    } else {
                        item
                    }
                },
            )
        }

        viewModelScope.launch {
            val mutation = captureMutationRepository.updateFavorite(
                captureId = id,
                isFavorite = nextFavorite,
            )
            if (mutation.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { item ->
                            if (item.id == id) {
                                item.copy(isFavorite = currentItem.isFavorite)
                            } else {
                                item
                            }
                        },
                    )
                }
            }
        }
    }

    private fun applyFirstPage(page: PendingRecentPage) {
        loadMoreJob?.cancel()
        _uiState.update { state ->
            state.copy(
                phase = recentPhaseAfterRemoval(
                    remainingIsEmpty = page.items.isEmpty(),
                    currentPhase = state.phase,
                ),
                items = page.items,
                resultCount = page.resultCount,
                hasNext = page.hasNext,
                nextPage = 1,
                isLoadingMore = false,
            )
        }
        reconcileThumbnails(page.items.map { item -> item.id })
    }

    private fun recentPhaseAfterRemoval(
        remainingIsEmpty: Boolean,
        currentPhase: RecentOrganizedScreenshotsPhase,
    ): RecentOrganizedScreenshotsPhase {
        if (!remainingIsEmpty) {
            return RecentOrganizedScreenshotsPhase.Content
        }
        return if (currentPhase == RecentOrganizedScreenshotsPhase.Content) {
            RecentOrganizedScreenshotsPhase.Content
        } else {
            RecentOrganizedScreenshotsPhase.Empty
        }
    }
}

private data class PendingRecentPage(
    val items: List<RecentOrganizedScreenshotUiModel>,
    val resultCount: Long,
    val hasNext: Boolean,
)
