package com.chalkak.recap.feature.collection

data class CollectionUiState(
    val title: String = "Collections",
    val description: String = "Saved purpose-based card collections will be organized here.",
)

sealed interface CollectionAction {
    data class OpenCollection(val collectionId: String) : CollectionAction
}
