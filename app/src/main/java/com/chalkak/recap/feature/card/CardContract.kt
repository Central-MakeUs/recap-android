package com.chalkak.recap.feature.card

data class CardUiState(
    val title: String = "Cards",
    val description: String = "Generated information cards will be listed here.",
)

sealed interface CardAction {
    data class OpenCard(val cardId: String) : CardAction
}
