package com.chalkak.recap.app

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.chalkak.recap.R
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

sealed interface RecapRoute : NavKey {
    @Serializable
    data object Home : RecapRoute

    @Serializable
    data object Collection : RecapRoute

    @Serializable
    data object MyPage : RecapRoute

    companion object {
        val topLevelRoutes: List<RecapRoute>
            get() = listOf(Home, Collection)
    }
}

@get:StringRes
val RecapRoute.labelResId: Int
    get() = when (this) {
        RecapRoute.Home -> R.string.bottom_nav_home
        RecapRoute.Collection -> R.string.bottom_nav_collection
        RecapRoute.MyPage -> R.string.bottom_nav_my_page
    }
