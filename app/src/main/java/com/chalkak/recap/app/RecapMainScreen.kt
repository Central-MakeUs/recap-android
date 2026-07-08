package com.chalkak.recap.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBar
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDestination
import com.chalkak.recap.core.design.component.topbar.RecapMainTopBar

@Composable
fun RecapMainScreen(
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home

    fun navigateTo(route: MainTabRoute) {
        if (backStack.lastOrNull() != route) {
            backStack.clear()
            backStack.add(route)
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
                currentDestination = currentRoute.toBottomBarDestination(),
                onDestinationClick = { destination ->
                    navigateTo(destination.toMainTabRoute())
                },
                onCleanupClick = {},
            )
        },
    ) { innerPadding ->
        RecapMainTabNavHost(
            backStack = backStack,
            onNavigateToDeveloper = onNavigateToDeveloper,
            modifier = Modifier.padding(innerPadding),
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
