package com.chalkak.recap.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.feature.collection.CollectionScreen
import com.chalkak.recap.feature.home.HomeAction
import com.chalkak.recap.feature.home.HomeScreen
import com.chalkak.recap.feature.mypage.MyPageScreen

@Composable
fun RecapNavHost(
    backStack: NavBackStack<NavKey>,
    onNavigateToDeveloper: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = { route ->
            when (route) {
                RecapRoute.Home -> NavEntry(route) {
                    HomeScreen(
                        onAction = { action ->
                            when (action) {
                                HomeAction.StartImport -> Unit
                                HomeAction.EnterDeveloperOptions -> onNavigateToDeveloper()
                            }
                        },
                    )
                }
                RecapRoute.Collection -> NavEntry(route) {
                    CollectionScreen()
                }
                RecapRoute.MyPage -> NavEntry(route) {
                    MyPageScreen()
                }
                else -> error("Unknown main route: $route")
            }
        },
    )
}
