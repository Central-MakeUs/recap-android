package com.chalkak.recap.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private var storedCards: List<StoredScreenshotCard> = emptyList()
    private var detailFilter: CollectionDetailFilter? = null
    private var detailSort: CollectionListSort = CollectionListSort.Latest
    private var hasReceivedFirstEmission = false

    init {
        viewModelScope.launch {
            screenshotCardRepository.observeStoredCards().collect { cards ->
                hasReceivedFirstEmission = true
                storedCards = cards
                publishState()
            }
        }
    }

    fun onAction(action: CollectionAction) {
        when (action) {
            CollectionAction.OpenFavoriteDetail -> {
                detailFilter = CollectionDetailFilter.Favorites
                detailSort = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.OpenTypeDetail -> {
                detailFilter = CollectionDetailFilter.ByType(action.contentType)
                detailSort = CollectionListSort.Latest
                publishState()
            }

            CollectionAction.CloseDetail -> {
                detailFilter = null
                detailSort = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.SetDetailSort -> {
                detailSort = action.sort
                publishState()
            }

            is CollectionAction.ToggleFavorite -> {
                val currentCard = storedCards.firstOrNull { card ->
                    card.analysisResult.imageId == action.imageId
                } ?: return
                viewModelScope.launch {
                    runCatching {
                        screenshotCardRepository.updateFavorite(
                            imageId = action.imageId,
                            isFavorite = !currentCard.analysisResult.isFavorite,
                        )
                    }
                }
            }
        }
    }

    private fun publishState() {
        val hasStoredScreenshots = storedCards.isNotEmpty()
        val overview = storedCards.toOverviewUiModel()
        val detail = detailFilter?.let { filter ->
            storedCards.toDetailUiModel(filter = filter, sort = detailSort)
        }

        _uiState.update {
            CollectionUiState(
                isLoading = !hasReceivedFirstEmission,
                hasStoredScreenshots = hasStoredScreenshots,
                overview = overview,
                detail = detail,
            )
        }
    }
}
