package com.chalkak.recap.app

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBar
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDestination
import com.chalkak.recap.feature.collection.CollectionTab
import com.chalkak.recap.feature.home.HomeAnalysisProgressUiModel
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecapMainScreen(
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRecentOrganizedScreenshots: () -> Unit = {},
    onNavigateToOrganize: () -> Unit = {},
    homeNavigationRequestId: Int = 0,
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
) {
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home
    val hazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    var collectionFavoritesNavigationRequestId by remember { mutableIntStateOf(0) }

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            RecapBottomBar(
                hazeState = hazeState,
                currentDestination = currentRoute.toBottomBarDestination(),
                onDestinationClick = { destination ->
                    navigateTo(destination.toMainTabRoute())
                },
                onOrganizeClick = onNavigateToOrganize,
            )
        },
    ) { _ ->
        RecapMainTabNavHost(
            hazeState = hazeState,
            backStack = backStack,
            onNavigateToDeveloper = onNavigateToDeveloper,
            onNavigateToMyPage = onNavigateToMyPage,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToRecentOrganizedScreenshots = onNavigateToRecentOrganizedScreenshots,
            onNavigateToOrganize = onNavigateToOrganize,
            onNavigateToCollectionFavorites = ::navigateToCollectionFavorites,
            collectionFavoritesNavigationRequestId = collectionFavoritesNavigationRequestId,
            collectionInitialTab = CollectionTab.Favorites,
            showDeveloperLogoShortcut = BuildConfig.DEBUG,
            analysisProgressFlow = analysisProgressFlow,
            modifier = Modifier.fillMaxSize(),
        )
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
