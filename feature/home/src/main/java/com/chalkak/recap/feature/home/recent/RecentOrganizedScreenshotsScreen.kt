package com.chalkak.recap.feature.home.recent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.OrganizedRelativeTimeFormatter
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.topbar.RecentOrganizedScreenshotsTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography

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
                title = stringResource(R.string.home_recent_organized_screenshots_title),
                onBackClick = { onAction(RecentOrganizedScreenshotsAction.NavigateBack) },
                onSearchClick = { onAction(RecentOrganizedScreenshotsAction.OpenSearch) },
            )
            if (visibleItems.isNotEmpty()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = RecapGray700)) {
                            append(visibleItems.size.toString())
                        }
                        append(" ")
                        withStyle(SpanStyle(color = RecapGray500)) {
                            append(
                                pluralStringResource(
                                    R.plurals.recap_haze_folder_card_recap_label,
                                    visibleItems.size,
                                ),
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(
                            horizontal = RecentOrganizedScreenshotsTokens.HorizontalPadding,
                            vertical = RecentOrganizedScreenshotsTokens.CountVerticalPadding,
                        )
                        .align(alignment = Alignment.End),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (visibleItems.isEmpty()) {
                RecentOrganizedScreenshotsEmptyContent(
                    onImportClick = {
                        onAction(RecentOrganizedScreenshotsAction.StartImport)
                    },
                )
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
private fun RecentOrganizedScreenshotsEmptyContent(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = RecentOrganizedScreenshotsTokens.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_character_1),
            contentDescription = stringResource(R.string.home_empty_character_content_description),
            modifier = Modifier
                .size(
                    width = RecentOrganizedScreenshotsTokens.EmptyCharacterWidth,
                    height = RecentOrganizedScreenshotsTokens.EmptyCharacterHeight,
                )
                .offset(x = RecentOrganizedScreenshotsTokens.EmptyCharacterOffsetX),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.home_empty_title),
            style = RecapTypography.RecapHeading3,
            fontWeight = FontWeight.Bold,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.home_empty_description),
            style = RecapTypography.RecapBody2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.EmptyDescriptionSpacing))
        RecapButton(
            text = stringResource(R.string.home_empty_import_button),
            onClick = onImportClick,
            size = RecapButtonSize.Medium,
            colors = RecapButtonDefaults.secondaryColors(),
            modifier = Modifier.widthIn(min = RecentOrganizedScreenshotsTokens.EmptyImportButtonMinWidth),
            textStyle = RecapTypography.RecapHeading3,
        )
    }
}

private object RecentOrganizedScreenshotsTokens {
    val HorizontalPadding = 16.dp
    val CountVerticalPadding = 8.dp
    val ListVerticalPadding = 4.dp
    val EmptyCharacterWidth = 122.67.dp
    val EmptyCharacterHeight = 89.dp
    val EmptyCharacterOffsetX = 6.dp
    val EmptyCharacterSpacing = 20.dp
    val EmptyTitleSpacing = 13.dp
    val EmptyDescriptionSpacing = 23.dp
    val EmptyImportButtonMinWidth = 200.dp
}

@Preview(
    name = "Recent Organized Screenshots",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun RecentOrganizedScreenshotsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsPreviewUiState,
            onAction = {},
        )
    }
}

@Preview(
    name = "Recent Organized Screenshots Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun RecentOrganizedScreenshotsScreenEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(),
            onAction = {},
        )
    }
}
