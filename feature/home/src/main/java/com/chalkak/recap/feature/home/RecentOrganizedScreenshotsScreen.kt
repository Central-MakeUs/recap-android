package com.chalkak.recap.feature.home

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.card.OrganizedRelativeTimeFormatter
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.topbar.RecentOrganizedScreenshotsTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun RecentOrganizedScreenshotsScreen(
    uiState: RecentOrganizedScreenshotsUiState,
    onAction: (RecentOrganizedScreenshotsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val nowMillis = remember { System.currentTimeMillis() }
    val visibleItems = remember(uiState.items, nowMillis) {
        uiState.items.filter { item ->
            OrganizedRelativeTimeFormatter.isVisible(
                organizedAtMillis = item.organizedAtMillis,
                nowMillis = nowMillis,
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecentOrganizedScreenshotsTopBar(
                onBackClick = { onAction(RecentOrganizedScreenshotsAction.NavigateBack) },
                onSearchClick = { onAction(RecentOrganizedScreenshotsAction.OpenSearch) },
            )
            RecentOrganizedScreenshotsTitle(
                count = visibleItems.size,
                modifier = Modifier.padding(
                    horizontal = RecentOrganizedScreenshotsTokens.HorizontalPadding,
                    vertical = RecentOrganizedScreenshotsTokens.TitleVerticalPadding,
                ),
            )
            if (visibleItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.home_recent_organized_screenshots_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RecapGray500,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = RecentOrganizedScreenshotsTokens.ListVerticalPadding +
                            navigationBarBottomPadding,
                    ),
                ) {
                    itemsIndexed(
                        items = visibleItems,
                        key = { _, item -> item.id },
                    ) { index, item ->
                        ScreenshotCard(
                            thumbnailModel = item.thumbnailModel,
                            categoryType = item.categoryType,
                            title = item.title,
                            description = item.description,
                            isFavorite = item.isFavorite,
                            onClick = { onAction(RecentOrganizedScreenshotsAction.SelectItem(item.id)) },
                            onFavoriteClick = {
                                onAction(RecentOrganizedScreenshotsAction.ToggleFavorite(item.id))
                            },
                            horizontalContentPadding = 0.dp,
                            showBottomDivider = index < visibleItems.lastIndex,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = RecentOrganizedScreenshotsTokens.HorizontalPadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentOrganizedScreenshotsTitle(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.home_recent_organized_screenshots_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = RecapGray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.collection_recap_count, count),
            style = MaterialTheme.typography.labelLarge,
            color = RecapGray500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private object RecentOrganizedScreenshotsTokens {
    val HorizontalPadding = 16.dp
    val TitleVerticalPadding = 8.dp
    val ListVerticalPadding = 4.dp
}

@Preview(name = "Recent Organized Screenshots", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RecentOrganizedScreenshotsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsPreviewUiState,
            onAction = {},
        )
    }
}

@Preview(name = "Recent Organized Screenshots Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RecentOrganizedScreenshotsScreenEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(),
            onAction = {},
        )
    }
}
