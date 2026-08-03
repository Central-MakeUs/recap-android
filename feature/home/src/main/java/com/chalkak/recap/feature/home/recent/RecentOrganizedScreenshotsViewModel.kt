package com.chalkak.recap.feature.home.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.capture.CaptureThumbnailUpdates
import com.chalkak.recap.core.data.home.RecentCapturesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecentOrganizedScreenshotsViewModel @Inject constructor(
    private val recentCapturesRepository: RecentCapturesRepository,
    private val captureMutationRepository: CaptureMutationRepository,
    private val thumbnailUpdates: CaptureThumbnailUpdates,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentOrganizedScreenshotsUiState())
    val uiState: StateFlow<RecentOrganizedScreenshotsUiState> = _uiState.asStateFlow()

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
                            _uiState.update { state ->
                                state.copy(
                                    phase = if (items.isEmpty()) {
                                        RecentOrganizedScreenshotsPhase.Empty
                                    } else {
                                        RecentOrganizedScreenshotsPhase.Content
                                    },
                                    items = items,
                                    resultCount = page.count,
                                    hasNext = page.hasNext,
                                    nextPage = 1,
                                    isLoadingMore = false,
                                )
                            }
                            reconcileThumbnails(items.map { item -> item.id })
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
}
