package com.chalkak.recap.feature.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.search.RecentSearchStore
import com.chalkak.recap.core.data.search.SearchRepository
import com.chalkak.recap.core.model.search.SearchScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val captureMutationRepository: CaptureMutationRepository,
    private val recentSearchStore: RecentSearchStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var preserveSessionOnNextDispose = false

    init {
        viewModelScope.launch {
            recentSearchStore.recentSearches.collect { terms ->
                _uiState.update { state -> state.copy(recentSearches = terms) }
            }
        }
    }

    fun onAction(action: SearchAction) {
        when (action) {
            SearchAction.Reset,
            SearchAction.NavigateBack,
            -> endSearchSession()

            SearchAction.LeaveComposition -> leaveComposition()

            is SearchAction.UpdateQuery -> {
                if (action.query.isEmpty()) {
                    clearSearchSession()
                } else {
                    _uiState.update { state -> state.copy(query = action.query) }
                }
            }

            SearchAction.SubmitSearch,
            SearchAction.RetrySearch,
            -> submitSearch(reset = true)

            SearchAction.LoadMore -> loadMore()

            is SearchAction.SelectRecentSearch -> {
                _uiState.update { state -> state.copy(query = action.term) }
                submitSearch(reset = true)
            }

            is SearchAction.RemoveRecentSearch -> {
                viewModelScope.launch {
                    recentSearchStore.remove(action.term)
                }
            }

            SearchAction.ClearAllRecentSearches -> {
                viewModelScope.launch {
                    recentSearchStore.clearAll()
                }
            }

            is SearchAction.ToggleFavorite -> toggleFavorite(action.captureId)

            is SearchAction.SelectResult -> prepareNavigateToDetail()
        }
    }

    private fun prepareNavigateToDetail() {
        preserveSessionOnNextDispose = true
        _uiState.update { state -> state.copy(autoFocus = false) }
    }

    private fun leaveComposition() {
        if (preserveSessionOnNextDispose) {
            preserveSessionOnNextDispose = false
            return
        }
        endSearchSession()
    }

    private fun endSearchSession() {
        clearSearchSession()
        _uiState.update { state -> state.copy(autoFocus = true) }
    }

    private fun clearSearchSession() {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        _uiState.update { state ->
            state.copy(
                query = "",
                submittedQuery = "",
                phase = SearchContentPhase.Idle,
                results = emptyList(),
                resultCount = 0L,
                hasNext = false,
                nextPage = 0,
                isLoadingMore = false,
            )
        }
    }

    private fun submitSearch(reset: Boolean) {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            return
        }

        searchJob?.cancel()
        loadMoreJob?.cancel()
        searchJob = viewModelScope.launch {
            recentSearchStore.remember(query)
            _uiState.update { state ->
                state.copy(
                    query = query,
                    submittedQuery = query,
                    phase = if (reset) SearchContentPhase.Loading else state.phase,
                    isLoadingMore = false,
                    hasNext = false,
                    nextPage = 0,
                )
            }

            val result = searchRepository.search(
                query = query,
                scope = SearchScope.ALL,
                page = 0,
            )

            result.fold(
                onSuccess = { page ->
                    val items = page.toSearchResultItems()
                    _uiState.update { state ->
                        state.copy(
                            phase = if (items.isEmpty()) {
                                SearchContentPhase.Empty
                            } else {
                                SearchContentPhase.Results
                            },
                            results = items,
                            resultCount = page.count,
                            hasNext = page.hasNext,
                            nextPage = 1,
                            isLoadingMore = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(
                            phase = SearchContentPhase.Error,
                            results = emptyList(),
                            resultCount = 0L,
                            hasNext = false,
                            nextPage = 0,
                            isLoadingMore = false,
                        )
                    }
                },
            )
        }
    }

    private fun loadMore() {
        val state = _uiState.value
        if (
            state.phase != SearchContentPhase.Results ||
            !state.hasNext ||
            state.isLoadingMore ||
            state.submittedQuery.isBlank()
        ) {
            return
        }
        if (loadMoreJob?.isActive == true) {
            return
        }

        val query = state.submittedQuery
        val pageToLoad = state.nextPage
        loadMoreJob = viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoadingMore = true) }

            val result = searchRepository.search(
                query = query,
                scope = SearchScope.ALL,
                page = pageToLoad,
            )

            result.fold(
                onSuccess = { page ->
                    val newItems = page.toSearchResultItems()
                    _uiState.update { current ->
                        val merged = (current.results + newItems)
                            .distinctBy { item -> item.captureId }
                        current.copy(
                            results = merged,
                            resultCount = page.count,
                            hasNext = page.hasNext,
                            nextPage = pageToLoad + 1,
                            isLoadingMore = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { current ->
                        current.copy(isLoadingMore = false)
                    }
                },
            )
        }
    }

    private fun toggleFavorite(captureId: Long) {
        val currentItem = _uiState.value.results.firstOrNull { item ->
            item.captureId == captureId
        } ?: return
        val nextFavorite = !currentItem.isFavorite

        _uiState.update { state ->
            state.copy(
                results = state.results.map { item ->
                    if (item.captureId == captureId) {
                        item.copy(isFavorite = nextFavorite)
                    } else {
                        item
                    }
                },
            )
        }

        viewModelScope.launch {
            val mutation = captureMutationRepository.updateFavorite(
                captureId = captureId,
                isFavorite = nextFavorite,
            )
            if (mutation.isFailure) {
                _uiState.update { state ->
                    state.copy(
                        results = state.results.map { item ->
                            if (item.captureId == captureId) {
                                item.copy(isFavorite = currentItem.isFavorite)
                            } else {
                                item
                            }
                        },
                    )
                }
            }
        }
    }
}
