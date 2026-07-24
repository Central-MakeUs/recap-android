package com.chalkak.recap.app

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.app.share.PendingShareIntake
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBar
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDestination
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.feature.home.HomeAnalysisProgressUiModel
import com.chalkak.recap.feature.organize.MAX_SELECTION_COUNT
import com.chalkak.recap.feature.organize.OrganizeRoute
import com.chalkak.recap.feature.organize.UnsupportedShareScreen
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecapMainScreen(
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRecentOrganizedScreenshots: () -> Unit = {},
    onOrganizeComplete: (List<LocalImage>) -> Unit = {},
    onNavigateToScreenshot: (Long) -> Unit = {},
    homeNavigationRequestId: Int = 0,
    pendingOpenOrganize: Boolean = false,
    onPendingOpenOrganizeConsumed: () -> Unit = {},
    pendingShareIntake: PendingShareIntake? = null,
    onPendingShareIntakeFinished: (String) -> Unit = {},
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
) {
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home
    val hazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    val toastDispatcher = LocalRecapToastDispatcher.current
    var openCollectionFavoritesOnNextEnter by remember { mutableStateOf(false) }
    var collectionPredictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var showOrganize by rememberSaveable { mutableStateOf(false) }
    var activeShareSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var presentedShareSessionId by rememberSaveable { mutableStateOf<String?>(null) }
    var showUnsupportedShare by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pendingOpenOrganize) {
        if (pendingOpenOrganize) {
            activeShareSessionId = null
            showOrganize = true
            onPendingOpenOrganizeConsumed()
        }
    }

    val shareConfirmation = pendingShareIntake as? PendingShareIntake.Confirmation
    val isFirstSharePresentation = shareConfirmation != null &&
        presentedShareSessionId != shareConfirmation.sessionId
    val shareNonImageRemovedToastMessage =
        if (isFirstSharePresentation && shareConfirmation.rejectedCount > 0) {
            stringResource(
                R.string.share_non_image_removed,
                shareConfirmation.rejectedCount,
            )
        } else {
            null
        }
    val shareMaxSelectionToastMessage =
        if (isFirstSharePresentation && shareConfirmation.trimmedByMax) {
            stringResource(
                R.string.share_max_selection_message,
                MAX_SELECTION_COUNT,
            )
        } else {
            null
        }

    LaunchedEffect(pendingShareIntake) {
        when (val pending = pendingShareIntake) {
            is PendingShareIntake.Confirmation -> {
                shareNonImageRemovedToastMessage?.let { message ->
                    toastDispatcher.showToast(
                        message = message,
                        type = RecapToastType.Error,
                    )
                }
                shareMaxSelectionToastMessage?.let { message ->
                    toastDispatcher.showToast(
                        message = message,
                        type = RecapToastType.Error,
                    )
                }
                presentedShareSessionId = pending.sessionId
                activeShareSessionId = pending.sessionId
                showUnsupportedShare = false
                showOrganize = true
            }

            is PendingShareIntake.Unsupported -> {
                presentedShareSessionId = pending.sessionId
                activeShareSessionId = null
                showOrganize = false
                showUnsupportedShare = true
            }

            null -> Unit
        }
    }

    fun navigateTo(route: MainTabRoute) {
        if (backStack.lastOrNull() == route) return
        when (route) {
            MainTabRoute.Home -> {
                backStack.clear()
                backStack.add(MainTabRoute.Home)
            }

            MainTabRoute.Collection -> {
                // Keep Home under Collection so system back returns to Home.
                while (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
                if (backStack.lastOrNull() != MainTabRoute.Home) {
                    backStack.clear()
                    backStack.add(MainTabRoute.Home)
                }
                backStack.add(MainTabRoute.Collection)
            }
        }
    }

    fun navigateToCollectionFavorites() {
        openCollectionFavoritesOnNextEnter = true
        navigateTo(MainTabRoute.Collection)
    }

    LaunchedEffect(homeNavigationRequestId) {
        if (homeNavigationRequestId > 0) {
            navigateTo(MainTabRoute.Home)
        }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute != MainTabRoute.Collection) {
            collectionPredictiveBackProgress = 0f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                RecapBottomBar(
                    hazeState = hazeState,
                    currentDestination = currentRoute.toBottomBarDestination(),
                    predictiveBackProgress = collectionPredictiveBackProgress,
                    onDestinationClick = { destination ->
                        navigateTo(destination.toMainTabRoute())
                    },
                    onOrganizeClick = {
                        activeShareSessionId = null
                        showOrganize = true
                    },
                )
            },
        ) { _ ->
            RecapMainTabNavHost(
                hazeState = hazeState,
                backStack = backStack,
                onNavigateToDeveloper = onNavigateToDeveloper,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToRecentOrganizedScreenshots = onNavigateToRecentOrganizedScreenshots,
                onNavigateToOrganize = {
                    activeShareSessionId = null
                    showOrganize = true
                },
                onNavigateToCollectionFavorites = ::navigateToCollectionFavorites,
                onNavigateToScreenshot = onNavigateToScreenshot,
                openCollectionFavoritesOnEnter = openCollectionFavoritesOnNextEnter,
                onOpenCollectionFavoritesOnEnterConsumed = {
                    openCollectionFavoritesOnNextEnter = false
                },
                showDeveloperLogoShortcut = BuildConfig.DEBUG,
                analysisProgressFlow = analysisProgressFlow,
                onCollectionPredictiveBackProgress = { progress ->
                    collectionPredictiveBackProgress = progress
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (showOrganize) {
            val activeShare = (pendingShareIntake as? PendingShareIntake.Confirmation)
                ?.takeIf { pending -> pending.sessionId == activeShareSessionId }
            key(activeShare?.sessionId ?: NORMAL_ORGANIZE_SESSION_KEY) {
                OrganizeRoute(
                    sharedImages = activeShare?.images,
                    shareSessionId = activeShare?.sessionId,
                    onNavigateBack = {
                        val completedShareSessionId = activeShareSessionId
                        showOrganize = false
                        activeShareSessionId = null
                        completedShareSessionId?.let(onPendingShareIntakeFinished)
                    },
                    onOrganizeComplete = { selectedScreenshots ->
                        val completedShareSessionId = activeShareSessionId
                        showOrganize = false
                        activeShareSessionId = null
                        completedShareSessionId?.let(onPendingShareIntakeFinished)
                        onOrganizeComplete(selectedScreenshots)
                    },
                )
            }
        }

        if (showUnsupportedShare) {
            UnsupportedShareScreen(
                onCloseClick = {
                    val unsupportedSessionId =
                        (pendingShareIntake as? PendingShareIntake.Unsupported)?.sessionId
                    showUnsupportedShare = false
                    unsupportedSessionId?.let(onPendingShareIntakeFinished)
                },
            )
        }
    }
}

private fun MainTabRoute.toBottomBarDestination(): RecapBottomBarDestination = when (this) {
    MainTabRoute.Home -> RecapBottomBarDestination.Home
    MainTabRoute.Collection -> RecapBottomBarDestination.Collection
}

private fun RecapBottomBarDestination.toMainTabRoute(): MainTabRoute = when (this) {
    RecapBottomBarDestination.Home -> MainTabRoute.Home
    RecapBottomBarDestination.Collection -> MainTabRoute.Collection
}

private const val NORMAL_ORGANIZE_SESSION_KEY = "normal-organize"
