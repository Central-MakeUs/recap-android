package com.chalkak.recap.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RecentOrganizedScreenshotsViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecentOrganizedScreenshotsUiState())
    val uiState: StateFlow<RecentOrganizedScreenshotsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            screenshotCardRepository.observeStoredCards().collect { cards ->
                _uiState.value = cards.toRecentOrganizedScreenshotsUiState()
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
                    runCatching {
                        screenshotCardRepository.updateFavorite(
                            captureId = action.id,
                            isFavorite = !currentItem.isFavorite,
                        )
                    }
                }
            }

            else -> Unit
        }
    }
}
