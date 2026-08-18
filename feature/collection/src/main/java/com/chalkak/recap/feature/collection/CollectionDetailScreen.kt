package com.chalkak.recap.feature.collection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.animation.recapScreenshotCardItemAnimation
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.component.chip.RecapSortToggle
import com.chalkak.recap.core.design.component.divider.RecapSectionDivider
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.component.topbar.CollectionDetailTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun CollectionDetailScreen(
    detail: CollectionDetailUiModel,
    selection: CollectionSelectionUiState,
    onBackClick: () -> Unit,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    isSearchVisible: Boolean = false,
    onItemClick: (Long) -> Unit = {},
    onItemEditClick: (Long) -> Unit = {},
    pendingDeleteCaptureId: Long? = null,
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
            navigationBarBottomPadding
    val categoryType = detail.categoryType
    val swipeActionsEnabled = categoryType != null && !selection.isActive
    var revealedCaptureId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(swipeActionsEnabled) {
        if (!swipeActionsEnabled) {
            revealedCaptureId = null
        }
    }
    val listState = rememberLazyListState()
    LaunchedEffect(detail.sort, detail.cards.firstOrNull()?.captureId) {
        listState.scrollToItem(0)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = isSearchVisible,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(CollectionDetailTokens.SearchToggleFadeMillis),
                    ) togetherWith fadeOut(
                        animationSpec = tween(CollectionDetailTokens.SearchToggleFadeMillis),
                    )
                },
                label = "collectionDetailSearchToggle",
            ) { searchVisible ->
                if (searchVisible) {
                    CollectionDetailSearchBar(
                        query = searchQuery,
                        onQueryChange = { query ->
                            onAction(CollectionAction.UpdateDetailSearchQuery(query))
                        },
                        onSearch = { onAction(CollectionAction.SubmitDetailSearch) },
                        onBackClick = { onAction(CollectionAction.HideDetailSearch) },
                        placeholder = stringResource(
                            R.string.collection_detail_search_placeholder,
                            stringResource(detail.titleResId),
                        ),
                    )
                } else {
                    CollectionDetailTopBar(
                        title = stringResource(detail.titleResId),
                        leadingIconResId = categoryType?.iconResId,
                        leadingIconTint = categoryType?.borderColor ?: RecapBlue500,
                        onBackClick = onBackClick,
                        onSearchClick = { onAction(CollectionAction.ShowDetailSearch) },
                    )
                }
            }
            CollectionDetailToolbar(
                selectedSort = detail.sort,
                selection = selection,
                canStartSelection = detail.cards.isNotEmpty(),
                showSelectionActions = detail.categoryType != null,
                onAction = onAction,
                modifier = Modifier.padding(
                    horizontal = CollectionDetailTokens.HorizontalPadding,
                    vertical = CollectionDetailTokens.ToolbarVerticalPadding,
                ),
            )
            RecapSectionDivider(
                height = 6.dp
            )
            CollectionDetailRecapCount(
                count = detail.count,
                modifier = Modifier.padding(
                    horizontal = CollectionDetailTokens.HorizontalPadding,
                    vertical = CollectionDetailTokens.RecapCountVerticalPadding,
                ),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    top = CollectionDetailTokens.ListVerticalPadding,
                    bottom = CollectionDetailTokens.ListVerticalPadding + bottomContentPadding,
                ),
            ) {
                items(
                    items = detail.cards,
                    key = { card -> card.captureId },
                ) { card ->
                    CollectionSelectableCaptureItem(
                        item = card,
                        selection = selection,
                        metadataMode = detail.cardMetadataMode,
                        onOpenClick = { onItemClick(card.captureId) },
                        onFavoriteClick = {
                            onAction(CollectionAction.ToggleFavorite(card.captureId))
                        },
                        onSelectionToggle = {
                            onAction(CollectionAction.ToggleItemSelection(card.captureId))
                        },
                        swipeActionsEnabled = swipeActionsEnabled,
                        swipeRevealed = revealedCaptureId == card.captureId,
                        onSwipeRevealedChange = { revealed ->
                            revealedCaptureId = when {
                                revealed -> card.captureId
                                revealedCaptureId == card.captureId -> null
                                else -> revealedCaptureId
                            }
                        },
                        onSwipeDragStarted = {
                            if (
                                revealedCaptureId != null &&
                                revealedCaptureId != card.captureId
                            ) {
                                revealedCaptureId = null
                            }
                        },
                        onEditClick = {
                            revealedCaptureId = null
                            onItemEditClick(card.captureId)
                        },
                        onDeleteClick = {
                            revealedCaptureId = null
                            onAction(CollectionAction.RequestDeleteItem(card.captureId))
                        },
                        modifier = Modifier.recapScreenshotCardItemAnimation(),
                    )
                }
                if (detail.cards.isEmpty()) {
                    item(key = "collection_detail_empty") {
                        CollectionDetailEmptyContent(
                            messageResId = detail.emptyMessageResId,
                            modifier = Modifier.fillParentMaxSize(),
                        )
                    }
                }
                if (detail.isLoadingMore) {
                    item(key = "collection_detail_loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = RecapBlue500,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }

            LaunchedEffect(listState, detail.cards.size, detail.hasNext, detail.isLoadingMore) {
                snapshotFlow {
                    val secondLastIndex = detail.cards.lastIndex - 1
                    secondLastIndex >= 0 &&
                            listState.layoutInfo.visibleItemsInfo.any { item ->
                                item.index == secondLastIndex
                            }
                }
                    .distinctUntilChanged()
                    .filter { isSecondLastVisible -> isSecondLastVisible }
                    .collect {
                        if (detail.hasNext && !detail.isLoadingMore) {
                            onAction(CollectionAction.LoadMoreDetailSearch)
                        }
                    }
            }
        }
    }

    if (selection.showDeleteConfirmDialog) {
        RecapPopup(
            title = stringResource(
                R.string.collection_delete_confirm_title,
                selection.selectedCount,
            ),
            description = stringResource(R.string.collection_delete_confirm_description),
            confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onConfirmClick = { onAction(CollectionAction.ConfirmDeleteSelected) },
            onCancelClick = { onAction(CollectionAction.DismissDeleteConfirmDialog) },
            onDismissRequest = { onAction(CollectionAction.DismissDeleteConfirmDialog) },
            confirmButtonColor = RecapError,
        )
    }

    if (pendingDeleteCaptureId != null) {
        RecapPopup(
            title = stringResource(R.string.screenshot_delete_confirm_title),
            description = stringResource(R.string.screenshot_delete_confirm_description),
            confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onConfirmClick = { onAction(CollectionAction.ConfirmDeleteItem) },
            onCancelClick = { onAction(CollectionAction.DismissDeleteItem) },
            onDismissRequest = { onAction(CollectionAction.DismissDeleteItem) },
            confirmButtonColor = RecapError,
        )
    }
}

