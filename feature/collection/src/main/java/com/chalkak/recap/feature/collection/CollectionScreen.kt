package com.chalkak.recap.feature.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun CollectionScreen(
    hazeState: HazeState,
    uiState: CollectionUiState,
    onAction: (CollectionAction) -> Unit,
    onNavigateToOrganize: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(state = hazeState),
        color = MaterialTheme.colorScheme.background,
    ) {
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
                    overview = uiState.overview,
                    onAction = onAction,
                )
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
        Text(
            text = stringResource(R.string.collection_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = RecapGray900,
        )
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
                    border = BorderStroke(1.dp, com.chalkak.recap.core.design.theme.RecapGray100),
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
    overview: CollectionOverviewUiModel,
    onAction: (CollectionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val bottomContentPadding = RecapBottomBarDefaults.ContentScrollPadding +
        navigationBarBottomPadding

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(
                top = 24.dp,
                bottom = 24.dp + bottomContentPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.collection_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
            )
            Text(
                text = stringResource(R.string.collection_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray500,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.collection_favorites_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
            )
            val favoriteSummary = overview.favoriteSummary
            if (favoriteSummary != null) {
                CollectionOverviewCard(
                    thumbnailModels = favoriteSummary.previewThumbnailModels,
                    title = stringResource(R.string.collection_favorites_section_title),
                    subtitle = stringResource(
                        R.string.collection_favorites_capture_count,
                        favoriteSummary.count,
                    ),
                    exampleTitles = emptyList(),
                    additionalExampleCount = 0,
                    onClick = { onAction(CollectionAction.OpenFavoriteDetail) },
                    showFavoriteBadge = true,
                )
            } else {
                CollectionThinMessageCard(
                    message = stringResource(R.string.collection_favorites_empty),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.collection_types_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
            )
            overview.typeSummaries.forEach { typeSummary ->
                CollectionOverviewCard(
                    thumbnailModels = typeSummary.previewThumbnailModels,
                    title = stringResource(typeSummary.labelResId),
                    subtitle = stringResource(R.string.collection_type_count, typeSummary.count),
                    exampleTitles = typeSummary.exampleTitles,
                    additionalExampleCount = typeSummary.additionalExampleCount,
                    onClick = {
                        onAction(CollectionAction.OpenTypeDetail(typeSummary.contentType))
                    },
                )
            }
        }
    }
}

@Preview(name = "Collection Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            hazeState = rememberHazeState(),
            uiState = CollectionUiState(
                isLoading = false,
                hasStoredScreenshots = false,
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection No Favorite", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionNoFavoritePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            hazeState = rememberHazeState(),
            uiState = CollectionUiState(
                isLoading = false,
                hasStoredScreenshots = true,
                overview = CollectionOverviewUiModel(
                    favoriteSummary = null,
                    typeSummaries = listOf(
                        CollectionTypeSummaryUiModel(
                            contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                            labelResId = R.string.collection_content_type_shopping_product,
                            count = 12,
                            exampleTitles = listOf("택배 반품 절차", "노트북 가격 비교"),
                            additionalExampleCount = 0,
                            previewThumbnailModels = emptyList(),
                        ),
                    ),
                ),
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}

@Preview(name = "Collection Populated", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            hazeState = rememberHazeState(),
            uiState = CollectionUiState(
                isLoading = false,
                hasStoredScreenshots = true,
                overview = CollectionOverviewUiModel(
                    favoriteSummary = CollectionFavoriteSummaryUiModel(
                        count = 4,
                        previewThumbnailModels = emptyList(),
                    ),
                    typeSummaries = listOf(
                        CollectionTypeSummaryUiModel(
                            contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                            labelResId = R.string.collection_content_type_shopping_product,
                            count = 12,
                            exampleTitles = listOf("택배 반품 절차", "노트북 가격 비교"),
                            additionalExampleCount = 0,
                            previewThumbnailModels = emptyList(),
                        ),
                        CollectionTypeSummaryUiModel(
                            contentType = ScreenshotContentType.OTHER,
                            labelResId = R.string.collection_content_type_other,
                            count = 3,
                            exampleTitles = listOf("이사 체크리스트"),
                            additionalExampleCount = 2,
                            previewThumbnailModels = emptyList(),
                        ),
                    ),
                ),
            ),
            onAction = {},
            onNavigateToOrganize = {},
        )
    }
}
