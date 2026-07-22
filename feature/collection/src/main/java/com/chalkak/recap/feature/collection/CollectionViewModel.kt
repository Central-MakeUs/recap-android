package com.chalkak.recap.feature.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.storage.StorageRepository
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val storageRepository: StorageRepository,
    private val captureMutationRepository: CaptureMutationRepository,
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
    private var selectionGeneration = 0L

    private var latestOverview = StorageOverview(
        hasAnyCapture = false,
        favoriteCount = 0,
        types = emptyList(),
    )
    private var latestDetailCards: CaptureList? = null
    private var detailCaptureIds: Set<Long> = emptySet()
    private var favoriteStates: Map<Long, Boolean> = emptyMap()
    private var hasReceivedFirstOverview = false

    init {
        viewModelScope.launch {
            searchQuery
                .flatMapLatest { query -> storageRepository.observeOverview(query) }
                .collect { overview ->
                    hasReceivedFirstOverview = true
                    latestOverview = overview
                    publishState()
                }
        }

        viewModelScope.launch {
            combine(detailFilter, detailSort, detailSearchQuery) { filter, sort, query ->
                Triple(filter, sort, query)
            }.flatMapLatest { (filter, sort, query) ->
                if (filter == null) {
                    flowOf(null)
                } else {
                    observeDetail(filter = filter, sort = sort, searchQuery = query)
                }
            }.collect { detail ->
                latestDetailCards = detail
                detailCaptureIds = detail?.items?.map { it.captureId }?.toSet().orEmpty()
                favoriteStates = detail?.items?.associate { it.captureId to it.isFavorite }.orEmpty()
                publishState()
            }
        }
    }

    fun onAction(action: CollectionAction) {
        when (action) {
            is CollectionAction.UpdateSearchQuery -> {
                if (searchQuery.value != action.query) {
                    clearSelection()
                }
                searchQuery.value = action.query
                publishState()
            }

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
                publishState()
            }

            is CollectionAction.SetTypeViewMode -> {
                typeViewMode.value = action.viewMode
                publishState()
            }

            CollectionAction.OpenFavoriteDetail -> {
                clearSelection()
                clearDetailSearch()
                detailFilter.value = CollectionDetailFilter.Favorites
                detailSort.value = CollectionListSort.Latest
                publishState()
            }

            is CollectionAction.OpenFavoriteItem -> Unit

            is CollectionAction.OpenTypeDetail -> {
                clearSelection()
                clearDetailSearch()
                detailFilter.value = CollectionDetailFilter.ByType(action.contentType)
                detailSort.value = CollectionListSort.Latest
                publishState()
            }

            CollectionAction.CloseDetail -> {
                clearSelection()
                clearDetailSearch()
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
                viewModelScope.launch {
                    captureMutationRepository.updateFavorite(
                        captureId = action.captureId,
                        isFavorite = !currentFavorite,
                    )
                }
            }

            CollectionAction.StartSelection -> {
                selectionGeneration += 1
                selection.value = CollectionSelectionUiState(isActive = true)
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
        }
    }

    private fun observeDetail(
        filter: CollectionDetailFilter,
        sort: CollectionListSort,
        searchQuery: String,
    ) = when (filter) {
        CollectionDetailFilter.Favorites ->
            storageRepository.observeFavoriteCaptures(
                sort = sort.toCaptureSort(),
                searchQuery = searchQuery,
            )
        is CollectionDetailFilter.ByType ->
            storageRepository.observeCapturesByType(
                typeCode = filter.contentType,
                sort = sort.toCaptureSort(),
                searchQuery = searchQuery,
            )
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

    private fun clearSelection() {
        selectionGeneration += 1
        selection.value = CollectionSelectionUiState()
    }

    private fun clearDetailSearch() {
        detailSearchQuery.value = ""
        isDetailSearchVisible.value = false
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
            (latestDetailCards ?: CaptureList(count = 0, items = emptyList()))
                .toDetailUiModel(
                    filter = activeFilter,
                    sort = detailSort.value,
                )
        }

        _uiState.update {
            CollectionUiState(
                isLoading = !hasReceivedFirstOverview,
                hasStoredScreenshots = latestOverview.hasAnyCapture,
                searchQuery = searchQuery.value,
                detailSearchQuery = detailSearchQuery.value,
                isDetailSearchVisible = isDetailSearchVisible.value,
                typeViewMode = typeViewMode.value,
                overview = latestOverview.toOverviewUiModel(),
                detail = detail,
                selection = currentSelection,
            )
        }
    }

    private fun CollectionListSort.toCaptureSort(): CaptureSort =
        when (this) {
            CollectionListSort.Latest -> CaptureSort.Latest
            CollectionListSort.Oldest -> CaptureSort.Oldest
        }
}
