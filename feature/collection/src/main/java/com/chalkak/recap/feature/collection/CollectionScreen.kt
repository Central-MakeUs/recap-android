package com.chalkak.recap.feature.collection

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDefaults
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
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
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
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

                uiState.isLoadError -> {
                    CollectionLoadErrorContent(
                        onRetryClick = { onAction(CollectionAction.RetryLoad) },
                    )
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
private fun CollectionLoadErrorContent(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = CollectionScreenTokens.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_error_circle_60),
            contentDescription = stringResource(
                R.string.collection_load_error_character_content_description,
            ),
            modifier = Modifier.size(CollectionScreenTokens.ErrorIconSize),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(CollectionScreenTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.collection_load_error_title),
            style = RecapHeading3,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(CollectionScreenTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.collection_load_error_description),
            style = RecapBody2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(CollectionScreenTokens.EmptyDescriptionSpacing))
        RecapButton(
            text = stringResource(R.string.collection_load_error_retry),
            onClick = onRetryClick,
            colors = RecapButtonDefaults.secondaryColors(),
            modifier = Modifier.widthIn(min = CollectionScreenTokens.ErrorRetryButtonMinWidth),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.recap_arrow_retry_24),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )
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
            .padding(horizontal = CollectionScreenTokens.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_character_1),
            contentDescription = stringResource(
                R.string.collection_empty_character_content_description,
            ),
            modifier = Modifier
                .size(
                    width = CollectionScreenTokens.EmptyCharacterWidth,
                    height = CollectionScreenTokens.EmptyCharacterHeight,
                )
                .offset(x = CollectionScreenTokens.EmptyCharacterOffsetX),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(CollectionScreenTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.collection_empty_title),
            style = RecapHeading3,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(CollectionScreenTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.collection_empty_description),
            style = RecapBody2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(CollectionScreenTokens.EmptyDescriptionSpacing))
        RecapButton(
            text = stringResource(R.string.collection_empty_organize_button),
            onClick = onNavigateToOrganize,
            size = RecapButtonSize.Large,
            colors = RecapButtonDefaults.secondaryColors(),
            contentPadding = PaddingValues(horizontal = 28.5.dp, vertical = 12.5.dp)
        )
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
    val bottomContentPadding = RecapBottomBarDefaults.Height +
            RecapBottomBarDefaults.BottomPadding +
            navigationBarBottomPadding

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        RecapSearchBar(
            value = "",
            onValueChange = {},
            onClick = { onAction(CollectionAction.OpenSearch) },
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
    Column(modifier = modifier.fillMaxSize()) {
        CollectionFavoritesEntryCard(
            count = favoriteCount,
            onClick = { onAction(CollectionAction.OpenFavoriteDetail) },
            modifier = Modifier
                .padding(horizontal = CollectionScreenTokens.HorizontalPadding)
                .padding(bottom = CollectionScreenTokens.FavoriteCardBottomPadding),
        )
        AnimatedContent(
            targetState = viewMode,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    collectionViewModeForwardTransition()
                } else {
                    collectionViewModeBackwardTransition()
                }
            },
            label = "collectionTypeViewMode",
        ) { animatedViewMode ->
            when (animatedViewMode) {
                CollectionTypeViewMode.Grid -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val gridColumns = collectionTypeGridColumns(maxWidth)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = CollectionScreenTokens.HorizontalPadding),
                            contentPadding = PaddingValues(
                                top = CollectionScreenTokens.TypeGridTopPadding,
                                bottom = bottomContentPadding,
                            ),
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
                }

                CollectionTypeViewMode.List -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = CollectionScreenTokens.TypeListTopPadding,
                            bottom = bottomContentPadding,
                        ),
                    ) {
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
    }
}

private fun collectionViewModeForwardTransition() =
    slideInHorizontally(
        animationSpec = tween(CollectionViewModeSlideDurationMillis),
        initialOffsetX = { fullWidth -> fullWidth / CollectionViewModeSlideFraction },
    ) + fadeIn(
        animationSpec = tween(CollectionViewModeFadeDurationMillis),
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(CollectionViewModeSlideDurationMillis),
        targetOffsetX = { fullWidth -> -fullWidth / CollectionViewModeSlideFraction },
    ) + fadeOut(
        animationSpec = tween(CollectionViewModeFadeDurationMillis),
    )

private fun collectionViewModeBackwardTransition() =
    slideInHorizontally(
        animationSpec = tween(CollectionViewModeSlideDurationMillis),
        initialOffsetX = { fullWidth -> -fullWidth / CollectionViewModeSlideFraction },
    ) + fadeIn(
        animationSpec = tween(CollectionViewModeFadeDurationMillis),
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(CollectionViewModeSlideDurationMillis),
        targetOffsetX = { fullWidth -> fullWidth / CollectionViewModeSlideFraction },
    ) + fadeOut(
        animationSpec = tween(CollectionViewModeFadeDurationMillis),
    )

