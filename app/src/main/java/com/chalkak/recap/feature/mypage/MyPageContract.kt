package com.chalkak.recap.feature.mypage

data class MyPageUiState(
    val title: String = "My Page",
    val description: String = "Settings, trash, excluded cards, and service information will be managed here.",
)

sealed interface MyPageAction {
    data object OpenSettings : MyPageAction
}
