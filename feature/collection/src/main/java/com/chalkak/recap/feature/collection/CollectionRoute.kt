package com.chalkak.recap.feature.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
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
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.serialization.Serializable

@Composable
fun CollectionRoute(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onNavigateToOrganize: () -> Unit,
    onNavigateToScreenshot: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    favoritesNavigationRequestId: Int = 0,
    onPredictiveBackProgress: (Float) -> Unit = {},
    viewModel: CollectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val toastDispatcher = LocalRecapToastDispatcher.current
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

    LaunchedEffect(favoritesNavigationRequestId) {
        if (favoritesNavigationRequestId > 0) {
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
            viewModel.onAction(CollectionAction.OpenFavoriteDetail)
            val alreadyOnFavoriteDetail =
                backStack.lastOrNull() == CollectionDestination.FavoriteDetail
            if (!alreadyOnFavoriteDetail) {
                backStack.add(CollectionDestination.FavoriteDetail)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectionEvent.ShowDeleteSuccessToast -> {
                    toastDispatcher.showToast(
                        message = resources.getString(
                            R.string.collection_delete_success_toast,
                            event.deletedCount,
                        ),
                        type = RecapToastType.Success,
                    )
                }

                is CollectionEvent.ShowDeletePartialFailureToast -> {
                    toastDispatcher.showToast(
                        message = resources.getString(
                            R.string.collection_delete_partial_failure_toast,
                            event.deletedCount,
                            event.failedCount,
                        ),
                        type = RecapToastType.Error,
                    )
                }

                CollectionEvent.ShowDeleteFailureToast -> {
                    toastDispatcher.showToast(
                        message = resources.getString(R.string.collection_delete_failure_toast),
                        type = RecapToastType.Error,
                    )
                }
            }
        }
    }

    LaunchedEffect(predictiveProgress) {
        onPredictiveBackProgress(predictiveProgress)
    }

    LaunchedEffect(backStack.lastOrNull(), uiState.detail) {
        if (uiState.detail != null) return@LaunchedEffect
        when (val route = backStack.lastOrNull()) {
            CollectionDestination.FavoriteDetail -> {
                viewModel.onAction(CollectionAction.OpenFavoriteDetail)
            }

            is CollectionDestination.TypeDetail -> {
                val contentType = runCatching {
                    ScreenshotContentType.valueOf(route.contentTypeName)
                }.getOrNull() ?: return@LaunchedEffect
                viewModel.onAction(CollectionAction.OpenTypeDetail(contentType))
            }

            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        onDispose {
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

            is CollectionAction.OpenFavoriteItem -> {
                onNavigateToScreenshot(action.captureId)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = ::handleBack,
            modifier = Modifier.fillMaxSize(),
            entryProvider = { route ->
                when (route) {
                    CollectionDestination.Overview -> NavEntry(route) {
                        CollectionScreen(
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
                                onAction = ::handleAction,
                                onItemClick = onNavigateToScreenshot,
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
                                onAction = ::handleAction,
                                onItemClick = onNavigateToScreenshot,
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
