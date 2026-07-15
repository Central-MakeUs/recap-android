package com.chalkak.recap.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.HomeFavoriteCard
import com.chalkak.recap.core.design.component.card.RecentOrganizedScreenshotCard
import com.chalkak.recap.core.design.component.icon.RecapCategoryIcon
import com.chalkak.recap.core.design.component.icon.RecapCategoryIconSize
import com.chalkak.recap.core.design.component.topbar.HomeTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    uiState: HomeUiState = HomeUiState(),
    analysisProgress: HomeAnalysisProgressUiModel = HomeAnalysisProgressUiModel(),
    onAction: (HomeAction) -> Unit = {},
    onLogoClick: (() -> Unit)? = null,
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
            navigationBarBottomPadding

    Column(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)
            .background(MaterialTheme.colorScheme.background),
    ) {
        HomeTopBar(
            onSettingsClick = { onAction(HomeAction.OpenSettings) },
            onSearchClick = { onAction(HomeAction.OpenSearch) },
            onLogoClick = onLogoClick,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = HomeScreenTokens.VerticalPadding,
                    bottom = HomeScreenTokens.VerticalPadding + bottomContentPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.SectionSpacing),
        ) {
            if (analysisProgress.isRunning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HomeScreenTokens.HorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.AnalysisProgressSpacing),
                ) {
                    Text(
                        text = stringResource(R.string.home_analysis_progress_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = RecapGray900,
                    )
                    LinearProgressIndicator(
                        progress = { analysisProgress.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            FavoriteItemsSection(
                items = uiState.favoriteItems,
                onMoreClick = { onAction(HomeAction.OpenFavoriteCategories) },
                onItemClick = { onAction(HomeAction.SelectFavoriteItem(it)) },
                modifier = Modifier.padding(horizontal = HomeScreenTokens.HorizontalPadding),
            )
            RecentOrganizedScreenshotsSection(
                screenshots = uiState.recentScreenshots,
                onMoreClick = { onAction(HomeAction.OpenRecentScreenshots) },
                onScreenshotClick = { onAction(HomeAction.SelectRecentScreenshot(it)) },
            )
            FrequentSaveTypesSection(
                saveTypes = uiState.frequentSaveTypes,
                onSaveTypeClick = { onAction(HomeAction.SelectFrequentSaveType(it)) },
                modifier = Modifier.padding(horizontal = HomeScreenTokens.HorizontalPadding),
            )
        }
    }
}

@Composable
private fun HomeEmptyOrganizePrompt(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = HomeScreenTokens.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_character_1),
            contentDescription = stringResource(R.string.home_empty_character_content_description),
            modifier = Modifier
                .size(
                    width = HomeScreenTokens.EmptyCharacterWidth,
                    height = HomeScreenTokens.EmptyCharacterHeight,
                )
                .offset(x = 21.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(HomeScreenTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = RecapGray700,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(HomeScreenTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.home_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(HomeScreenTokens.EmptyDescriptionSpacing))
        RecapButton(
            // TODO: RecapButton 내부 padding 수정
            text = stringResource(R.string.home_empty_import_button),
            onClick = onImportClick,
            size = RecapButtonSize.Medium,
            modifier = Modifier.widthIn(min = HomeScreenTokens.EmptyImportButtonMinWidth),
            textStyle = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun RecentOrganizedScreenshotsSection(
    screenshots: List<HomeRecentScreenshotUiModel>,
    onMoreClick: () -> Unit,
    onScreenshotClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.SectionContentSpacing),
    ) {
        HomeSectionHeader(
            title = stringResource(R.string.home_recent_organized_screenshots_title),
            onMoreClick = onMoreClick,
            modifier = Modifier.padding(horizontal = HomeScreenTokens.HorizontalPadding),
        )
        if (screenshots.isEmpty()) {
            HomeSectionEmptyText(
                text = stringResource(R.string.home_recent_organized_screenshots_empty),
                modifier = Modifier.padding(horizontal = HomeScreenTokens.HorizontalPadding),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = HomeScreenTokens.HorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(HomeScreenTokens.RecentCardSpacing),
            ) {
                items(
                    items = screenshots,
                    key = { it.id },
                ) { screenshot ->
                    RecentOrganizedScreenshotCard(
                        thumbnailModel = screenshot.thumbnailModel,
                        title = screenshot.title,
                        categoryType = screenshot.categoryType,
                        onClick = { onScreenshotClick(screenshot.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemsSection(
    items: List<HomeFavoriteItemUiModel>,
    onMoreClick: () -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeSection(
        title = stringResource(R.string.home_favorites_title),
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        if (items.isEmpty()) {
            HomeFavoritesEmptyText()
            return@HomeSection
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.FavoriteCardSpacing),
        ) {
            items.chunked(HomeScreenTokens.FavoriteGridColumns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        HomeScreenTokens.FavoriteCardSpacing,
                    ),
                ) {
                    rowItems.forEach { item ->
                        HomeFavoriteCard(
                            categoryType = item.categoryType,
                            title = item.title,
                            onClick = { onItemClick(item.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(HomeScreenTokens.FavoriteGridColumns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeFavoritesEmptyText(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.FavoritesEmptyTextSpacing),
    ) {
        Text(
            text = stringResource(R.string.home_favorites_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.home_favorites_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeFavoritesEmptyPrompt(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_character_2),
            contentDescription = stringResource(
                R.string.home_favorites_empty_character_content_description,
            ),
            modifier = Modifier.size(
                width = HomeScreenTokens.FavoritesEmptyCharacterWidth,
                height = HomeScreenTokens.FavoritesEmptyCharacterHeight,
            ),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(HomeScreenTokens.FavoritesEmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.home_favorites_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FrequentSaveTypesSection(
    saveTypes: List<HomeFrequentSaveTypeUiModel>,
    onSaveTypeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeSection(
        title = stringResource(R.string.home_frequent_save_types_title),
        onMoreClick = null,
        modifier = modifier,
    ) {
        if (saveTypes.isEmpty()) {
            HomeSectionEmptyText(
                text = stringResource(R.string.home_frequent_save_types_empty),
            )
            return@HomeSection
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HomeScreenTokens.FrequentTypeCardSpacing),
        ) {
            saveTypes.forEach { saveType ->
                val interactionSource = remember(saveType.id) { MutableInteractionSource() }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        HomeScreenTokens.FrequentTypeLabelSpacing,
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = { onSaveTypeClick(saveType.id) },
                    ),
                ) {
                    RecapCategoryIcon(
                        category = saveType.categoryType,
                        size = RecapCategoryIconSize.Large,
                    )
                    Text(
                        text = stringResource(saveType.categoryType.labelResId),
                        style = RecapCaption1,
                        color = RecapGray700,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    onMoreClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.SectionContentSpacing),
    ) {
        HomeSectionHeader(
            title = title,
            onMoreClick = onMoreClick,
        )
        content()
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    onMoreClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = RecapHeading2,
            color = RecapGray900,
        )
        if (onMoreClick != null) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_24),
                contentDescription = stringResource(R.string.home_section_more_content_description),
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onMoreClick,
                    )
                    .size(20.dp),
                tint = RecapGray300,
            )
        }
    }
}

@Composable
private fun HomeSectionEmptyText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = RecapGray500,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

private object HomeScreenTokens {
    val HorizontalPadding = 16.dp
    val VerticalPadding = 20.dp
    val SectionSpacing = 32.dp
    val SectionContentSpacing = 12.dp
    val RecentCardSpacing = 10.dp
    val FavoriteCardSpacing = 11.dp
    const val FavoriteGridColumns = 2
    val FavoritesEmptyTextSpacing = 4.dp
    val FrequentTypeCardSpacing = 16.dp
    val FrequentTypeLabelSpacing = 8.dp
    val AnalysisProgressSpacing = 8.dp
    val EmptyCharacterWidth = 175.dp
    val EmptyCharacterHeight = 127.dp
    val EmptyCharacterSpacing = 24.dp
    val EmptyTitleSpacing = 15.dp
    val EmptyDescriptionSpacing = 33.dp
    val EmptyImportButtonMinWidth = 200.dp
    val FavoritesEmptyCharacterWidth = 133.dp
    val FavoritesEmptyCharacterHeight = 128.dp
    val FavoritesEmptyCharacterSpacing = 27.dp
}

@Preview(name = "Home Screen", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(
            hazeState = rememberHazeState(),
            uiState = HomePreviewUiState,
        )
    }
}

@Preview(
    name = "Home Screen - Analysis Progress",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun HomeScreenAnalysisProgressPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(
            hazeState = rememberHazeState(),
            uiState = HomePreviewUiState,
            analysisProgress = HomeAnalysisProgressUiModel(
                isRunning = true,
                progress = 0.4f,
            ),
        )
    }
}

@Preview(name = "Home Screen - Empty", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(hazeState = rememberHazeState())
    }
}

@Preview(
    name = "Home Screen - Favorites Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun HomeScreenFavoritesEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(
            hazeState = rememberHazeState(),
            uiState = HomePreviewUiState.copy(favoriteItems = emptyList()),
        )
    }
}

@Preview(
    name = "Home Empty Organize Prompt",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
)
@Composable
private fun HomeEmptyOrganizePromptPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeEmptyOrganizePrompt(onImportClick = {})
    }
}

@Preview(
    name = "Home Favorites Empty Prompt",
    showBackground = true,
    widthDp = 360,
    heightDp = 320,
)
@Composable
private fun HomeFavoritesEmptyPromptPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeFavoritesEmptyPrompt(modifier = Modifier.padding(20.dp))
    }
}
