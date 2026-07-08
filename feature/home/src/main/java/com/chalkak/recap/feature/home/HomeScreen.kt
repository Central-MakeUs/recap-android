package com.chalkak.recap.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.card.FavoriteCategoryCard
import com.chalkak.recap.core.design.component.card.FrequentSaveTypeFolderCard
import com.chalkak.recap.core.design.component.card.RecentOrganizedScreenshotCard
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
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
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
        navigationBarBottomPadding

    Box(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(state = hazeState)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HomeScreenTokens.HorizontalPadding)
                .padding(
                    top = HomeScreenTokens.VerticalPadding,
                    bottom = HomeScreenTokens.VerticalPadding + bottomContentPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(HomeScreenTokens.SectionSpacing),
        ) {
            if (analysisProgress.isRunning) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
            RecentOrganizedScreenshotsSection(
                screenshots = uiState.recentScreenshots,
                onMoreClick = { onAction(HomeAction.OpenRecentScreenshots) },
                onScreenshotClick = { onAction(HomeAction.SelectRecentScreenshot(it)) },
            )
            FavoriteCategoriesSection(
                categories = uiState.favoriteCategories,
                onMoreClick = { onAction(HomeAction.OpenFavoriteCategories) },
                onCategoryClick = { onAction(HomeAction.SelectFavoriteCategory(it)) },
                onFavoriteClick = { onAction(HomeAction.ToggleFavoriteCategory(it)) },
            )
            FrequentSaveTypesSection(
                saveTypes = uiState.frequentSaveTypes,
                onMoreClick = { onAction(HomeAction.OpenFrequentSaveTypes) },
                onSaveTypeClick = { onAction(HomeAction.SelectFrequentSaveType(it)) },
            )
        }
    }
}

@Composable
private fun RecentOrganizedScreenshotsSection(
    screenshots: List<HomeRecentScreenshotUiModel>,
    onMoreClick: () -> Unit,
    onScreenshotClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeSection(
        title = stringResource(R.string.home_recent_organized_screenshots_title),
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(HomeScreenTokens.RecentCardSpacing),
            modifier = Modifier.clip(shape = RoundedCornerShape(HomeScreenTokens.HomeSectionRadius))
        ) {
            items(
                items = screenshots,
                key = { it.id },
            ) { screenshot ->
                RecentOrganizedScreenshotCard(
                    thumbnailModel = screenshot.thumbnailResId,
                    title = stringResource(screenshot.titleResId),
                    categoryLabel = stringResource(screenshot.categoryLabelResId),
                    onClick = { onScreenshotClick(screenshot.id) },
                )
            }
        }
    }
}

@Composable
private fun FavoriteCategoriesSection(
    categories: List<HomeFavoriteCategoryUiModel>,
    onMoreClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeSection(
        title = stringResource(R.string.home_favorites_title),
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HomeScreenTokens.HomeSectionRadius)),
        ) {
            categories.forEachIndexed { index, category ->
                FavoriteCategoryCard(
                    thumbnailModel = category.thumbnailResId,
                    categoryLabel = stringResource(category.categoryLabelResId),
                    title = stringResource(category.titleResId),
                    description = stringResource(category.descriptionResId),
                    isFavorite = category.isFavorite,
                    onClick = { onCategoryClick(category.id) },
                    onFavoriteClick = { onFavoriteClick(category.id) },
                )
                if (index < categories.lastIndex) {
                    HorizontalDivider(
                        color = RecapGray100,
                        thickness = HomeScreenTokens.FavoriteDividerThickness,
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequentSaveTypesSection(
    saveTypes: List<HomeFrequentSaveTypeUiModel>,
    onMoreClick: () -> Unit,
    onSaveTypeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeSection(
        title = stringResource(R.string.home_frequent_save_types_title),
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(HomeScreenTokens.FrequentTypeCardSpacing),
            modifier = Modifier.clip(shape = RoundedCornerShape(HomeScreenTokens.HomeSectionRadius))
        ) {
            items(
                items = saveTypes,
                key = { it.id },
            ) { saveType ->
                FrequentSaveTypeFolderCard(
                    categoryLabel = stringResource(saveType.categoryLabelResId),
                    recapCount = saveType.recapCount,
                    onClick = { onSaveTypeClick(saveType.id) },
                )
            }
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
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
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RecapGray900,
        )
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
                .padding(2.dp),
            tint = RecapGray300,
        )
    }
}

private object HomeScreenTokens {
    val HomeSectionRadius = 12.dp
    val HorizontalPadding = 20.dp
    val VerticalPadding = 20.dp
    val SectionSpacing = 32.dp
    val SectionContentSpacing = 12.dp
    val RecentCardSpacing = 12.dp
    val FrequentTypeCardSpacing = 16.dp
    val FavoriteDividerThickness = 1.dp
    val AnalysisProgressSpacing = 8.dp
}

@Preview(name = "Home Screen", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(hazeState = rememberHazeState())
    }
}

@Preview(name = "Home Screen - Analysis Progress", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun HomeScreenAnalysisProgressPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeScreen(
            hazeState = rememberHazeState(),
            analysisProgress = HomeAnalysisProgressUiModel(
                isRunning = true,
                progress = 0.4f,
            ),
        )
    }
}
