package com.chalkak.recap.feature.home.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.home.RecentCapturesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RecentOrganizedScreenshotsViewModel @Inject constructor(
    private val recentCapturesRepository: RecentCapturesRepository,
    private val captureMutationRepository: CaptureMutationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentOrganizedScreenshotsUiState())
    val uiState: StateFlow<RecentOrganizedScreenshotsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recentCapturesRepository.observeRecentCaptures().collect { captures ->
                _uiState.value = captures.toRecentOrganizedScreenshotsUiState()
            }
        }
    }

    fun onAction(action: RecentOrganizedScreenshotsAction) {
        when (action) {
            is RecentOrganizedScreenshotsAction.ToggleFavorite -> {
                val currentItem = _uiState.value.items.firstOrNull { item ->
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
}