@Composable
private fun CollectionDetailSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(CollectionDetailTokens.SearchBarHeight)
                .padding(horizontal = CollectionDetailTokens.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RecapSearchBar(
                value = query,
                onValueChange = onQueryChange,
                onSearch = onSearch,
                autoFocus = true,
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.search_screen_close),
                style = RecapBody2,
                color = RecapGray500,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onBackClick,
                ),
            )
        }
    }
}

@Composable
private fun CollectionDetailEmptyContent(
    messageResId: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CollectionDetailTokens.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_character_1),
            contentDescription = stringResource(
                R.string.collection_detail_empty_character_content_description,
            ),
            modifier = Modifier
                .size(
                    width = CollectionDetailTokens.EmptyCharacterWidth,
                    height = CollectionDetailTokens.EmptyCharacterHeight,
                )
                .offset(x = CollectionDetailTokens.EmptyCharacterOffsetX),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(CollectionDetailTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(messageResId),
            style = RecapHeading3,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CollectionDetailRecapCount(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = count.toString(),
            style = RecapHeading4,
            color = RecapGray900,
        )
        Text(
            text = pluralStringResource(
                R.plurals.recap_haze_folder_card_recap_label,
                count,
            ),
            style = RecapBody2,
            color = RecapGray500,
        )
    }
}

