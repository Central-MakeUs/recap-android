package com.chalkak.recap.feature.collection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.chrisbanes.haze.HazeState
import kotlinx.serialization.Serializable

@Composable
fun CollectionRoute(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onNavigateToOrganize: () -> Unit,
    onNavigateBack: () -> Unit = {},
    initialTab: CollectionTab = CollectionTab.Favorites,
    favoritesNavigationRequestId: Int = 0,
    onPredictiveBackProgress: (Float) -> Unit = {},
    viewModel: CollectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(CollectionDestination.Overview)
    val isAtRoot = backStack.size <= 1
    val canPredictivePopToHome = isAtRoot &&
        !uiState.selection.isActive &&
        !uiState.isDetailSearchVisible
    val navigationEventState = rememberNavigationEventState(
        currentInfo = NavigationEventInfo.None,
    )
    val predictiveProgress = when (val transitionState = navigationEventState.transitionState) {
        is NavigationEventTransitionState.InProgress -> {
            if (canPredictivePopToHome) {
                transitionState.latestEvent.progress
            } else {
                0f
            }
        }
        is NavigationEventTransitionState.Idle -> 0f
    }

    LaunchedEffect(initialTab) {
        viewModel.onAction(CollectionAction.SelectTab(initialTab))
    }

    LaunchedEffect(favoritesNavigationRequestId) {
        if (favoritesNavigationRequestId > 0) {
            viewModel.onAction(CollectionAction.CloseDetail)
            viewModel.onAction(CollectionAction.SelectTab(CollectionTab.Favorites))
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        }
    }

    LaunchedEffect(predictiveProgress) {
        onPredictiveBackProgress(predictiveProgress)
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onAction(CollectionAction.CloseDetail)
            onPredictiveBackProgress(0f)
        }
    }

    fun handleAction(action: CollectionAction) {
        when (action) {
            CollectionAction.OpenFavoriteDetail -> {
                viewModel.onAction(action)
                backStack.add(CollectionDestination.FavoriteDetail)
            }

            is CollectionAction.OpenTypeDetail -> {
                viewModel.onAction(action)
                backStack.add(CollectionDestination.TypeDetail(action.contentType.name))
            }

            else -> viewModel.onAction(action)
        }
    }

    fun navigateBackFromDetail() {
        backStack.removeLastOrNull()
        viewModel.onAction(CollectionAction.CloseDetail)
    }

    fun handleBack() {
        when {
            uiState.selection.isActive -> viewModel.onAction(CollectionAction.CancelSelection)
            uiState.isDetailSearchVisible -> viewModel.onAction(CollectionAction.HideDetailSearch)
            backStack.size > 1 -> navigateBackFromDetail()
            else -> onNavigateBack()
        }
    }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = isAtRoot,
        onBackCancelled = { onPredictiveBackProgress(0f) },
        onBackCompleted = ::handleBack,
    )

    NavDisplay(
        backStack = backStack,
        onBack = ::handleBack,
        modifier = modifier.fillMaxSize(),
        entryProvider = { route ->
            when (route) {
                CollectionDestination.Overview -> NavEntry(route) {
                    CollectionScreen(
                        hazeState = hazeState,
                        uiState = uiState,
                        onAction = ::handleAction,
                        onNavigateToOrganize = onNavigateToOrganize,
                    )
                }

                CollectionDestination.FavoriteDetail -> NavEntry(route) {
                    uiState.detail?.let { detail ->
                        CollectionDetailScreen(
                            detail = detail,
                            selection = uiState.selection,
                            onBackClick = ::handleBack,
                            onAction = viewModel::onAction,
                            searchQuery = uiState.detailSearchQuery,
                            isSearchVisible = uiState.isDetailSearchVisible,
                        )
                    }
                }

                is CollectionDestination.TypeDetail -> NavEntry(route) {
                    uiState.detail?.let { detail ->
                        CollectionDetailScreen(
                            detail = detail,
                            selection = uiState.selection,
                            onBackClick = ::handleBack,
                            onAction = viewModel::onAction,
                            searchQuery = uiState.detailSearchQuery,
                            isSearchVisible = uiState.isDetailSearchVisible,
                        )
                    }
                }

                else -> error("Unknown collection route: $route")
            }
        },
    )
}

@Serializable
private sealed interface CollectionDestination : NavKey {
    @Serializable
    data object Overview : CollectionDestination

    @Serializable
    data object FavoriteDetail : CollectionDestination

    @Serializable
    data class TypeDetail(val contentTypeName: String) : CollectionDestination
}
