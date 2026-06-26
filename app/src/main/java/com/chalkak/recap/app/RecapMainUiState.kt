package com.chalkak.recap.app

data class RecapMainUiState(
    val selectedRoute: RecapRoute = RecapRoute.Home,
    val globalMessage: String? = null,
)
