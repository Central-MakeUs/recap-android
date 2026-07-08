package com.chalkak.recap.feature.collection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Composable
fun CollectionRoute(
    onNavigateToOrganize: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(CollectionDestination.Overview)

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

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                navigateBackFromDetail()
            }
        },
        modifier = modifier,
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
                            onBackClick = ::navigateBackFromDetail,
                            onSortSelected = { sort ->
                                viewModel.onAction(CollectionAction.SetDetailSort(sort))
                            },
                            onFavoriteClick = { imageId ->
                                viewModel.onAction(CollectionAction.ToggleFavorite(imageId))
                            },
                        )
                    }
                }

                is CollectionDestination.TypeDetail -> NavEntry(route) {
                    uiState.detail?.let { detail ->
                        CollectionDetailScreen(
                            detail = detail,
                            onBackClick = ::navigateBackFromDetail,
                            onSortSelected = { sort ->
                                viewModel.onAction(CollectionAction.SetDetailSort(sort))
                            },
                            onFavoriteClick = { imageId ->
                                viewModel.onAction(CollectionAction.ToggleFavorite(imageId))
                            },
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
