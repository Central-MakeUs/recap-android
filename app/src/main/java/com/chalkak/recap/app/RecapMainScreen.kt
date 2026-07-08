package com.chalkak.recap.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBar
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDestination
import com.chalkak.recap.core.design.component.topbar.RecapMainTopBar
import com.chalkak.recap.feature.home.HomeAnalysisProgressUiModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun RecapMainScreen(
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToOrganize: () -> Unit = {},
    homeNavigationRequestId: Int = 0,
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
) {
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home
    val hazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)

    fun navigateTo(route: MainTabRoute) {
        if (backStack.lastOrNull() != route) {
            backStack.clear()
            backStack.add(route)
        }
    }

    LaunchedEffect(homeNavigationRequestId) {
        if (homeNavigationRequestId > 0) {
            navigateTo(MainTabRoute.Home)
        }
    }

    Scaffold(
        topBar = {
            RecapMainTopBar(
                onSettingsClick = onNavigateToMyPage,
                onSearchClick = onNavigateToSearch,
                onLogoClick = if (BuildConfig.DEBUG && currentRoute == MainTabRoute.Home) {
                    onNavigateToDeveloper
                } else {
                    null
                },
            )
        },
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
    ) { innerPadding ->
        RecapMainTabNavHost(
            hazeState = hazeState,
            backStack = backStack,
            onNavigateToDeveloper = onNavigateToDeveloper,
            onNavigateToOrganize = onNavigateToOrganize,
            analysisProgressFlow = analysisProgressFlow,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
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
