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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.res.pluralStringResource
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
import com.chalkak.recap.core.design.component.card.RecapHazeFolderCard
import com.chalkak.recap.core.design.component.icon.RecapCategoryIcon
import com.chalkak.recap.core.design.component.icon.RecapCategoryIconSize
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.component.topbar.CollectionTopBar
import com.chalkak.recap.core.design.component.topbar.CollectionTypeViewMode
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

@Composable
fun CollectionScreen(
    uiState: CollectionUiState,
    onAction: (CollectionAction) -> Unit,
    onNavigateToOrganize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showViewModeToggle = uiState.hasStoredScreenshots

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
        CollectionUnifiedOverview(
            favoriteCount = uiState.overview.favoriteSummary.count,
            typeSummaries = uiState.overview.typeSummaries,
            viewMode = uiState.typeViewMode,
            onAction = onAction,
            bottomContentPadding = bottomContentPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = CollectionScreenTokens.ContentTopPadding),
        )
    }
}

@Composable
private fun CollectionUnifiedOverview(
    favoriteCount: Int,
    typeSummaries: List<CollectionTypeSummaryUiModel>,
    viewMode: CollectionTypeViewMode,
    onAction: (CollectionAction) -> Unit,
    bottomContentPadding: Dp,
    modifier: Modifier = Modifier,
) {
    when (viewMode) {
        CollectionTypeViewMode.Grid -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = CollectionScreenTokens.HorizontalPadding),
                contentPadding = PaddingValues(bottom = bottomContentPadding),
                horizontalArrangement = Arrangement.spacedBy(CollectionScreenTokens.TypeGridSpacing),
                verticalArrangement = Arrangement.spacedBy(CollectionScreenTokens.TypeGridRowSpacing),
            ) {
                item(
                    key = "favorites-entry",
                    contentType = "favorites-entry",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    CollectionFavoritesEntryCard(
                        count = favoriteCount,
                        onClick = { onAction(CollectionAction.OpenFavoriteDetail) },
                        modifier = Modifier.padding(bottom = CollectionScreenTokens.FavoriteCardBottomPadding),
                    )
                }
                items(
                    items = typeSummaries,
                    key = { summary -> summary.contentType.name },
                    contentType = { "category-grid" },
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

        CollectionTypeViewMode.List -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomContentPadding),
            ) {
                item(
                    key = "favorites-entry",
                    contentType = "favorites-entry",
                ) {
                    CollectionFavoritesEntryCard(
                        count = favoriteCount,
                        onClick = { onAction(CollectionAction.OpenFavoriteDetail) },
                        modifier = Modifier
                            .padding(horizontal = CollectionScreenTokens.HorizontalPadding)
                            .padding(bottom = CollectionScreenTokens.FavoriteCardBottomPadding),
                    )
                }
                itemsIndexed(
                    items = typeSummaries,
                    key = { _, summary -> summary.contentType.name },
                    contentType = { _, _ -> "category-list" },
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

@Composable
private fun CollectionTypeGridItem(
    summary: CollectionTypeSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecapHazeFolderCard(
            category = summary.categoryType,
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
        Text(
            text = pluralStringResource(
                R.plurals.collection_recap_count,
                summary.count,
                summary.count,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = RecapGray500,
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
    val exampleText = summary.exampleTitles.joinToString(
        separator = stringResource(R.string.collection_type_examples_separator),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CollectionScreenTokens.MinimumTouchTarget)
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
        RecapCategoryIcon(
            category = summary.categoryType,
            size = RecapCategoryIconSize.Compact,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(summary.labelResId),
                    style = RecapHeading3,
                    color = Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.collection_recap_count,
                        summary.count,
                        summary.count,
                    ),
                    style = RecapCaption2,
                    color = RecapGray300,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (exampleText.isNotBlank()) {
                Text(
                    text = exampleText,
                    style = RecapCaption1,
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
    val SearchTopPadding = 8.dp
    val ContentTopPadding = 20.dp
    val FavoriteCardBottomPadding = 20.dp
    val TypeGridSpacing = 19.dp
    val TypeGridRowSpacing = 24.dp
    val MinimumTouchTarget = 48.dp
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

@Preview(name = "Collection Overview Grid", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionOverviewGridPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewOverviewUiState(CollectionTypeViewMode.Grid),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection Overview List", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionOverviewListPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewOverviewUiState(CollectionTypeViewMode.List),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(
    name = "Collection Overview Zero Favorites",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun CollectionOverviewZeroFavoritesPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = previewOverviewUiState(
                viewMode = CollectionTypeViewMode.Grid,
                favoriteCount = 0,
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

private fun previewOverviewUiState(
    viewMode: CollectionTypeViewMode,
    favoriteCount: Int = 4,
): CollectionUiState {
    return CollectionUiState(
        isLoading = false,
        hasStoredScreenshots = true,
        typeViewMode = viewMode,
        overview = CollectionOverviewUiModel(
            favoriteSummary = CollectionFavoriteSummaryUiModel(count = favoriteCount),
            typeSummaries = listOf(
                CollectionTypeSummaryUiModel(
                    contentType = ScreenshotContentType.SHOPPING,
                    labelResId = R.string.home_category_shopping_product,
                    categoryType = RecapCategoryType.ShoppingProduct,
                    count = 20,
                    exampleTitles = listOf("택배 반품 절차", "노트북 가격 비교"),
                    additionalExampleCount = 0,
                ),
                CollectionTypeSummaryUiModel(
                    contentType = ScreenshotContentType.PLACE,
                    labelResId = R.string.home_category_place_restaurant,
                    categoryType = RecapCategoryType.PlaceRestaurant,
                    count = 23,
                    exampleTitles = listOf("성수 카페", "강남 맛집"),
                    additionalExampleCount = 0,
                ),
                CollectionTypeSummaryUiModel(
                    contentType = ScreenshotContentType.ETC,
                    labelResId = R.string.home_category_other,
                    categoryType = RecapCategoryType.Other,
                    count = 2,
                    exampleTitles = listOf("미분류 메모"),
                    additionalExampleCount = 0,
                ),
            ),
        ),
    )
}
