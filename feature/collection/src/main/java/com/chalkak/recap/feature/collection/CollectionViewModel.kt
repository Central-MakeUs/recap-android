package com.chalkak.recap.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val screenshotImageStorage: ScreenshotImageStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CollectionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CollectionEvent> = _events.asSharedFlow()

    private var storedCards: List<StoredScreenshotCard> = emptyList()
    private var detailFilter: CollectionDetailFilter? = null
    private var detailSort: CollectionListSort = CollectionListSort.Latest
    private var searchQuery: String = ""
    private var detailSearchQuery: String = ""
    private var isDetailSearchVisible: Boolean = false
    private var typeViewMode: CollectionTypeViewMode = CollectionTypeViewMode.Grid
    private var selection = CollectionSelectionUiState()
    private var selectionGeneration = 0L
    private var hasReceivedFirstEmission = false

    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

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
                if (searchQuery != action.query) {
                    clearSelection()
                }
                searchQuery = action.query
                publishState()
            }

            CollectionAction.ShowDetailSearch -> {
                isDetailSearchVisible = true
                publishState()
            }

            CollectionAction.HideDetailSearch -> {
                clearDetailSearch()
                publishState()
            }

            is CollectionAction.UpdateDetailSearchQuery -> {
                if (detailSearchQuery != action.query) {
                    clearSelection()
                }
                detailSearchQuery = action.query
                publishState()
            }

            is CollectionAction.SetTypeViewMode -> {
                typeViewMode = action.viewMode
                publishState()
            }

            CollectionAction.OpenFavoriteDetail -> {
                clearSelection()
                clearDetailSearch()
                detailFilter = CollectionDetailFilter.Favorites
                detailSort = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.OpenFavoriteItem -> Unit

            is CollectionAction.OpenTypeDetail -> {
                clearSelection()
                clearDetailSearch()
                detailFilter = CollectionDetailFilter.ByType(action.contentType)
                detailSort = CollectionListSort.Latest
                publishState()
            }

            CollectionAction.CloseDetail -> {
                clearSelection()
                clearDetailSearch()
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

            CollectionAction.StartSelection -> {
                selectionGeneration += 1
                selection = CollectionSelectionUiState(isActive = true)
                publishState()
            }

            CollectionAction.CancelSelection -> {
                clearSelection()
                publishState()
            }

            is CollectionAction.ToggleItemSelection -> {
                if (!selection.isActive || selection.isDeleting) {
                    return
                }
                val storedImageIds = storedCards.mapTo(mutableSetOf()) { card ->
                    card.analysisResult.imageId
                }
                if (action.imageId !in storedImageIds) {
                    return
                }
                val selectedImageIds = selection.selectedImageIds.toMutableSet().apply {
                    if (!add(action.imageId)) {
                        remove(action.imageId)
                    }
                }
                selection = selection.copy(selectedImageIds = selectedImageIds)
                publishState()
            }

            CollectionAction.DeleteSelected -> showDeleteConfirmDialog()
            CollectionAction.ConfirmDeleteSelected -> deleteSelectedCards()
            CollectionAction.DismissDeleteConfirmDialog -> dismissDeleteConfirmDialog()
        }
    }

    private fun showDeleteConfirmDialog() {
        if (!selection.isActive || selection.isDeleting || selection.selectedCount == 0) {
            return
        }
        selection = selection.copy(showDeleteConfirmDialog = true)
        publishState()
    }

    private fun dismissDeleteConfirmDialog() {
        if (!selection.showDeleteConfirmDialog || selection.isDeleting) {
            return
        }
        selection = selection.copy(showDeleteConfirmDialog = false)
        publishState()
    }

    private fun deleteSelectedCards() {
        if (!selection.isActive || selection.isDeleting) {
            return
        }
        val storedImageIds = storedCards.mapTo(mutableSetOf()) { card ->
            card.analysisResult.imageId
        }
        val imageIds = selection.selectedImageIds.intersect(storedImageIds)
        if (imageIds.isEmpty()) {
            selection = selection.copy(showDeleteConfirmDialog = false)
            publishState()
            return
        }

        selection = selection.copy(
            isDeleting = true,
            showDeleteConfirmDialog = false,
        )
        val deleteGeneration = selectionGeneration
        publishState()
        viewModelScope.launch {
            try {
                screenshotCardRepository.deleteCards(imageIds)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                if (selectionGeneration == deleteGeneration) {
                    selection = selection.copy(isDeleting = false)
                    publishState()
                }
                return@launch
            }

            _events.emit(CollectionEvent.ShowDeleteSuccessToast(deletedCount = imageIds.size))

            try {
                withContext(ioDispatcher + NonCancellable) {
                    screenshotImageStorage.deleteStoredImages(imageIds)
                }
            } catch (_: Exception) {
                // Room is already committed; private file cleanup remains best-effort.
            } finally {
                if (selectionGeneration == deleteGeneration) {
                    clearSelection()
                    publishState()
                }
            }
        }
    }

    private fun clearSelection() {
        selectionGeneration += 1
        selection = CollectionSelectionUiState()
    }

    private fun clearDetailSearch() {
        detailSearchQuery = ""
        isDetailSearchVisible = false
    }

    private fun publishState() {
        val storedImageIds = storedCards.mapTo(mutableSetOf()) { card ->
            card.analysisResult.imageId
        }
        if (selection.isActive) {
            selection = selection.copy(
                selectedImageIds = selection.selectedImageIds.intersect(storedImageIds),
            )
        }
        val hasStoredScreenshots = storedCards.isNotEmpty()
        val overview = storedCards.toOverviewUiModel(searchQuery = searchQuery)
        val detail = detailFilter?.let { filter ->
            storedCards.toDetailUiModel(
                filter = filter,
                sort = detailSort,
                searchQuery = detailSearchQuery,
            )
        }

        _uiState.update {
            CollectionUiState(
                isLoading = !hasReceivedFirstEmission,
                hasStoredScreenshots = hasStoredScreenshots,
                searchQuery = searchQuery,
                detailSearchQuery = detailSearchQuery,
                isDetailSearchVisible = isDetailSearchVisible,
                typeViewMode = typeViewMode,
                overview = overview,
                detail = detail,
                selection = selection,
            )
        }
    }
}
