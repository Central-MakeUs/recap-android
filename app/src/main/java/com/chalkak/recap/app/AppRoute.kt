package com.chalkak.recap.app

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.chalkak.recap.core.design.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface RecapRootRoute : NavKey {
    @Serializable
    data object Onboarding : RecapRootRoute

    @Serializable
    data object Main : RecapRootRoute

    @Serializable
    data object Developer : RecapRootRoute
}

sealed interface AppRoute : NavKey {
    @Serializable
    data object MainTabs : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object NotificationSettings : AppRoute

    @Serializable
    data object UsageGuide : AppRoute

    @Serializable
    data object ShareFavoriteGuide : AppRoute

    @Serializable
    data object DataManagement : AppRoute

    @Serializable
    data object AccountManagement : AppRoute

    @Serializable
    data object PrivacyGuide : AppRoute

    @Serializable
    data object Search : AppRoute

    @Serializable
    data object RecentOrganizedScreenshots : AppRoute

    @Serializable
    data class Screenshot(val imageId: String) : AppRoute
}

sealed interface MainTabRoute : NavKey {
    @Serializable
    data object Home : MainTabRoute

    @Serializable
    data object Collection : MainTabRoute

    companion object {
        val topLevelRoutes: List<MainTabRoute>
            get() = listOf(Home, Collection)
    }
}

@get:StringRes
val MainTabRoute.labelResId: Int
    get() = when (this) {
        MainTabRoute.Home -> R.string.bottom_nav_home
        MainTabRoute.Collection -> R.string.bottom_nav_collection
    }
