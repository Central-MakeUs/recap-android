package com.chalkak.recap.feature.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.chip.RecapFilterTag
import com.chalkak.recap.core.design.component.chip.RecapFilterTagOption
import com.chalkak.recap.core.design.component.topbar.CollectionDetailTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray500

@Composable
fun CollectionDetailScreen(
    detail: CollectionDetailUiModel,
    onBackClick: () -> Unit,
    onSortSelected: (CollectionListSort) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    onSelectClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
        navigationBarBottomPadding
    val categoryType = detail.categoryType

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CollectionDetailTopBar(
                title = stringResource(detail.titleResId),
                countText = stringResource(R.string.collection_recap_count, detail.count),
                leadingIconResId = categoryType?.iconResId,
                leadingIconTint = categoryType?.contentColor ?: RecapBlue500,
                onBackClick = onBackClick,
                onSearchClick = onSearchClick,
            )
            CollectionDetailToolbar(
                selectedSort = detail.sort,
                onSortSelected = onSortSelected,
                onSelectClick = onSelectClick,
                modifier = Modifier.padding(
                    horizontal = CollectionDetailTokens.HorizontalPadding,
                    vertical = CollectionDetailTokens.ToolbarVerticalPadding,
                ),
            )
            if (detail.cards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(detail.emptyMessageResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RecapGray500,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = CollectionDetailTokens.HorizontalPadding,
                        top = CollectionDetailTokens.ListVerticalPadding,
                        end = CollectionDetailTokens.HorizontalPadding,
                        bottom = CollectionDetailTokens.ListVerticalPadding + bottomContentPadding,
                    ),
                ) {
                    itemsIndexed(
                        items = detail.cards,
                        key = { _, card -> card.imageId },
                    ) { index, card ->
                        CollectionCaptureListItem(
                            card = card,
                            onClick = { onItemClick(card.imageId) },
                            onFavoriteClick = { onFavoriteClick(card.imageId) },
                        )
                        if (index < detail.cards.lastIndex) {
                            HorizontalDivider(
                                color = RecapGray100,
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionDetailToolbar(
    selectedSort: CollectionListSort,
    onSortSelected: (CollectionListSort) -> Unit,
    onSelectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortOptions = listOf(
        RecapFilterTagOption(
            id = CollectionListSort.Latest.name,
            label = stringResource(R.string.collection_sort_latest),
        ),
        RecapFilterTagOption(
            id = CollectionListSort.Name.name,
            label = stringResource(R.string.collection_sort_name),
        ),
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecapFilterTag(
            options = sortOptions,
            selectedOptionId = selectedSort.name,
            onOptionSelected = { option ->
                val sort = CollectionListSort.entries.firstOrNull { it.name == option.id }
                    ?: return@RecapFilterTag
                onSortSelected(sort)
            },
        )
        Text(
            text = stringResource(R.string.collection_select_action),
            style = MaterialTheme.typography.labelLarge,
            color = RecapGray500,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onSelectClick,
            ),
        )
    }
}

private object CollectionDetailTokens {
    val HorizontalPadding = 20.dp
    val ToolbarVerticalPadding = 8.dp
    val ListVerticalPadding = 4.dp
}

@Preview(name = "Collection Detail Populated", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionDetailPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = CollectionDetailUiModel(
                titleResId = R.string.collection_content_type_shopping_product,
                count = 3,
                sort = CollectionListSort.Latest,
                categoryType = RecapCategoryType.ShoppingProduct,
                cards = listOf(
                    CollectionCardItemUiModel(
                        imageId = "1",
                        title = "여름 원피스 주문 내역",
                        summary = "가격과 배송 정보가 포함된 상품 캡처",
                        contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                        createdAtMillis = 1_719_446_400_000L,
                        isFavorite = true,
                        thumbnailModel = null,
                    ),
                    CollectionCardItemUiModel(
                        imageId = "2",
                        title = "택배 반품 절차",
                        summary = "반품 신청 전 확인해야 할 체크리스트",
                        contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                        createdAtMillis = 1_718_208_000_000L,
                        isFavorite = false,
                        thumbnailModel = null,
                    ),
                    CollectionCardItemUiModel(
                        imageId = "3",
                        title = "노트북 가격 비교",
                        summary = "쿠팡 · 컴퓨존 견적 캡처 비교",
                        contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                        createdAtMillis = 1_717_862_400_000L,
                        isFavorite = false,
                        thumbnailModel = null,
                    ),
                ),
                emptyMessageResId = R.string.collection_detail_empty,
            ),
            onBackClick = {},
            onSortSelected = {},
            onFavoriteClick = {},
        )
    }
}

@Preview(name = "Collection Detail Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionDetailEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = CollectionDetailUiModel(
                titleResId = R.string.collection_favorites_detail_title,
                count = 0,
                sort = CollectionListSort.Latest,
                cards = emptyList(),
                emptyMessageResId = R.string.collection_favorites_empty,
            ),
            onBackClick = {},
            onSortSelected = {},
            onFavoriteClick = {},
        )
    }
}
