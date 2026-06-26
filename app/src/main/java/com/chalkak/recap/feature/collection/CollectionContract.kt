package com.chalkak.recap.feature.collection

import androidx.annotation.StringRes
import com.chalkak.recap.R

data class CollectionUiState(
    @StringRes val titleResId: Int = R.string.collection_title,
    @StringRes val descriptionResId: Int = R.string.collection_description,
)

sealed interface CollectionAction {
    data class OpenCollection(val collectionId: String) : CollectionAction
}
