package com.chalkak.recap.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.home.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val captureMutationRepository: CaptureMutationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            homeRepository.observeSummary().collect { summary ->
                _uiState.value = summary.toHomeUiState()
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
