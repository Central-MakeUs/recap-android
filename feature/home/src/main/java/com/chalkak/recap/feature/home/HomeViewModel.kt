package com.chalkak.recap.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.capture.CaptureThumbnailUpdates
import com.chalkak.recap.core.data.home.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val captureMutationRepository: CaptureMutationRepository,
    private val thumbnailUpdates: CaptureThumbnailUpdates,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSummary()
        viewModelScope.launch {
            thumbnailUpdates.thumbnailReady.collect { ready ->
                applyThumbnailReady(ready.captureId, ready.localPath)
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.RetryLoad -> homeRepository.refreshSummary()
            is HomeAction.ToggleFavoriteItem -> {
                val currentItem = _uiState.value.favoriteItems.firstOrNull { item ->
                    item.id == action.id
                } ?: return
                viewModelScope.launch {
                    captureMutationRepository.updateFavorite(
                        captureId = action.id,
                        isFavorite = !currentItem.isFavorite,
                    )
                }
            }

            else -> Unit
        }
    }

    private fun observeSummary() {
        viewModelScope.launch {
            homeRepository.observeSummary().collect { result ->
                result.fold(
                    onSuccess = { summary ->
                        val homeState = summary.toHomeUiState().copy(
                            phase = HomeContentPhase.Content,
                        )
                        _uiState.value = homeState
                        reconcileThumbnails(
                            (homeState.recentScreenshots.map { it.id } +
                                homeState.favoriteItems.map { it.id }).distinct(),
                        )
                    },
                    onFailure = {
                        _uiState.value = HomeUiState(phase = HomeContentPhase.Error)
                    },
                )
            }
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
                recentScreenshots = state.recentScreenshots.map { item ->
                    if (item.id == captureId) {
                        item.copy(thumbnailModel = localPath)
                    } else {
                        item
                    }
                },
                favoriteItems = state.favoriteItems.map { item ->
                    if (item.id == captureId) {
                        item.copy(thumbnailModel = localPath)
                    } else {
                        item
                    }
                },
            )
        }
    }
}
