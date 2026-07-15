package com.chalkak.recap.feature.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.component.chip.RecapFilterTag
import com.chalkak.recap.core.design.component.chip.RecapFilterTagOption
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.component.topbar.CollectionDetailTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun CollectionDetailScreen(
    detail: CollectionDetailUiModel,
    selection: CollectionSelectionUiState,
    onBackClick: () -> Unit,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isSearchVisible: Boolean = false,
    onItemClick: (String) -> Unit = {},
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
        navigationBarBottomPadding
    val categoryType = detail.categoryType
    val itemImageIds = remember(detail.cards) {
        detail.cards.mapTo(linkedSetOf()) { card -> card.imageId }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isSearchVisible) {
                CollectionDetailSearchBar(
                    query = searchQuery,
                    onQueryChange = { query ->
                        onAction(CollectionAction.UpdateDetailSearchQuery(query))
                    },
                    onBackClick = { onAction(CollectionAction.HideDetailSearch) },
                )
            } else {
                CollectionDetailTopBar(
                    title = stringResource(detail.titleResId),
                    countText = pluralStringResource(
                        R.plurals.collection_recap_count,
                        detail.count,
                        detail.count,
                    ),
                    leadingIconResId = categoryType?.iconResId,
                    leadingIconTint = categoryType?.contentColor ?: RecapBlue500,
                    onBackClick = onBackClick,
                    onSearchClick = { onAction(CollectionAction.ShowDetailSearch) },
                )
            }
            CollectionDetailToolbar(
                selectedSort = detail.sort,
                selection = selection,
                canStartSelection = detail.cards.isNotEmpty(),
                onAction = onAction,
                modifier = Modifier.padding(
                    horizontal = CollectionDetailTokens.HorizontalPadding,
                    vertical = CollectionDetailTokens.ToolbarVerticalPadding,
                ),
            )
            CollectionSelectAllRow(
                visible = selection.isActive,
                itemImageIds = itemImageIds,
                selectedImageIds = selection.selectedImageIds,
                onToggleAll = {
                    onAction(CollectionAction.ToggleAllSelection(itemImageIds))
                },
                modifier = Modifier.padding(
                    horizontal = CollectionDetailTokens.HorizontalPadding,
                ),
                enabled = !selection.isDeleting,
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
                        top = CollectionDetailTokens.ListVerticalPadding,
                        bottom = CollectionDetailTokens.ListVerticalPadding + bottomContentPadding,
                    ),
                ) {
                    itemsIndexed(
                        items = detail.cards,
                        key = { _, card -> card.imageId },
                    ) { index, card ->
                        CollectionSelectableCaptureItem(
                            item = card,
                            selection = selection,
                            metadataMode = detail.cardMetadataMode,
                            onOpenClick = { onItemClick(card.imageId) },
                            onFavoriteClick = {
                                onAction(CollectionAction.ToggleFavorite(card.imageId))
                            },
                            onSelectionToggle = {
                                onAction(CollectionAction.ToggleItemSelection(card.imageId))
                            },
                            showBottomDivider = index < detail.cards.lastIndex,
                            modifier = Modifier.padding(horizontal = CollectionDetailTokens.HorizontalPadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionDetailSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CollectionDetailTokens.SearchBarHeight)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onBackClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left_24),
                    contentDescription = stringResource(
                        R.string.collection_back_content_description,
                    ),
                    tint = RecapGray900,
                    modifier = Modifier.size(24.dp),
                )
            }
            RecapSearchBar(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CollectionDetailToolbar(
    selectedSort: CollectionListSort,
    selection: CollectionSelectionUiState,
    canStartSelection: Boolean,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortOptions = listOf(
        RecapFilterTagOption(
            id = CollectionListSort.Latest.name,
            label = stringResource(R.string.collection_sort_latest),
        ),
        RecapFilterTagOption(
            id = CollectionListSort.Oldest.name,
            label = stringResource(R.string.collection_sort_oldest),
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
                onAction(CollectionAction.SetDetailSort(sort))
            },
        )
        CollectionSelectionActions(
            selection = selection,
            onStartSelection = { onAction(CollectionAction.StartSelection) },
            onCancelSelection = { onAction(CollectionAction.CancelSelection) },
            onDeleteSelected = { onAction(CollectionAction.DeleteSelected) },
            canStartSelection = canStartSelection,
        )
    }
}

private object CollectionDetailTokens {
    val HorizontalPadding = 16.dp
    val ToolbarVerticalPadding = 8.dp
    val ListVerticalPadding = 4.dp
    val SearchBarHeight = 56.dp
}

@Preview(name = "Collection Detail Populated", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionDetailPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = previewCollectionDetailUiModel(),
            selection = CollectionSelectionUiState(),
            onBackClick = {},
            onAction = {},
        )
    }
}

@Preview(
    name = "Collection Detail Selection",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun CollectionDetailSelectionPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = previewCollectionDetailUiModel(),
            selection = CollectionSelectionUiState(
                isActive = true,
                selectedImageIds = setOf("1"),
            ),
            onBackClick = {},
            onAction = {},
        )
    }
}

@Preview(name = "Collection Detail Search", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionDetailSearchPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = previewCollectionDetailUiModel(),
            selection = CollectionSelectionUiState(),
            onBackClick = {},
            onAction = {},
            searchQuery = "원피스",
            isSearchVisible = true,
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
                cardMetadataMode = ScreenshotCardMetadataMode.CategoryChip,
            ),
            selection = CollectionSelectionUiState(),
            onBackClick = {},
            onAction = {},
        )
    }
}

private fun previewCollectionDetailUiModel(): CollectionDetailUiModel {
    return CollectionDetailUiModel(
        titleResId = R.string.collection_content_type_shopping_product,
        count = 3,
        sort = CollectionListSort.Latest,
        categoryType = RecapCategoryType.ShoppingProduct,
        cardMetadataMode = ScreenshotCardMetadataMode.OrganizedDate,
        cards = listOf(
            CollectionCardItemUiModel(
                imageId = "1",
                title = "여름 원피스 주문 내역",
                summary = "가격과 배송 정보가 포함된 상품 캡처",
                contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                categoryType = RecapCategoryType.ShoppingProduct,
                createdAtMillis = 1_719_446_400_000L,
                isFavorite = true,
                thumbnailModel = null,
            ),
            CollectionCardItemUiModel(
                imageId = "2",
                title = "택배 반품 절차",
                summary = "반품 신청 전 확인해야 할 체크리스트",
                contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                categoryType = RecapCategoryType.ShoppingProduct,
                createdAtMillis = 1_718_208_000_000L,
                isFavorite = false,
                thumbnailModel = null,
            ),
            CollectionCardItemUiModel(
                imageId = "3",
                title = "노트북 가격 비교",
                summary = "쿠팡 · 컴퓨존 견적 캡처 비교",
                contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                categoryType = RecapCategoryType.ShoppingProduct,
                createdAtMillis = 1_717_862_400_000L,
                isFavorite = false,
                thumbnailModel = null,
            ),
        ),
        emptyMessageResId = R.string.collection_detail_empty,
    )
}