@Composable
private fun CollectionTypeGridItem(
    summary: CollectionTypeSummaryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryLabel = stringResource(summary.labelResId)
    val recapCountLabel = pluralStringResource(
        R.plurals.collection_recap_count,
        summary.count,
        summary.count,
    )
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
            text = categoryLabel,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
            style = RecapHeading3,
            color = RecapGray900,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
        Text(
            text = recapCountLabel,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(),
            style = RecapCaption2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
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

private const val CollectionViewModeSlideDurationMillis = 300
private const val CollectionViewModeFadeDurationMillis = 250
private const val CollectionViewModeSlideFraction = 6

/**
 * Type grid column count from available width (before horizontal padding).
 *
 * - below 348.dp: 2 columns
 * - below 493.dp: 3 columns
 * - otherwise: 4 columns (cap)
 *
 * 360.dp phones stay on 3 columns with the 348.dp three-column floor.
 * Four-column floor matches fixed haze-folder card width 99.dp plus
 * horizontal padding/spacing: 40 + 99*4 + 19*3 = 493.dp.
 */
internal fun collectionTypeGridColumns(availableWidth: Dp): Int = when {
    availableWidth < CollectionScreenTokens.TypeGridThreeColumnMinWidth -> 2
    availableWidth < CollectionScreenTokens.TypeGridFourColumnMinWidth -> 3
    else -> 4
}

private object CollectionScreenTokens {
    val HorizontalPadding = 20.dp
    val SearchTopPadding = 8.dp
    val ContentTopPadding = 20.dp
    val FavoriteCardBottomPadding = 10.dp
    val TypeGridTopPadding = 16.dp
    val TypeListTopPadding = 10.dp
    val TypeGridSpacing = 19.dp
    val TypeGridRowSpacing = 24.dp
    /** Inclusive lower bound for 3 columns (keeps 360.dp phones on 3×3). */
    val TypeGridThreeColumnMinWidth = 332.dp
    /** Inclusive lower bound for 4 columns (fold / wide). */
    val TypeGridFourColumnMinWidth = 493.dp
    val MinimumTouchTarget = 48.dp
    val EmptyCharacterWidth = 122.dp
    val EmptyCharacterHeight = 89.dp
    val EmptyCharacterOffsetX = 6.dp
    val EmptyCharacterSpacing = 20.dp
    val EmptyTitleSpacing = 13.dp
    val EmptyDescriptionSpacing = 23.dp
    val ErrorIconSize = 60.dp
    val ErrorRetryButtonMinWidth = 188.dp
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

@Preview(name = "Collection Load Error", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionLoadErrorPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionScreen(
            uiState = CollectionUiState(
                isLoading = false,
                isLoadError = true,
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

internal fun previewOverviewUiState(
    viewMode: CollectionTypeViewMode,
    favoriteCount: Int = 4,
): CollectionUiState {
    return CollectionUiState(
        isLoading = false,
        hasStoredScreenshots = true,
        typeViewMode = viewMode,
        overview = CollectionOverviewUiModel(
            favoriteSummary = CollectionFavoriteSummaryUiModel(count = favoriteCount),
            typeSummaries = CollectionOverviewPreviewTypeSummaries,
        ),
    )
}

/**
 * Overview preview/fixture order mirrors production overview category order
 * (SHOPPING → … → ETC), covering all 9 taxonomy entries.
 */
private val CollectionOverviewPreviewTypeSummaries = listOf(
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.SHOPPING,
        labelResId = R.string.category_type_shopping_product,
        categoryType = RecapCategoryType.ShoppingProduct,
        count = 20,
        exampleTitles = listOf("택배 반품 절차", "노트북 가격 비교"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.PLACE,
        labelResId = R.string.category_type_place_restaurant,
        categoryType = RecapCategoryType.PlaceRestaurant,
        count = 23,
        exampleTitles = listOf("성수 카페", "강남 맛집"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.SCHEDULE,
        labelResId = R.string.category_type_schedule_reservation,
        categoryType = RecapCategoryType.ScheduleReservation,
        count = 10,
        exampleTitles = listOf("치과 예약", "항공권 일정"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.KNOWLEDGE,
        labelResId = R.string.category_type_info_knowledge,
        categoryType = RecapCategoryType.InfoKnowledge,
        count = 12,
        exampleTitles = listOf("Compose 팁", "면접 질문"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.CONTENT,
        labelResId = R.string.category_type_book_content,
        categoryType = RecapCategoryType.BookContent,
        count = 1,
        exampleTitles = listOf("읽을 책 메모"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.BENEFIT,
        labelResId = R.string.category_type_benefit_event,
        categoryType = RecapCategoryType.BenefitEvent,
        count = 5,
        exampleTitles = listOf("쿠폰 만료", "이벤트 안내"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.RECORD,
        labelResId = R.string.category_type_record_capture,
        categoryType = RecapCategoryType.RecordCapture,
        count = 12,
        exampleTitles = listOf("회의 메모", "아이디어 스케치"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.JOB,
        labelResId = R.string.category_type_job_career,
        categoryType = RecapCategoryType.JobCareer,
        count = 8,
        exampleTitles = listOf("이력서 초안", "채용 공고"),
        additionalExampleCount = 0,
    ),
    CollectionTypeSummaryUiModel(
        contentType = ScreenshotContentType.ETC,
        labelResId = R.string.category_type_other,
        categoryType = RecapCategoryType.Other,
        count = 3,
        exampleTitles = listOf("미분류 메모", "임시 저장"),
        additionalExampleCount = 0,
    ),
)
