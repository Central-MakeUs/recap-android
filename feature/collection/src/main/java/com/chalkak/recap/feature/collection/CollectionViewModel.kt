package com.chalkak.recap.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
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
    private var searchQuery: String = ""
    private var selectedTab: CollectionTab = CollectionTab.Favorites
    private var typeViewMode: CollectionTypeViewMode = CollectionTypeViewMode.Grid
    private var othersSort: CollectionListSort = CollectionListSort.Latest
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
            is CollectionAction.UpdateSearchQuery -> {
                searchQuery = action.query
                publishState()
            }

            is CollectionAction.SelectTab -> {
                selectedTab = action.tab
                publishState()
            }

            is CollectionAction.SetTypeViewMode -> {
                typeViewMode = action.viewMode
                publishState()
            }

            is CollectionAction.SetOthersSort -> {
                othersSort = action.sort
                publishState()
            }

            CollectionAction.OpenFavoriteDetail -> {
                detailFilter = CollectionDetailFilter.Favorites
                detailSort = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.OpenFavoriteItem -> {
                // TODO: Connect favorite item destination when detail route is defined.
            }

            is CollectionAction.OpenOtherItem -> {
                // TODO: Connect other item destination when detail route is defined.
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
        val overview = storedCards.toOverviewUiModel(
            searchQuery = searchQuery,
            othersSort = othersSort,
        )
        val detail = detailFilter?.let { filter ->
            storedCards.toDetailUiModel(filter = filter, sort = detailSort)
        }

        _uiState.update {
            CollectionUiState(
                isLoading = !hasReceivedFirstEmission,
                hasStoredScreenshots = hasStoredScreenshots,
                searchQuery = searchQuery,
                selectedTab = selectedTab,
                typeViewMode = typeViewMode,
                othersSort = othersSort,
                overview = overview,
                detail = detail,
            )
        }
    }
}
