package com.chalkak.recap.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface RecapRootRoute : NavKey {
    @Serializable
    data object Onboarding : RecapRootRoute

    @Serializable
    data object Main : RecapRootRoute

    @Serializable
    data object Demo : RecapRootRoute
}

sealed class RecapRoute(
    val route: String,
    val label: String,
) : NavKey {
    @Serializable
    data object Home : RecapRoute("home", "Home")

    @Serializable
    data object Card : RecapRoute("card", "Cards")

    @Serializable
    data object Collection : RecapRoute("collection", "Collections")

    @Serializable
    data object Search : RecapRoute("search", "Search")

    @Serializable
    data object MyPage : RecapRoute("mypage", "My")

    companion object {
        val topLevelRoutes: List<RecapRoute>
            get() = listOf(Home, Card, Collection, Search, MyPage)
    }
}
