package com.chalkak.recap.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface RecapRootRoute : NavKey {
    @Serializable
    data object Onboarding : RecapRootRoute

    @Serializable
    data object Main : RecapRootRoute

    @Serializable
    data object Demo : RecapRootRoute
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
            get() = listOf(Home, Collection, MyPage)
    }
}

val RecapRoute.label: String
    get() = when (this) {
        RecapRoute.Home -> "홈"
        RecapRoute.Collection -> "컬렉션"
        RecapRoute.MyPage -> "마이페이지"
    }
