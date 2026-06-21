package com.chalkak.recap.app

data class RecapMainUiState(
    val startRoute: RecapRoute = RecapRoute.Home,
    val selectedRoute: RecapRoute = RecapRoute.Home,
    val globalMessage: String? = null,
)
