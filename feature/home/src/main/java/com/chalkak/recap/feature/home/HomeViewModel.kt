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
class HomeViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            screenshotCardRepository.observeStoredCards().collect { cards ->
                _uiState.value = cards.toHomeUiState()
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.ToggleFavoriteItem -> {
                val currentItem = _uiState.value.favoriteItems.firstOrNull { item ->
                    item.id == action.id
                } ?: return
                viewModelScope.launch {
                    runCatching {
                        screenshotCardRepository.updateFavorite(
                            imageId = action.id,
                            isFavorite = !currentItem.isFavorite,
                        )
                    }
                }
            }

            else -> Unit
        }
    }
}
