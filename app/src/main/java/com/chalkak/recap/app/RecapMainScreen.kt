package com.chalkak.recap.app

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBar
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDestination
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.component.toast.rememberRecapToastHostState
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.feature.home.HomeAnalysisProgressUiModel
import com.chalkak.recap.feature.organize.OrganizeRoute
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
    onNavigateToScreenshot: (String) -> Unit = {},
    homeNavigationRequestId: Int = 0,
    pendingOpenOrganize: Boolean = false,
    onPendingOpenOrganizeConsumed: () -> Unit = {},
    pendingScreenshotDeletedToast: Boolean = false,
    onPendingScreenshotDeletedToastConsumed: () -> Unit = {},
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
) {
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home
    val hazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    val toastHostState = rememberRecapToastHostState()
    val resources = LocalResources.current
    var collectionFavoritesNavigationRequestId by remember { mutableIntStateOf(0) }
    var collectionPredictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var showOrganize by rememberSaveable { mutableStateOf(false) }
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val toastBottomPadding = RecapBottomBarDefaults.Height +
        RecapBottomBarDefaults.BottomPadding +
        navigationBarBottomPadding +
        8.dp

    LaunchedEffect(pendingOpenOrganize) {
        if (pendingOpenOrganize) {
            showOrganize = true
            onPendingOpenOrganizeConsumed()
        }
    }

    LaunchedEffect(pendingScreenshotDeletedToast) {
        if (!pendingScreenshotDeletedToast) return@LaunchedEffect
        onPendingScreenshotDeletedToastConsumed()
        toastHostState.showToast(
            message = resources.getString(R.string.screenshot_delete_success_toast),
            type = RecapToastType.Success,
        )
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
        navigateTo(MainTabRoute.Collection)
        collectionFavoritesNavigationRequestId += 1
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
                    onOrganizeClick = { showOrganize = true },
                )
            },
        ) { _ ->
            RecapMainTabNavHost(
                hazeState = hazeState,
                toastHostState = toastHostState,
                backStack = backStack,
                onNavigateToDeveloper = onNavigateToDeveloper,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToRecentOrganizedScreenshots = onNavigateToRecentOrganizedScreenshots,
                onNavigateToOrganize = { showOrganize = true },
                onNavigateToCollectionFavorites = ::navigateToCollectionFavorites,
                onNavigateToScreenshot = onNavigateToScreenshot,
                collectionFavoritesNavigationRequestId = collectionFavoritesNavigationRequestId,
                showDeveloperLogoShortcut = BuildConfig.DEBUG,
                analysisProgressFlow = analysisProgressFlow,
                onCollectionPredictiveBackProgress = { progress ->
                    collectionPredictiveBackProgress = progress
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        RecapToastHost(
            hostState = toastHostState,
            hazeState = hazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = toastBottomPadding),
        )

        if (showOrganize) {
            OrganizeRoute(
                onNavigateBack = { showOrganize = false },
                onOrganizeComplete = { selectedScreenshots ->
                    showOrganize = false
                    onOrganizeComplete(selectedScreenshots)
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
