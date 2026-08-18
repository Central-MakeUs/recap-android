package com.chalkak.recap.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.capture.CaptureThumbnailUpdates
import com.chalkak.recap.core.data.network.MainContentRecoveryTrigger
import com.chalkak.recap.core.data.search.SearchRepository
import com.chalkak.recap.core.data.storage.StorageRepository
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchScope
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val searchRepository: SearchRepository,
    private val captureMutationRepository: CaptureMutationRepository,
    private val thumbnailUpdates: CaptureThumbnailUpdates,
    private val mainContentRecoveryTrigger: MainContentRecoveryTrigger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CollectionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CollectionEvent> = _events.asSharedFlow()

    private val searchQuery = MutableStateFlow("")
    private val detailSearchQuery = MutableStateFlow("")
    private val detailFilter = MutableStateFlow<CollectionDetailFilter?>(null)
    private val detailSort = MutableStateFlow(CollectionListSort.Latest)
    private val isDetailSearchVisible = MutableStateFlow(false)
    private val typeViewMode = MutableStateFlow(CollectionTypeViewMode.Grid)
    private val selection = MutableStateFlow(CollectionSelectionUiState())
    private val pendingDeleteCaptureId = MutableStateFlow<Long?>(null)
    private var selectionGeneration = 0L

    private var latestOverview = StorageOverview(
        hasAnyCapture = false,
        favoriteCount = 0,
        types = emptyList(),
    )
    private var latestDetailCards: CaptureList? = null
    private var publishedDetailCards: CaptureList? = null
    private var detailCaptureIds: Set<Long> = emptySet()
    private var favoriteStates: Map<Long, Boolean> = emptyMap()
    private var hasReceivedFirstOverview = false
    private var overviewLoadFailed = false
    private var pendingAutoRetryOnFailure = true

    private var isDetailSearchMode = false
    private var detailSubmittedQuery = ""
    private var detailSearchCards: List<CollectionCardItemUiModel> = emptyList()
    private var detailSearchCount = 0
    private var detailSearchHasNext = false
    private var detailSearchNextPage = 0
    private var detailSearchLoadingMore = false
    private var detailSearchJob: Job? = null
    private var detailLoadMoreJob: Job? = null

    init {
        viewModelScope.launch {
            thumbnailUpdates.thumbnailReady.collect { ready ->
                applyThumbnailReady(ready.captureId, ready.localPath)
            }
        }

        viewModelScope.launch {
            mainContentRecoveryTrigger.recoveries.collect {
                if (overviewLoadFailed) {
                    storageRepository.refreshOverview()
                }
            }
        }

        viewModelScope.launch {
            // Overview search is intentionally non-functional; always observe unfiltered.
            storageRepository.observeOverview(searchQuery = "").collect { result ->
                result.fold(
                    onSuccess = { overview ->
                        pendingAutoRetryOnFailure = false
                        overviewLoadFailed = false
                        hasReceivedFirstOverview = true
                        latestOverview = overview
                        publishState()
                    },
                    onFailure = {
                        if (pendingAutoRetryOnFailure) {
                            pendingAutoRetryOnFailure = false
                            storageRepository.refreshOverview()
                        } else {
                            overviewLoadFailed = true
                            hasReceivedFirstOverview = true
                            publishState()
                        }
                    },
                )
            }
        }

        viewModelScope.launch {
            combine(detailFilter, detailSort) { filter, sort -> filter to sort }
                .flatMapLatest { (filter, sort) ->
                    if (filter == null) {
                        flowOf(null)
                    } else {
                        observeDetail(filter = filter, sort = sort)
                    }
                }.collect { detail ->
                    latestDetailCards = detail
                    if (!isDetailSearchMode) {
                        applyObservedDetailCards(detail)
                    }
                    publishState()
                    detail?.items?.map { it.captureId }?.let { ids ->
                        reconcileThumbnails(ids)
                    }
                }
        }
    }

    fun onAction(action: CollectionAction) {
        when (action) {
            is CollectionAction.UpdateSearchQuery -> {
                // Overview search bar is visual-only.
                searchQuery.value = action.query
                publishState()
            }

            CollectionAction.OpenSearch -> Unit

            CollectionAction.RetryLoad -> storageRepository.refreshOverview()

            CollectionAction.ShowDetailSearch -> {
                isDetailSearchVisible.value = true
                publishState()
            }

            CollectionAction.HideDetailSearch -> {
                clearDetailSearch()
                publishState()
            }

            is CollectionAction.UpdateDetailSearchQuery -> {
                if (detailSearchQuery.value != action.query) {
                    clearSelection()
                }
                detailSearchQuery.value = action.query
                if (action.query.isEmpty()) {
                    exitDetailSearchMode()
                }
                publishState()
            }

            CollectionAction.SubmitDetailSearch -> submitDetailSearch()

            CollectionAction.LoadMoreDetailSearch -> loadMoreDetailSearch()

            is CollectionAction.SetTypeViewMode -> {
                typeViewMode.value = action.viewMode
                publishState()
            }

            CollectionAction.OpenFavoriteDetail -> {
                clearSelection()
                clearDetailSearch()
                pendingDeleteCaptureId.value = null
                detailFilter.value = CollectionDetailFilter.Favorites
                detailSort.value = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.OpenFavoriteItem -> Unit

            is CollectionAction.OpenTypeDetail -> {
                clearSelection()
                clearDetailSearch()
                pendingDeleteCaptureId.value = null
                detailFilter.value = CollectionDetailFilter.ByType(action.contentType)
                detailSort.value = CollectionListSort.Latest
                publishState()
            }

            CollectionAction.CloseDetail -> {
                clearSelection()
                clearDetailSearch()
                pendingDeleteCaptureId.value = null
                detailFilter.value = null
                detailSort.value = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.SetDetailSort -> {
                detailSort.value = action.sort
                publishState()
            }

            is CollectionAction.ToggleFavorite -> {
                val currentFavorite = favoriteStates[action.captureId] ?: return
                val nextFavorite = !currentFavorite
                favoriteStates = favoriteStates + (action.captureId to nextFavorite)
                if (isDetailSearchMode) {
                    detailSearchCards = detailSearchCards.map { card ->
                        if (card.captureId == action.captureId) {
                            card.copy(isFavorite = nextFavorite)
                        } else {
                            card
                        }
                    }
                }
                publishState()
                viewModelScope.launch {
                    val mutation = captureMutationRepository.updateFavorite(
                        captureId = action.captureId,
                        isFavorite = nextFavorite,
                    )
                    if (mutation.isFailure) {
                        favoriteStates = favoriteStates + (action.captureId to currentFavorite)
                        if (isDetailSearchMode) {
                            detailSearchCards = detailSearchCards.map { card ->
                                if (card.captureId == action.captureId) {
                                    card.copy(isFavorite = currentFavorite)
                                } else {
                                    card
                                }
                            }
                        }
                        publishState()
                    } else {
                        _events.emit(CollectionEvent.ShowFavoriteToast(isFavorite = nextFavorite))
                    }
                }
            }

            CollectionAction.StartSelection -> {
                selectionGeneration += 1
                selection.value = CollectionSelectionUiState(isActive = true)
                pendingDeleteCaptureId.value = null
                publishState()
            }

            CollectionAction.CancelSelection -> {
                clearSelection()
                publishState()
            }

            is CollectionAction.ToggleItemSelection -> {
                val current = selection.value
                if (!current.isActive || current.isDeleting) {
                    return
                }
                if (action.captureId !in detailCaptureIds) {
                    return
                }
                val selectedCaptureIds = current.selectedCaptureIds.toMutableSet().apply {
                    if (!add(action.captureId)) {
                        remove(action.captureId)
                    }
                }
                selection.value = current.copy(selectedCaptureIds = selectedCaptureIds)
                publishState()
            }

            CollectionAction.DeleteSelected -> showDeleteConfirmDialog()
            CollectionAction.ConfirmDeleteSelected -> deleteSelectedCards()
            CollectionAction.DismissDeleteConfirmDialog -> dismissDeleteConfirmDialog()
            is CollectionAction.RequestDeleteItem -> requestDeleteItem(action.captureId)
            CollectionAction.ConfirmDeleteItem -> confirmDeleteItem()
            CollectionAction.DismissDeleteItem -> dismissDeleteItem()
        }
    }

    private fun observeDetail(
        filter: CollectionDetailFilter,
        sort: CollectionListSort,
    ) = when (filter) {
        CollectionDetailFilter.Favorites ->
            storageRepository.observeFavoriteCaptures(
                sort = sort.toCaptureSort(),
                searchQuery = "",
            )
        is CollectionDetailFilter.ByType ->
            storageRepository.observeCapturesByType(
                typeCode = filter.contentType,
                sort = sort.toCaptureSort(),
                searchQuery = "",
            )
    }

    private fun submitDetailSearch() {
        val filter = detailFilter.value ?: return
        val query = detailSearchQuery.value.trim()
        if (query.isEmpty()) {
            return
        }

        detailSearchJob?.cancel()
        detailLoadMoreJob?.cancel()
        detailSearchJob = viewModelScope.launch {
            detailSearchQuery.value = query
            isDetailSearchMode = true
            detailSubmittedQuery = query
            detailSearchLoadingMore = false
            publishState()

            searchRepository.observeSearch(
                query = query,
                scope = filter.toSearchScope(),
                typeCode = filter.toTypeCode(),
            ).collect { result ->
                detailLoadMoreJob?.cancel()
                result.fold(
                    onSuccess = { page ->
                    detailSearchCards = page.items.map { item ->
                        item.toCardItemUiModel()
                    }
                    detailSearchCount = page.count.toInt()
                    detailSearchHasNext = page.hasNext
                    detailSearchNextPage = 1
                    detailCaptureIds = detailSearchCards.map { it.captureId }.toSet()
                    favoriteStates = detailSearchCards.associate { it.captureId to it.isFavorite }
                    publishState()
                    reconcileThumbnails(detailSearchCards.map { it.captureId })
                    },
                    onFailure = {
                    detailSearchCards = emptyList()
                    detailSearchCount = 0
                    detailSearchHasNext = false
                    detailSearchNextPage = 0
                    detailCaptureIds = emptySet()
                    favoriteStates = emptyMap()
                    publishState()
                    },
                )
            }
        }
    }

    private fun loadMoreDetailSearch() {
        val filter = detailFilter.value ?: return
        if (
            !isDetailSearchMode ||
            !detailSearchHasNext ||
            detailSearchLoadingMore ||
            detailSubmittedQuery.isBlank()
        ) {
            return
        }
        if (detailLoadMoreJob?.isActive == true) {
            return
        }

        val pageToLoad = detailSearchNextPage
        val query = detailSubmittedQuery
        detailLoadMoreJob = viewModelScope.launch {
            detailSearchLoadingMore = true
            publishState()

            val result = searchRepository.search(
                query = query,
                scope = filter.toSearchScope(),
                typeCode = filter.toTypeCode(),
                page = pageToLoad,
            )

            result.fold(
                onSuccess = { page ->
                    val newCards = page.items.map { item -> item.toCardItemUiModel() }
                    detailSearchCards = (detailSearchCards + newCards)
                        .distinctBy { card -> card.captureId }
                    detailSearchCount = page.count.toInt()
                    detailSearchHasNext = page.hasNext
                    detailSearchNextPage = pageToLoad + 1
                    detailCaptureIds = detailSearchCards.map { it.captureId }.toSet()
                    favoriteStates = detailSearchCards.associate { it.captureId to it.isFavorite }
                    detailSearchLoadingMore = false
                    publishState()
                    reconcileThumbnails(newCards.map { it.captureId })
                },
                onFailure = {
                    detailSearchLoadingMore = false
                    publishState()
                },
            )
        }
    }

    private fun exitDetailSearchMode() {
        detailSearchJob?.cancel()
        detailLoadMoreJob?.cancel()
        isDetailSearchMode = false
        detailSubmittedQuery = ""
        detailSearchCards = emptyList()
        detailSearchCount = 0
        detailSearchHasNext = false
        detailSearchNextPage = 0
        detailSearchLoadingMore = false
        latestDetailCards?.let { detail ->
            publishedDetailCards = detail
            detailCaptureIds = detail.items.map { it.captureId }.toSet()
            favoriteStates = detail.items.associate { it.captureId to it.isFavorite }
        }
    }

    private fun showDeleteConfirmDialog() {
        val current = selection.value
        if (!current.isActive || current.isDeleting || current.selectedCount == 0) {
            return
        }
        selection.value = current.copy(showDeleteConfirmDialog = true)
        publishState()
    }

    private fun dismissDeleteConfirmDialog() {
        val current = selection.value
        if (!current.showDeleteConfirmDialog || current.isDeleting) {
            return
        }
        selection.value = current.copy(showDeleteConfirmDialog = false)
        publishState()
    }

    private fun requestDeleteItem(captureId: Long) {
        if (captureId !in detailCaptureIds) {
            return
        }
        pendingDeleteCaptureId.value = captureId
        publishState()
    }

    private fun dismissDeleteItem() {
        if (pendingDeleteCaptureId.value == null) {
            return
        }
        pendingDeleteCaptureId.value = null
        publishState()
    }

    private fun confirmDeleteItem() {
        val captureId = pendingDeleteCaptureId.value ?: return
        pendingDeleteCaptureId.value = null
        publishState()
        viewModelScope.launch {
            val result = captureMutationRepository.deleteCaptures(setOf(captureId))
            val deleteResult = result.getOrNull()
            if (result.isFailure || deleteResult == null || captureId !in deleteResult.deletedIds) {
                _events.emit(CollectionEvent.ShowDeleteFailureToast)
                return@launch
            }
            if (isDetailSearchMode) {
                val remaining = detailSearchCards.filterNot { card ->
                    card.captureId == captureId
                }
                val removedCount = detailSearchCards.size - remaining.size
                detailSearchCards = remaining
                detailSearchCount = (detailSearchCount - removedCount).coerceAtLeast(0)
                detailCaptureIds = detailSearchCards.map { card -> card.captureId }.toSet()
                favoriteStates = detailSearchCards.associate { card ->
                    card.captureId to card.isFavorite
                }
            }
            _events.emit(CollectionEvent.ShowDeleteSuccessToast(deletedCount = 1))
            publishState()
        }
    }

    private fun deleteSelectedCards() {
        val current = selection.value
        if (!current.isActive || current.isDeleting) {
            return
        }
        val captureIds = current.selectedCaptureIds.intersect(detailCaptureIds)
        if (captureIds.isEmpty()) {
            selection.value = current.copy(showDeleteConfirmDialog = false)
            publishState()
            return
        }

        selection.value = current.copy(
            isDeleting = true,
            showDeleteConfirmDialog = false,
        )
        val deleteGeneration = selectionGeneration
        publishState()
        viewModelScope.launch {
            val result = captureMutationRepository.deleteCaptures(captureIds)
            if (selectionGeneration != deleteGeneration) {
                return@launch
            }
            val deleteResult = result.getOrNull()
            if (result.isFailure || deleteResult == null) {
                selection.value = selection.value.copy(isDeleting = false)
                publishState()
                _events.emit(CollectionEvent.ShowDeleteFailureToast)
                return@launch
            }

            when {
                deleteResult.isFullFailure -> {
                    selection.value = selection.value.copy(isDeleting = false)
                    publishState()
                    _events.emit(CollectionEvent.ShowDeleteFailureToast)
                }

                deleteResult.isPartialSuccess -> {
                    selection.value = selection.value.copy(
                        selectedCaptureIds = deleteResult.failedIds,
                        isDeleting = false,
                        showDeleteConfirmDialog = false,
                    )
                    publishState()
                    _events.emit(
                        CollectionEvent.ShowDeletePartialFailureToast(
                            deletedCount = deleteResult.deletedIds.size,
                            failedCount = deleteResult.failedIds.size,
                        ),
                    )
                }

                else -> {
                    if (isDetailSearchMode) {
                        val remaining = detailSearchCards.filterNot {
                            it.captureId in deleteResult.deletedIds
                        }
                        val removedCount = detailSearchCards.size - remaining.size
                        detailSearchCards = remaining
                        detailSearchCount = (detailSearchCount - removedCount).coerceAtLeast(0)
                        detailCaptureIds = detailSearchCards.map { it.captureId }.toSet()
                        favoriteStates = detailSearchCards.associate { it.captureId to it.isFavorite }
                    }
                    _events.emit(
                        CollectionEvent.ShowDeleteSuccessToast(
                            deletedCount = deleteResult.deletedIds.size,
                        ),
                    )
                    clearSelection()
                    publishState()
                }
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
        var changed = false
        latestDetailCards = latestDetailCards?.let { detail ->
            val updatedItems = detail.items.map { summary ->
                if (summary.captureId == captureId) {
                    changed = true
                    summary.copy(thumbnailUrl = localPath)
                } else {
                    summary
                }
            }
            if (changed) {
                detail.copy(items = updatedItems)
            } else {
                detail
            }
        }
        publishedDetailCards = publishedDetailCards?.let { detail ->
            val updatedItems = detail.items.map { summary ->
                if (summary.captureId == captureId) {
                    changed = true
                    summary.copy(thumbnailUrl = localPath)
                } else {
                    summary
                }
            }
            detail.copy(items = updatedItems)
        }
        val updatedSearchCards = detailSearchCards.map { card ->
            if (card.captureId == captureId) {
                changed = true
                card.copy(thumbnailModel = localPath)
            } else {
                card
            }
        }
        detailSearchCards = updatedSearchCards
        if (changed) {
            publishState()
        }
    }

    private fun applyObservedDetailCards(detail: CaptureList?) {
        val nextIds = detail?.items?.map { summary -> summary.captureId }?.toSet().orEmpty()
        publishedDetailCards = detail
        detailCaptureIds = nextIds
        favoriteStates = detail?.items?.associate { summary ->
            summary.captureId to summary.isFavorite
        }.orEmpty()
    }

    private fun clearSelection() {
        selectionGeneration += 1
        selection.value = CollectionSelectionUiState()
    }

    private fun clearDetailSearch() {
        detailSearchQuery.value = ""
        isDetailSearchVisible.value = false
        exitDetailSearchMode()
    }

    private fun publishState() {
        var currentSelection = selection.value
        if (currentSelection.isActive) {
            currentSelection = currentSelection.copy(
                selectedCaptureIds = currentSelection.selectedCaptureIds.intersect(detailCaptureIds),
            )
            selection.value = currentSelection
        }

        val filter = detailFilter.value
        val detail = filter?.let { activeFilter ->
            if (isDetailSearchMode) {
                CaptureList(count = detailSearchCount, items = emptyList())
                    .toDetailUiModel(
                        filter = activeFilter,
                        sort = detailSort.value,
                        hasNext = detailSearchHasNext,
                        isLoadingMore = detailSearchLoadingMore,
                    )
                    .copy(cards = detailSearchCards, count = detailSearchCount)
            } else {
                (publishedDetailCards ?: latestDetailCards ?: CaptureList(
                    count = 0,
                    items = emptyList()
                ))
                    .toDetailUiModel(
                        filter = activeFilter,
                        sort = detailSort.value,
                    )
                    .let { detailUi ->
                        detailUi.copy(
                            cards = detailUi.cards.map { card ->
                                card.copy(
                                    isFavorite = favoriteStates[card.captureId] ?: card.isFavorite,
                                )
                            },
                        )
                    }
            }
        }

        _uiState.update {
            CollectionUiState(
                isLoading = !hasReceivedFirstOverview,
                isLoadError = overviewLoadFailed,
                hasStoredScreenshots = latestOverview.hasAnyCapture,
                searchQuery = searchQuery.value,
                detailSearchQuery = detailSearchQuery.value,
                isDetailSearchVisible = isDetailSearchVisible.value,
                typeViewMode = typeViewMode.value,
                overview = latestOverview.toOverviewUiModel(),
                detail = detail,
                selection = currentSelection,
                pendingDeleteCaptureId = pendingDeleteCaptureId.value,
            )
        }
    }

    private fun CollectionListSort.toCaptureSort(): CaptureSort =
        when (this) {
            CollectionListSort.Latest -> CaptureSort.Latest
            CollectionListSort.Oldest -> CaptureSort.Oldest
        }

    private fun CollectionDetailFilter.toSearchScope(): SearchScope =
        when (this) {
            CollectionDetailFilter.Favorites -> SearchScope.FAVORITE
            is CollectionDetailFilter.ByType ->
                if (contentType == ScreenshotContentType.ETC) {
                    SearchScope.ETC
                } else {
                    SearchScope.TYPE
                }
        }

    private fun CollectionDetailFilter.toTypeCode(): ScreenshotContentType? =
        when (this) {
            CollectionDetailFilter.Favorites -> null
            is CollectionDetailFilter.ByType ->
                contentType.takeUnless { it == ScreenshotContentType.ETC }
        }
}