@Composable
private fun CollectionDetailToolbar(
    selectedSort: CollectionListSort,
    selection: CollectionSelectionUiState,
    canStartSelection: Boolean,
    showSelectionActions: Boolean,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortLabel = stringResource(
        when (selectedSort) {
            CollectionListSort.Latest -> R.string.collection_sort_latest
            CollectionListSort.Oldest -> R.string.collection_sort_oldest
        },
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecapSortToggle(
            label = sortLabel,
            onClick = {
                val nextSort = when (selectedSort) {
                    CollectionListSort.Latest -> CollectionListSort.Oldest
                    CollectionListSort.Oldest -> CollectionListSort.Latest
                }
                onAction(CollectionAction.SetDetailSort(nextSort))
            },
        )
        if (showSelectionActions) {
            CollectionSelectionActions(
                selection = selection,
                onStartSelection = { onAction(CollectionAction.StartSelection) },
                onCancelSelection = { onAction(CollectionAction.CancelSelection) },
                onDeleteSelected = { onAction(CollectionAction.DeleteSelected) },
                canStartSelection = canStartSelection,
            )
        }
    }
}

private object CollectionDetailTokens {
    val HorizontalPadding = 16.dp
    val ToolbarVerticalPadding = 8.dp
    val RecapCountVerticalPadding = 12.dp
    val ListVerticalPadding = 4.dp
    val SearchBarHeight = 56.dp
    const val SearchToggleFadeMillis = 150
    val EmptyCharacterWidth = 122.67.dp
    val EmptyCharacterHeight = 89.dp
    val EmptyCharacterOffsetX = 6.dp
    val EmptyCharacterSpacing = 20.dp
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
                selectedCaptureIds = setOf(1L),
            ),
            onBackClick = {},
            onAction = {},
        )
    }
}

@Preview(
    name = "Collection Detail Delete Confirm",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun CollectionDetailDeleteConfirmPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = previewCollectionDetailUiModel(),
            selection = CollectionSelectionUiState(
                isActive = true,
                selectedCaptureIds = setOf(1L, 2L),
                showDeleteConfirmDialog = true,
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

internal fun previewCollectionDetailUiModel(): CollectionDetailUiModel {
    return CollectionDetailUiModel(
        titleResId = R.string.category_type_shopping_product,
        count = 3,
        sort = CollectionListSort.Latest,
        categoryType = RecapCategoryType.ShoppingProduct,
        cardMetadataMode = ScreenshotCardMetadataMode.OrganizedDate,
        cards = listOf(
            CollectionCardItemUiModel(
                captureId = 1L,
                title = "여름 원피스 주문 내역",
                summary = "가격과 배송 정보가 포함된 상품 캡처",
                contentTypeLabelResId = R.string.category_type_shopping_product,
                categoryType = RecapCategoryType.ShoppingProduct,
                organizedAtMillis = 1_719_446_400_000L,
                isFavorite = true,
                thumbnailModel = null,
            ),
            CollectionCardItemUiModel(
                captureId = 2L,
                title = "택배 반품 절차",
                summary = "반품 신청 전 확인해야 할 체크리스트",
                contentTypeLabelResId = R.string.category_type_shopping_product,
                categoryType = RecapCategoryType.ShoppingProduct,
                organizedAtMillis = 1_718_208_000_000L,
                isFavorite = false,
                thumbnailModel = null,
            ),
            CollectionCardItemUiModel(
                captureId = 3L,
                title = "노트북 가격 비교",
                summary = "쿠팡 · 컴퓨존 견적 캡처 비교",
                contentTypeLabelResId = R.string.category_type_shopping_product,
                categoryType = RecapCategoryType.ShoppingProduct,
                organizedAtMillis = 1_717_862_400_000L,
                isFavorite = false,
                thumbnailModel = null,
            ),
        ),
        emptyMessageResId = R.string.collection_detail_empty,
    )
}
