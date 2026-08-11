package com.chalkak.recap.feature.collection

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun CollectionDetailPopulatedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = previewCollectionDetailUiModel(),
            selection = CollectionSelectionUiState(),
            onBackClick = {},
            onAction = {},
        )
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun CollectionDetailEmptyScreenshot() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = CollectionDetailUiModel(
                titleResId = R.string.category_type_shopping_product,
                count = 0,
                sort = CollectionListSort.Latest,
                categoryType = RecapCategoryType.ShoppingProduct,
                cards = emptyList(),
                emptyMessageResId = R.string.collection_detail_empty,
                cardMetadataMode = ScreenshotCardMetadataMode.OrganizedDate,
            ),
            selection = CollectionSelectionUiState(),
            onBackClick = {},
            onAction = {},
        )
    }
}
