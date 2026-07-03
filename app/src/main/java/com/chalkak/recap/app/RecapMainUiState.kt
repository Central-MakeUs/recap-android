package com.chalkak.recap.app

data class RecapMainUiState(
    val selectedRoute: MainTabRoute = MainTabRoute.Home,
    val globalMessage: String? = null,
)
