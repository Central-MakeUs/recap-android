package com.chalkak.recap.feature.home

data class HomeUiState(
    val title: String = "Home",
    val description: String = "Recent screenshot cards and cleanup status will appear here.",
)

sealed interface HomeAction {
    data object StartImport : HomeAction
    data object EnterDemo : HomeAction
}
