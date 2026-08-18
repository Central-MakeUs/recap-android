package com.chalkak.recap.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.capture.CaptureThumbnailUpdates
import com.chalkak.recap.core.data.network.MainContentRecoveryTrigger
import com.chalkak.recap.core.data.search.SearchRepository
import com.chalkak.recap.core.data.storage.StorageRepository
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchScope
import com.chalkak.recap.core.model.storage.CaptureSort
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val searchRepository: SearchRepository,
    private val captureMutationRepository: CaptureMutationRepository,
    private val thumbnailUpdates: CaptureThumbnailUpdates,
    private val mainContentRecoveryTrigger: MainContentRecoveryTrigger,
) : ViewModel() {
    private val store = MutableStateFlow(CollectionStoreState())
    val uiState: StateFlow<CollectionUiState> = CollectionUiStateFlow(store.asStateFlow())

    private val _events = MutableSharedFlow<CollectionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<CollectionEvent> = _events.asSharedFlow()

    private var selectionGeneration = 0L
    private var detailSearchJob: Job? = null
    private var detailLoadMoreJob: Job? = null

    init {
        observeThumbnailUpdates()
        observeRecoveryRequests()
        observeOverview()
        observeDetail()
    }

    fun onAction(action: CollectionAction) {
        when (action) {
            is CollectionAction.UpdateSearchQuery -> updateUi { copy(searchQuery = action.query) }
            CollectionAction.OpenSearch -> Unit
            CollectionAction.RetryLoad -> storageRepository.refreshOverview()
            CollectionAction.ShowDetailSearch -> updateUi { copy(isDetailSearchVisible = true) }
            CollectionAction.HideDetailSearch -> clearDetailSearch()
            is CollectionAction.UpdateDetailSearchQuery -> updateDetailSearchQuery(action.query)
            CollectionAction.SubmitDetailSearch -> submitDetailSearch()
            CollectionAction.LoadMoreDetailSearch -> loadMoreDetailSearch()
            is CollectionAction.SetTypeViewMode -> updateUi { copy(typeViewMode = action.viewMode) }
            CollectionAction.OpenFavoriteDetail -> openDetail(CollectionDetailFilter.Favorites)
            is CollectionAction.OpenFavoriteItem -> Unit
            is CollectionAction.OpenTypeDetail -> {
                openDetail(CollectionDetailFilter.ByType(action.contentType))
            }
            CollectionAction.CloseDetail -> closeDetail()
            is CollectionAction.SetDetailSort -> setDetailSort(action.sort)
            is CollectionAction.ToggleFavorite -> toggleFavorite(action.captureId)
            CollectionAction.StartSelection -> startSelection()
            CollectionAction.CancelSelection -> clearSelection()
            is CollectionAction.ToggleItemSelection -> toggleItemSelection(action.captureId)
            CollectionAction.DeleteSelected -> showDeleteConfirmDialog()
            CollectionAction.ConfirmDeleteSelected -> deleteSelectedCards()
            CollectionAction.DismissDeleteConfirmDialog -> dismissDeleteConfirmDialog()
            is CollectionAction.RequestDeleteItem -> requestDeleteItem(action.captureId)
            CollectionAction.ConfirmDeleteItem -> confirmDeleteItem()
            CollectionAction.DismissDeleteItem -> dismissDeleteItem()
        }
    }

    private fun observeThumbnailUpdates() {
        viewModelScope.launch {
            thumbnailUpdates.thumbnailReady.collect { ready ->
                applyThumbnailReady(ready.captureId, ready.localPath)
            }
        }
    }

    private fun observeRecoveryRequests() {
        viewModelScope.launch {
            mainContentRecoveryTrigger.recoveries.collect {
                if (store.value.uiState.isLoadError) storageRepository.refreshOverview()
            }
        }
    }

    private fun observeOverview() {
        viewModelScope.launch {
            storageRepository.observeOverview(searchQuery = "").collect { result ->
                result.fold(
                    onSuccess = { overview ->
                        val mappedOverview = overview.toOverviewUiModel()
                        store.update { current ->
                            current.copy(
                                pendingAutoRetryOnFailure = false,
                                uiState = current.uiState.copy(
                                    isLoading = false,
                                    isLoadError = false,
                                    hasStoredScreenshots = overview.hasAnyCapture,
                                    overview = mappedOverview,
                                ),
                            )
                        }
                    },
                    onFailure = {
                        if (store.value.pendingAutoRetryOnFailure) {
                            store.update { it.copy(pendingAutoRetryOnFailure = false) }
                            storageRepository.refreshOverview()
                        } else {
                            updateUi { copy(isLoading = false, isLoadError = true) }
                        }
                    },
                )
            }
        }
    }

    private fun observeDetail() {
        viewModelScope.launch {
            store.map { it.detailFilter to it.detailSort }
                .distinctUntilChanged()
                .flatMapLatest { (filter, sort) ->
                    if (filter == null) {
                        flowOf(ObservedDetail(detail = null))
                    } else {
                        observeDetail(filter, sort).map { captures ->
                            ObservedDetail(captures.toDetailUiModel(filter, sort))
                        }
                    }
                }
                .collect { observed ->
                    store.update { current ->
                        val validIds = observed.detail?.cards
                            ?.mapTo(mutableSetOf()) { it.captureId }
                            .orEmpty()
                        current.copy(
                            observedDetail = observed.detail,
                            uiState = current.uiState.copy(
                                detail = if (current.isDetailSearchMode) {
                                    current.uiState.detail
                                } else {
                                    observed.detail
                                },
                                selection = current.uiState.selection.retainIds(validIds),
                            ),
                        )
                    }
                    observed.detail?.cards?.map { it.captureId }?.let(::reconcileThumbnails)
                }
        }
    }

    private fun observeDetail(filter: CollectionDetailFilter, sort: CollectionListSort) =
        when (filter) {
            CollectionDetailFilter.Favorites -> storageRepository.observeFavoriteCaptures(
                sort = sort.toCaptureSort(),
                searchQuery = "",
            )
            is CollectionDetailFilter.ByType -> storageRepository.observeCapturesByType(
                typeCode = filter.contentType,
                sort = sort.toCaptureSort(),
                searchQuery = "",
            )
        }

    private fun openDetail(filter: CollectionDetailFilter) {
        cancelDetailSearch()
        selectionGeneration += 1
        store.update { current ->
            current.copy(
                detailFilter = filter,
                detailSort = CollectionListSort.Latest,
                observedDetail = null,
                isDetailSearchMode = false,
                detailSubmittedQuery = "",
                detailSearchNextPage = 0,
                uiState = current.uiState.copy(
                    detail = null,
                    detailSearchQuery = "",
                    isDetailSearchVisible = false,
                    selection = CollectionSelectionUiState(),
                    pendingDeleteCaptureId = null,
                ),
            )
        }
    }

    private fun closeDetail() {
        cancelDetailSearch()
        selectionGeneration += 1
        store.update { current ->
            current.copy(
                detailFilter = null,
                detailSort = CollectionListSort.Latest,
                observedDetail = null,
                isDetailSearchMode = false,
                detailSubmittedQuery = "",
                detailSearchNextPage = 0,
                uiState = current.uiState.copy(
                    detail = null,
                    detailSearchQuery = "",
                    isDetailSearchVisible = false,
                    selection = CollectionSelectionUiState(),
                    pendingDeleteCaptureId = null,
                ),
            )
        }
    }

    private fun setDetailSort(sort: CollectionListSort) {
        store.update { it.copy(detailSort = sort) }
    }

    private fun updateDetailSearchQuery(query: String) {
        if (store.value.uiState.detailSearchQuery != query) clearSelection()
        updateUi { copy(detailSearchQuery = query) }
        if (query.isEmpty()) exitDetailSearchMode()
    }

    private fun submitDetailSearch() {
        val current = store.value
        val filter = current.detailFilter ?: return
        val query = current.uiState.detailSearchQuery.trim()
        if (query.isEmpty()) return

        cancelDetailSearch()
        detailSearchJob = viewModelScope.launch {
            store.update { state ->
                state.copy(
                    isDetailSearchMode = true,
                    detailSubmittedQuery = query,
                    detailSearchNextPage = 0,
                    uiState = state.uiState.copy(
                        detailSearchQuery = query,
                        detail = searchDetailUi(filter, state.detailSort),
                    ),
                )
            }
            searchRepository.observeSearch(
                query = query,
                scope = filter.toSearchScope(),
                typeCode = filter.toTypeCode(),
            ).collect { result ->
                detailLoadMoreJob?.cancel()
                result.fold(
                    onSuccess = { page ->
                        val cards = page.items.map { it.toCardItemUiModel() }
                        store.update { state ->
                            if (!state.isDetailSearchMode || state.detailSubmittedQuery != query) {
                                state
                            } else {
                                state.copy(
                                    detailSearchNextPage = 1,
                                    uiState = state.uiState.copy(
                                        detail = searchDetailUi(
                                            filter = filter,
                                            sort = state.detailSort,
                                            cards = cards,
                                            count = page.count.toInt(),
                                            hasNext = page.hasNext,
                                        ),
                                        selection = state.uiState.selection.retainIds(
                                            cards.mapTo(mutableSetOf()) { it.captureId },
                                        ),
                                    ),
                                )
                            }
                        }
                        reconcileThumbnails(cards.map { it.captureId })
                    },
                    onFailure = {
                        store.update { state ->
                            state.copy(
                                detailSearchNextPage = 0,
                                uiState = state.uiState.copy(
                                    detail = searchDetailUi(filter, state.detailSort),
                                    selection = state.uiState.selection.retainIds(emptySet()),
                                ),
                            )
                        }
                    },
                )
            }
        }
    }

    private fun loadMoreDetailSearch() {
        val current = store.value
        val filter = current.detailFilter ?: return
        val detail = current.uiState.detail ?: return
        if (!current.isDetailSearchMode || !detail.hasNext || detail.isLoadingMore ||
            current.detailSubmittedQuery.isBlank() || detailLoadMoreJob?.isActive == true
        ) return

        val pageToLoad = current.detailSearchNextPage
        val query = current.detailSubmittedQuery
        detailLoadMoreJob = viewModelScope.launch {
            updateDetail { copy(isLoadingMore = true) }
            val result = searchRepository.search(
                query = query,
                scope = filter.toSearchScope(),
                typeCode = filter.toTypeCode(),
                page = pageToLoad,
            )
            result.fold(
                onSuccess = { page ->
                    val newCards = page.items.map { it.toCardItemUiModel() }
                    store.update { state ->
                        val activeDetail = state.uiState.detail ?: return@update state
                        state.copy(
                            detailSearchNextPage = pageToLoad + 1,
                            uiState = state.uiState.copy(
                                detail = activeDetail.copy(
                                    cards = (activeDetail.cards + newCards)
                                        .distinctBy { it.captureId },
                                    count = page.count.toInt(),
                                    hasNext = page.hasNext,
                                    isLoadingMore = false,
                                ),
                            ),
                        )
                    }
                    reconcileThumbnails(newCards.map { it.captureId })
                },
                onFailure = { updateDetail { copy(isLoadingMore = false) } },
            )
        }
    }

    private fun exitDetailSearchMode() {
        cancelDetailSearch()
        store.update { current ->
            current.copy(
                isDetailSearchMode = false,
                detailSubmittedQuery = "",
                detailSearchNextPage = 0,
                uiState = current.uiState.copy(detail = current.observedDetail),
            )
        }
    }

    private fun clearDetailSearch() {
        updateUi { copy(detailSearchQuery = "", isDetailSearchVisible = false) }
        exitDetailSearchMode()
    }

    private fun cancelDetailSearch() {
        detailSearchJob?.cancel()
        detailLoadMoreJob?.cancel()
    }

    private fun toggleFavorite(captureId: Long) {
        val currentItem = store.value.uiState.detail?.cards?.firstOrNull {
            it.captureId == captureId
        } ?: return
        val nextFavorite = !currentItem.isFavorite
        updateFavorite(captureId, nextFavorite)
        viewModelScope.launch {
            val result = captureMutationRepository.updateFavorite(captureId, nextFavorite)
            if (result.isFailure) {
                updateFavorite(captureId, currentItem.isFavorite)
            } else {
                _events.emit(CollectionEvent.ShowFavoriteToast(nextFavorite))
            }
        }
    }

    private fun updateFavorite(captureId: Long, isFavorite: Boolean) {
        store.update { current ->
            val updateDetail: (CollectionDetailUiModel?) -> CollectionDetailUiModel? = { detail ->
                detail?.copy(cards = detail.cards.map { card ->
                    if (card.captureId == captureId) card.copy(isFavorite = isFavorite) else card
                })
            }
            current.copy(
                observedDetail = updateDetail(current.observedDetail),
                uiState = current.uiState.copy(detail = updateDetail(current.uiState.detail)),
            )
        }
    }

    private fun startSelection() {
        selectionGeneration += 1
        updateUi {
            copy(
                selection = CollectionSelectionUiState(isActive = true),
                pendingDeleteCaptureId = null,
            )
        }
    }

    private fun clearSelection() {
        selectionGeneration += 1
        updateUi { copy(selection = CollectionSelectionUiState()) }
    }

    private fun toggleItemSelection(captureId: Long) {
        val current = store.value.uiState
        if (!current.selection.isActive || current.selection.isDeleting) return
        if (current.detail?.cards?.none { it.captureId == captureId } != false) return
        val selectedIds = current.selection.selectedCaptureIds.toMutableSet().apply {
            if (!add(captureId)) remove(captureId)
        }
        updateUi { copy(selection = selection.copy(selectedCaptureIds = selectedIds)) }
    }

    private fun showDeleteConfirmDialog() {
        val selection = store.value.uiState.selection
        if (!selection.isActive || selection.isDeleting || selection.selectedCount == 0) return
        updateUi { copy(selection = selection.copy(showDeleteConfirmDialog = true)) }
    }

    private fun dismissDeleteConfirmDialog() {
        val selection = store.value.uiState.selection
        if (!selection.showDeleteConfirmDialog || selection.isDeleting) return
        updateUi { copy(selection = selection.copy(showDeleteConfirmDialog = false)) }
    }

    private fun requestDeleteItem(captureId: Long) {
        if (store.value.uiState.detail?.cards?.none { it.captureId == captureId } != false) return
        updateUi { copy(pendingDeleteCaptureId = captureId) }
    }

    private fun dismissDeleteItem() {
        if (store.value.uiState.pendingDeleteCaptureId == null) return
        updateUi { copy(pendingDeleteCaptureId = null) }
    }

    private fun confirmDeleteItem() {
        val captureId = store.value.uiState.pendingDeleteCaptureId ?: return
        updateUi { copy(pendingDeleteCaptureId = null) }
        viewModelScope.launch {
            val result = captureMutationRepository.deleteCaptures(setOf(captureId))
            val deleted = result.getOrNull()?.deletedIds.orEmpty()
            if (result.isFailure || captureId !in deleted) {
                _events.emit(CollectionEvent.ShowDeleteFailureToast)
                return@launch
            }
            removeSearchCards(deleted)
            _events.emit(CollectionEvent.ShowDeleteSuccessToast(1))
        }
    }

    private fun deleteSelectedCards() {
        val current = store.value.uiState
        if (!current.selection.isActive || current.selection.isDeleting) return
        val validIds = current.detail?.cards?.mapTo(mutableSetOf()) { it.captureId }.orEmpty()
        val captureIds = current.selection.selectedCaptureIds.intersect(validIds)
        if (captureIds.isEmpty()) {
            updateUi { copy(selection = selection.copy(showDeleteConfirmDialog = false)) }
            return
        }

        val deleteGeneration = selectionGeneration
        updateUi {
            copy(selection = selection.copy(isDeleting = true, showDeleteConfirmDialog = false))
        }
        viewModelScope.launch {
            val result = captureMutationRepository.deleteCaptures(captureIds)
            if (selectionGeneration != deleteGeneration) return@launch
            val deleteResult = result.getOrNull()
            if (result.isFailure || deleteResult == null) {
                updateUi { copy(selection = selection.copy(isDeleting = false)) }
                _events.emit(CollectionEvent.ShowDeleteFailureToast)
                return@launch
            }

            removeSearchCards(deleteResult.deletedIds)
            when {
                deleteResult.isFullFailure -> {
                    updateUi { copy(selection = selection.copy(isDeleting = false)) }
                    _events.emit(CollectionEvent.ShowDeleteFailureToast)
                }
                deleteResult.isPartialSuccess -> {
                    updateUi {
                        copy(selection = selection.copy(
                            selectedCaptureIds = deleteResult.failedIds,
                            isDeleting = false,
                            showDeleteConfirmDialog = false,
                        ))
                    }
                    _events.emit(CollectionEvent.ShowDeletePartialFailureToast(
                        deletedCount = deleteResult.deletedIds.size,
                        failedCount = deleteResult.failedIds.size,
                    ))
                }
                else -> {
                    _events.emit(CollectionEvent.ShowDeleteSuccessToast(deleteResult.deletedIds.size))
                    clearSelection()
                }
            }
        }
    }

    private fun removeSearchCards(captureIds: Set<Long>) {
        if (captureIds.isEmpty() || !store.value.isDetailSearchMode) return
        updateDetail {
            val remaining = cards.filterNot { it.captureId in captureIds }
            val removedCount = cards.size - remaining.size
            if (removedCount == 0) this else copy(
                cards = remaining,
                count = (count - removedCount).coerceAtLeast(0),
            )
        }
    }

    private fun reconcileThumbnails(captureIds: Iterable<Long>) {
        captureIds.forEach { captureId ->
            thumbnailUpdates.resolveLocalPath(captureId)?.let { path ->
                applyThumbnailReady(captureId, path)
            }
        }
    }

    private fun applyThumbnailReady(captureId: Long, localPath: String) {
        store.update { current ->
            val updateDetail: (CollectionDetailUiModel?) -> CollectionDetailUiModel? = { detail ->
                detail?.copy(cards = detail.cards.map { card ->
                    if (card.captureId == captureId) card.copy(thumbnailModel = localPath) else card
                })
            }
            current.copy(
                observedDetail = updateDetail(current.observedDetail),
                uiState = current.uiState.copy(detail = updateDetail(current.uiState.detail)),
            )
        }
    }

    private fun updateUi(transform: CollectionUiState.() -> CollectionUiState) {
        store.update { current -> current.copy(uiState = transform(current.uiState)) }
    }

    private fun updateDetail(transform: CollectionDetailUiModel.() -> CollectionDetailUiModel) {
        updateUi { copy(detail = detail?.let(transform)) }
    }

    private fun searchDetailUi(
        filter: CollectionDetailFilter,
        sort: CollectionListSort,
        cards: List<CollectionCardItemUiModel> = emptyList(),
        count: Int = 0,
        hasNext: Boolean = false,
        isLoadingMore: Boolean = false,
    ): CollectionDetailUiModel = CaptureList(count = count, items = emptyList())
        .toDetailUiModel(filter, sort, hasNext, isLoadingMore)
        .copy(cards = cards)

    private fun CollectionSelectionUiState.retainIds(validIds: Set<Long>) =
        if (isActive) copy(selectedCaptureIds = selectedCaptureIds.intersect(validIds)) else this

    private fun CollectionListSort.toCaptureSort() = when (this) {
        CollectionListSort.Latest -> CaptureSort.Latest
        CollectionListSort.Oldest -> CaptureSort.Oldest
    }

    private fun CollectionDetailFilter.toSearchScope() = when (this) {
        CollectionDetailFilter.Favorites -> SearchScope.FAVORITE
        is CollectionDetailFilter.ByType -> if (contentType == ScreenshotContentType.ETC) {
            SearchScope.ETC
        } else {
            SearchScope.TYPE
        }
    }

    private fun CollectionDetailFilter.toTypeCode() = when (this) {
        CollectionDetailFilter.Favorites -> null
        is CollectionDetailFilter.ByType -> contentType.takeUnless {
            it == ScreenshotContentType.ETC
        }
    }
}

private data class CollectionStoreState(
    val uiState: CollectionUiState = CollectionUiState(),
    val detailFilter: CollectionDetailFilter? = null,
    val detailSort: CollectionListSort = CollectionListSort.Latest,
    val observedDetail: CollectionDetailUiModel? = null,
    val isDetailSearchMode: Boolean = false,
    val detailSubmittedQuery: String = "",
    val detailSearchNextPage: Int = 0,
    val pendingAutoRetryOnFailure: Boolean = true,
)

private data class ObservedDetail(val detail: CollectionDetailUiModel?)

@OptIn(InternalCoroutinesApi::class, ExperimentalForInheritanceCoroutinesApi::class)
private class CollectionUiStateFlow(
    private val store: StateFlow<CollectionStoreState>,
) : StateFlow<CollectionUiState> {
    override val value: CollectionUiState
        get() = store.value.uiState

    override val replayCache: List<CollectionUiState>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<CollectionUiState>): Nothing {
        var previous: CollectionUiState? = null
        store.collect { state ->
            if (state.uiState != previous) {
                previous = state.uiState
                collector.emit(state.uiState)
            }
        }
    }
}
