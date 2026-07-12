package com.chalkak.recap.feature.collection

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.card.OrganizedRelativeTimeFormatter
import com.chalkak.recap.core.design.component.card.RecapHazeFolderCard
import com.chalkak.recap.core.design.component.chip.RecapFilterTag
import com.chalkak.recap.core.design.component.chip.RecapFilterTagOption
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.component.topbar.CollectionTopBar
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onAction: (CollectionAction) -> Unit,
    onNavigateToOrganize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showViewModeToggle = uiState.hasStoredScreenshots &&
            uiState.selectedTab == CollectionTab.Types

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CollectionTopBar(
                viewMode = if (showViewModeToggle) uiState.typeViewMode else null,
                onViewModeChange = if (showViewModeToggle) {
                    { viewMode: CollectionTypeViewMode ->
                        onAction(CollectionAction.SetTypeViewMode(viewMode))
                    }
                } else {
                    null
                },
            )
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !uiState.hasStoredScreenshots -> {
                    CollectionEmptyContent(onNavigateToOrganize = onNavigateToOrganize)
                }

                else -> {
                    CollectionOverviewContent(
                        uiState = uiState,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionEmptyContent(
    onNavigateToOrganize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, RecapGray100),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_storage_24),
                        contentDescription = null,
                        modifier = Modifier.padding(20.dp),
                        tint = RecapGray300,
                    )
                }
                Text(
                    text = stringResource(R.string.collection_empty_title),
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RecapGray900,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.collection_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecapGray500,
                    textAlign = TextAlign.Center,
                )
                RecapButton(
                    text = stringResource(R.string.collection_empty_organize_button),
                    onClick = onNavigateToOrganize,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    colors = com.chalkak.recap.core.design.component.button.RecapButtonDefaults.primaryColors(),
                    shadowElevation = 12.dp,
                )
            }
        }
    }
}

@Composable
private fun CollectionOverviewContent(
    uiState: CollectionUiState,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
            navigationBarBottomPadding

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        RecapSearchBar(
            value = uiState.searchQuery,
            onValueChange = { query -> onAction(CollectionAction.UpdateSearchQuery(query)) },
            modifier = Modifier
                .padding(horizontal = CollectionScreenTokens.HorizontalPadding)
                .padding(top = CollectionScreenTokens.SearchTopPadding),
        )
        CollectionTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = { tab -> onAction(CollectionAction.SelectTab(tab)) },
            modifier = Modifier
                .padding(horizontal = CollectionScreenTokens.HorizontalPadding)
                .padding(top = CollectionScreenTokens.ChipTopPadding),
        )
        when (uiState.selectedTab) {
            CollectionTab.Favorites -> {
                CollectionFavoritesContent(
                    favoriteItems = uiState.overview.favoriteItems,
                    selection = uiState.selection,
                    onAction = onAction,
                    bottomContentPadding = bottomContentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = CollectionScreenTokens.ContentTopPadding),
                )
            }

            CollectionTab.Types -> {
                CollectionTypesContent(
                    typeSummaries = uiState.overview.typeSummaries.filter { summary ->
                        summary.categoryType != null
                    },
                    viewMode = uiState.typeViewMode,
                    onAction = onAction,
                    bottomContentPadding = bottomContentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = CollectionScreenTokens.ContentTopPadding),
                )
            }

            CollectionTab.Others -> {
                CollectionOthersContent(
                    otherItems = uiState.overview.otherItems,
                    selectedSort = uiState.othersSort,
                    selection = uiState.selection,
                    onAction = onAction,
                    bottomContentPadding = bottomContentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = CollectionScreenTokens.ContentTopPadding),
                )
            }
        }
    }
}

@Composable
private fun CollectionTabRow(
    selectedTab: CollectionTab,
    onTabSelected: (CollectionTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CollectionTabChip(
            label = stringResource(R.string.collection_favorites_section_title),
            selected = selectedTab == CollectionTab.Favorites,
            onClick = { onTabSelected(CollectionTab.Favorites) },
        )
        CollectionTabChip(
            label = stringResource(R.string.collection_types_section_title),
            selected = selectedTab == CollectionTab.Types,
            onClick = { onTabSelected(CollectionTab.Types) },
        )
        CollectionTabChip(
            label = stringResource(R.string.collection_others_section_title),
            selected = selectedTab == CollectionTab.Others,
            onClick = { onTabSelected(CollectionTab.Others) },
        )
    }
}

