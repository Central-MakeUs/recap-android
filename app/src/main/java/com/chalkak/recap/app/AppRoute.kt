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
    data object MyPage : AppRoute

    @Serializable
    data object MyPageNotificationSettings : AppRoute

    @Serializable
    data object MyPageUploadGuide : AppRoute

    @Serializable
    data object MyPageDataManagement : AppRoute

    @Serializable
    data object MyPagePrivacyGuide : AppRoute

    @Serializable
    data object MyPageServiceInfo : AppRoute

    @Serializable
    data object Search : AppRoute

    @Serializable
    data object RecentOrganizedScreenshots : AppRoute

    @Serializable
    data object Organize : AppRoute
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
