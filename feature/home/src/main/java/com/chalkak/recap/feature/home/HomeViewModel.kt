package com.chalkak.recap.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.home.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val captureMutationRepository: CaptureMutationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val refreshKey = MutableStateFlow(0)

    init {
        observeSummary()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.RetryLoad -> refreshKey.update { value -> value + 1 }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSummary() {
        viewModelScope.launch {
            refreshKey
                .flatMapLatest { homeRepository.observeSummary() }
                .collect { result ->
                    result.fold(
                        onSuccess = { summary ->
                            _uiState.value = summary.toHomeUiState().copy(
                                phase = HomeContentPhase.Content,
                            )
                        },
                        onFailure = {
                            _uiState.value = HomeUiState(phase = HomeContentPhase.Error)
                        },
                    )
                }
        }
    }
}