@Composable
private fun CollectionTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) RecapBlue300 else RecapGray50,
        contentColor = if (selected) {
            MaterialTheme.colorScheme.background
        } else {
            RecapGray700
        },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun CollectionOthersContent(
    otherItems: List<CollectionCardItemUiModel>,
    selectedSort: CollectionListSort,
    selection: CollectionSelectionUiState,
    onAction: (CollectionAction) -> Unit,
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    if (otherItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.collection_others_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray500,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

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
    val itemImageIds = remember(otherItems) {
        otherItems.mapTo(linkedSetOf()) { item -> item.imageId }
    }

    Column(modifier = modifier.fillMaxSize()) {
        RecapFilterTag(
            options = sortOptions,
            selectedOptionId = selectedSort.name,
            onOptionSelected = { option ->
                val sort = CollectionListSort.entries.firstOrNull { it.name == option.id }
                    ?: return@RecapFilterTag
                onAction(CollectionAction.SetOthersSort(sort))
            },
            modifier = Modifier
                .padding(horizontal = CollectionScreenTokens.ListHorizontalPadding)
                .padding(bottom = 12.dp),
        )
        CollectionSectionHeader(
            leadingText = stringResource(
                R.string.collection_recap_count,
                otherItems.size,
            ),
            trailingContent = {
                CollectionSelectionActions(
                    selection = selection,
                    onStartSelection = { onAction(CollectionAction.StartSelection) },
                    onCancelSelection = { onAction(CollectionAction.CancelSelection) },
                    onDeleteSelected = { onAction(CollectionAction.DeleteSelected) },
                )
            },
            modifier = Modifier.padding(horizontal = CollectionScreenTokens.ListHorizontalPadding),
        )
        CollectionSelectAllRow(
            visible = selection.isActive,
            itemImageIds = itemImageIds,
            selectedImageIds = selection.selectedImageIds,
            onToggleAll = {
                onAction(CollectionAction.ToggleAllSelection(itemImageIds))
            },
            modifier = Modifier.padding(horizontal = CollectionScreenTokens.ListHorizontalPadding),
            enabled = !selection.isDeleting,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomContentPadding),
        ) {
            itemsIndexed(
                items = otherItems,
                key = { _, item -> item.imageId },
            ) { index, item ->
                CollectionCaptureListItem(
                    card = item,
                    onClick = { onAction(CollectionAction.OpenOtherItem(item.imageId)) },
                    onFavoriteClick = { onAction(CollectionAction.ToggleFavorite(item.imageId)) },
                    selection = selection,
                    onSelectionToggle = {
                        onAction(CollectionAction.ToggleItemSelection(item.imageId))
                    },
                    modifier = Modifier.padding(
                        horizontal = CollectionScreenTokens.ListHorizontalPadding,
                    ),
                )
                if (index < otherItems.lastIndex) {
                    HorizontalDivider(
                        color = RecapGray100,
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionFavoritesContent(
    favoriteItems: List<CollectionFavoriteItemUiModel>,
    selection: CollectionSelectionUiState,
    onAction: (CollectionAction) -> Unit,
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val nowMillis = remember { System.currentTimeMillis() }
    val visibleItems = remember(favoriteItems, nowMillis) {
        favoriteItems.filter { item ->
            OrganizedRelativeTimeFormatter.isVisible(
                organizedAtMillis = item.createdAtMillis,
                nowMillis = nowMillis,
            )
        }
    }
    val itemImageIds = remember(visibleItems) {
        visibleItems.mapTo(linkedSetOf()) { item -> item.imageId }
    }

    if (visibleItems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.collection_favorites_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray500,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        CollectionSectionHeader(
            leadingText = stringResource(
                R.string.collection_recap_count,
                visibleItems.size,
            ),
            trailingContent = {
                CollectionSelectionActions(
                    selection = selection,
                    onStartSelection = { onAction(CollectionAction.StartSelection) },
                    onCancelSelection = { onAction(CollectionAction.CancelSelection) },
                    onDeleteSelected = { onAction(CollectionAction.DeleteSelected) },
                )
            },
            modifier = Modifier.padding(horizontal = CollectionScreenTokens.ListHorizontalPadding),
        )
        CollectionSelectAllRow(
            visible = selection.isActive,
            itemImageIds = itemImageIds,
            selectedImageIds = selection.selectedImageIds,
            onToggleAll = {
                onAction(CollectionAction.ToggleAllSelection(itemImageIds))
            },
            modifier = Modifier.padding(horizontal = CollectionScreenTokens.ListHorizontalPadding),
            enabled = !selection.isDeleting,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomContentPadding),
        ) {
            itemsIndexed(
                items = visibleItems,
                key = { _, item -> item.imageId },
            ) { index, item ->
                CollectionSelectableFavoriteItem(
                    item = item,
                    selection = selection,
                    nowMillis = nowMillis,
                    onOpenClick = {
                        onAction(CollectionAction.OpenFavoriteItem(item.imageId))
                    },
                    onFavoriteClick = {
                        onAction(CollectionAction.ToggleFavorite(item.imageId))
                    },
                    onSelectionToggle = {
                        onAction(CollectionAction.ToggleItemSelection(item.imageId))
                    },
                    modifier = Modifier.padding(
                        horizontal = CollectionScreenTokens.ListHorizontalPadding,
                    ),
                )
                if (index < visibleItems.lastIndex) {
                    HorizontalDivider(
                        color = RecapGray100,
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionTypesContent(
    typeSummaries: List<CollectionTypeSummaryUiModel>,
    viewMode: CollectionTypeViewMode,
    onAction: (CollectionAction) -> Unit,
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    if (typeSummaries.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.collection_detail_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray500,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (viewMode) {
            CollectionTypeViewMode.Grid -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = CollectionScreenTokens.HorizontalPadding),
                ) {
                    CollectionSectionHeader(
                        leadingText = stringResource(
                            R.string.collection_type_folder_count,
                            typeSummaries.size,
                        ),
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = bottomContentPadding),
                        horizontalArrangement = Arrangement.spacedBy(
                            CollectionScreenTokens.TypeGridSpacing,
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            CollectionScreenTokens.TypeGridRowSpacing,
                        ),
                    ) {
                        items(
                            items = typeSummaries,
                            key = { summary -> summary.contentType.name },
                        ) { summary ->
                            CollectionTypeGridItem(
                                summary = summary,
                                onClick = {
                                    onAction(CollectionAction.OpenTypeDetail(summary.contentType))
                                },
                            )
                        }
                    }
                }
            }

            CollectionTypeViewMode.List -> {
                CollectionSectionHeader(
                    leadingText = stringResource(
                        R.string.collection_type_folder_count,
                        typeSummaries.size,
                    ),
                    modifier = Modifier.padding(horizontal = CollectionScreenTokens.HorizontalPadding),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomContentPadding),
                ) {
                    itemsIndexed(
                        items = typeSummaries,
                        key = { _, summary -> summary.contentType.name },
                    ) { index, summary ->
                        CollectionTypeListItem(
                            summary = summary,
                            onClick = {
                                onAction(CollectionAction.OpenTypeDetail(summary.contentType))
                            },
                        )
                        if (index < typeSummaries.lastIndex) {
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
private fun CollectionSectionHeader(
    leadingText: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = leadingText,
            style = MaterialTheme.typography.labelLarge,
            color = RecapGray500,
        )
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
private fun CollectionTypeGridItem(
    summary: CollectionTypeSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryType = summary.categoryType ?: return
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecapHazeFolderCard(
            category = categoryType,
            recapCount = summary.count,
            onClick = onClick,
        )
        Text(
            text = stringResource(summary.labelResId),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = RecapGray900,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CollectionTypeListItem(
    summary: CollectionTypeSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryType = summary.categoryType ?: return
    val exampleText = summary.exampleTitles.joinToString(
        separator = stringResource(R.string.collection_type_examples_separator),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                vertical = 12.dp,
                horizontal = CollectionScreenTokens.HorizontalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(27.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecapHazeFolderCard(
            category = categoryType,
            recapCount = summary.count,
            onClick = onClick,
            scale = CollectionScreenTokens.TypeListFolderScale,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = stringResource(summary.labelResId),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (exampleText.isNotBlank()) {
                Text(
                    text = exampleText,
                    style = MaterialTheme.typography.labelLarge,
                    color = RecapGray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private object CollectionScreenTokens {
    val HorizontalPadding = 20.dp
    val ListHorizontalPadding = 16.dp
    val SearchTopPadding = 8.dp
    val ChipTopPadding = 16.dp
    val ContentTopPadding = 20.dp
    val TypeGridSpacing = 19.dp
    val TypeGridRowSpacing = 39.dp
    const val TypeListFolderScale = 0.72f
}

@Preview(name = "Collection Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = CollectionUiState(
                isLoading = false,
                hasStoredScreenshots = false,
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection Favorites", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionFavoritesPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewFavoritesUiState(),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(
    name = "Collection Favorites Selection",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun CollectionFavoritesSelectionPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewFavoritesUiState(
                selection = CollectionSelectionUiState(
                    isActive = true,
                    selectedImageIds = setOf("1"),
                ),
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection Types Grid", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionTypesGridPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewTypesUiState(CollectionTypeViewMode.Grid),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection Types List", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionTypesListPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewTypesUiState(CollectionTypeViewMode.List),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection Others", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionOthersPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewOthersUiState(),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(
    name = "Collection Others Selection",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun CollectionOthersSelectionPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewOthersUiState(
                selection = CollectionSelectionUiState(
                    isActive = true,
                    selectedImageIds = setOf("other-1"),
                ),
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

private fun previewTypesUiState(viewMode: CollectionTypeViewMode): CollectionUiState {
    return CollectionUiState(
        isLoading = false,
        hasStoredScreenshots = true,
        selectedTab = CollectionTab.Types,
        typeViewMode = viewMode,
        overview = CollectionOverviewUiModel(
            typeSummaries = listOf(
                CollectionTypeSummaryUiModel(
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    labelResId = R.string.collection_content_type_shopping_product,
                    categoryType = RecapCategoryType.ShoppingProduct,
                    count = 20,
                    exampleTitles = listOf("택배 반품 절차", "노트북 가격 비교"),
                    additionalExampleCount = 0,
                    previewThumbnailModels = emptyList(),
                ),
                CollectionTypeSummaryUiModel(
                    contentType = ScreenshotContentType.PLACE_RESTAURANT,
                    labelResId = R.string.collection_content_type_place_restaurant,
                    categoryType = RecapCategoryType.PlaceRestaurant,
                    count = 23,
                    exampleTitles = listOf("성수 카페", "강남 맛집"),
                    additionalExampleCount = 0,
                    previewThumbnailModels = emptyList(),
                ),
                CollectionTypeSummaryUiModel(
                    contentType = ScreenshotContentType.SCHEDULE_RESERVATION,
                    labelResId = R.string.collection_content_type_schedule_reservation,
                    categoryType = RecapCategoryType.ScheduleReservation,
                    count = 10,
                    exampleTitles = listOf("호텔 예약", "병원 일정"),
                    additionalExampleCount = 0,
                    previewThumbnailModels = emptyList(),
                ),
            ),
        ),
    )
}

private fun previewFavoritesUiState(
    selection: CollectionSelectionUiState = CollectionSelectionUiState(),
): CollectionUiState {
    return CollectionUiState(
        isLoading = false,
        hasStoredScreenshots = true,
        selectedTab = CollectionTab.Favorites,
        overview = CollectionOverviewUiModel(
            favoriteSummary = CollectionFavoriteSummaryUiModel(
                count = 2,
                previewThumbnailModels = emptyList(),
            ),
            favoriteItems = listOf(
                CollectionFavoriteItemUiModel(
                    imageId = "1",
                    title = "연말정산 서류 목록",
                    summary = "연말정산 제출에 필요한 서류 정리",
                    categoryType = RecapCategoryType.RecordCapture,
                    createdAtMillis = System.currentTimeMillis(),
                    isFavorite = true,
                    thumbnailModel = null,
                ),
                CollectionFavoriteItemUiModel(
                    imageId = "2",
                    title = "택배 반품 절차",
                    summary = "반품 신청 전 확인해야 할 체크리스트",
                    categoryType = RecapCategoryType.ShoppingProduct,
                    createdAtMillis = System.currentTimeMillis(),
                    isFavorite = true,
                    thumbnailModel = null,
                ),
            ),
        ),
        selection = selection,
    )
}

private fun previewOthersUiState(
    selection: CollectionSelectionUiState = CollectionSelectionUiState(),
): CollectionUiState {
    return CollectionUiState(
        isLoading = false,
        hasStoredScreenshots = true,
        selectedTab = CollectionTab.Others,
        othersSort = CollectionListSort.Latest,
        overview = CollectionOverviewUiModel(
            otherItems = listOf(
                CollectionCardItemUiModel(
                    imageId = "other-1",
                    title = "연말정산 서류 목록",
                    summary = "연말정산 제출에 필요한 서류 정리",
                    contentTypeLabelResId = R.string.collection_content_type_other,
                    createdAtMillis = 1_719_446_400_000L,
                    isFavorite = false,
                    thumbnailModel = null,
                ),
                CollectionCardItemUiModel(
                    imageId = "other-2",
                    title = "미분류 메모",
                    summary = "카테고리를 특정하기 어려운 캡처",
                    contentTypeLabelResId = R.string.collection_content_type_other,
                    createdAtMillis = 1_718_208_000_000L,
                    isFavorite = false,
                    thumbnailModel = null,
                ),
            ),
        ),
        selection = selection,
    )
}
