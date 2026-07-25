package com.chalkak.recap.feature.home.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    uiState: SearchUiState = SearchUiState(),
    onAction: (SearchAction) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchTopBar(
                query = uiState.query,
                onQueryChange = { onAction(SearchAction.UpdateQuery(it)) },
                onSearch = { onAction(SearchAction.SubmitSearch) },
                onBackClick = { onAction(SearchAction.NavigateBack) },
            )
            when (uiState.phase) {
                SearchContentPhase.Idle -> SearchIdleContent(
                    recentSearches = uiState.recentSearches,
                    onAction = onAction,
                )

                SearchContentPhase.Loading -> SearchLoadingContent()

                SearchContentPhase.Results -> SearchResultsContent(
                    results = uiState.results,
                    hasNext = uiState.hasNext,
                    isLoadingMore = uiState.isLoadingMore,
                    onAction = onAction,
                )

                SearchContentPhase.Empty -> SearchEmptyContent()

                SearchContentPhase.Error -> SearchErrorContent(
                    onRetry = { onAction(SearchAction.RetrySearch) },
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SearchScreenTokens.TopBarHeight)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SearchBackButton(onClick = onBackClick)
            RecapSearchBar(
                value = query,
                onValueChange = onQueryChange,
                onSearch = onSearch,
                modifier = Modifier.weight(1f),
                placeholder = stringResource(R.string.search_screen_placeholder),
            )
        }
    }
}

@Composable
private fun SearchBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(36.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.search_screen_back_content_description),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun SearchIdleContent(
    recentSearches: List<String>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SearchScreenTokens.HorizontalPadding)
            .padding(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        SearchableInfoSection()
        RecentSearchesSection(
            recentSearches = recentSearches,
            onClearAllClick = { onAction(SearchAction.ClearAllRecentSearches) },
            onRecentSearchClick = { onAction(SearchAction.SelectRecentSearch(it)) },
        )
    }
}

@Composable
private fun SearchLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = RecapBlue500)
    }
}

@Composable
private fun SearchResultsContent(
    results: List<SearchResultItemUiModel>,
    hasNext: Boolean,
    isLoadingMore: Boolean,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, results.size, hasNext, isLoadingMore) {
        snapshotFlow {
            val secondLastIndex = results.lastIndex - 1
            secondLastIndex >= 0 &&
                listState.layoutInfo.visibleItemsInfo.any { item -> item.index == secondLastIndex }
        }
            .distinctUntilChanged()
            .filter { isSecondLastVisible -> isSecondLastVisible }
            .collect {
                if (hasNext && !isLoadingMore) {
                    onAction(SearchAction.LoadMore)
                }
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 32.dp,
        ),
    ) {
        itemsIndexed(
            items = results,
            key = { _, item -> item.captureId },
        ) { index, item ->
            ScreenshotCard(
                thumbnailModel = item.thumbnailModel,
                categoryType = item.categoryType,
                title = item.title,
                description = item.description,
                titleHighlightRange = item.titleHighlightRange,
                descriptionHighlightRange = item.descriptionHighlightRange,
                isFavorite = item.isFavorite,
                onClick = { onAction(SearchAction.SelectResult(item.captureId)) },
                onFavoriteClick = { onAction(SearchAction.ToggleFavorite(item.captureId)) },
                horizontalContentPadding = 0.dp,
                showBottomDivider = index < results.lastIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SearchScreenTokens.HorizontalPadding),
            )
        }
        if (isLoadingMore) {
            item(key = "search_loading_more") {
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
}

@Composable
private fun SearchEmptyContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SearchScreenTokens.HorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.search_screen_empty_results),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SearchErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SearchScreenTokens.HorizontalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.search_screen_error),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        RecapButton(
            text = stringResource(R.string.search_screen_retry),
            onClick = onRetry,
            size = RecapButtonSize.Medium,
            colors = RecapButtonDefaults.primaryColors(),
        )
    }
}

@Composable
private fun SearchableInfoSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.search_screen_searchable_info_title),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray300,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchableInfoItem(text = stringResource(R.string.search_screen_searchable_capture_title))
            SearchableInfoItem(text = stringResource(R.string.search_screen_searchable_summary))
            SearchableInfoItem(text = stringResource(R.string.search_screen_searchable_key_info))
            SearchableInfoItem(text = stringResource(R.string.search_screen_searchable_image_content))
        }
    }
}

@Composable
private fun SearchableInfoItem(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(RecapGray300),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = RecapGray300,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesSection(
    recentSearches: List<String>,
    onClearAllClick: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.search_screen_recent_searches_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
            if (recentSearches.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.search_screen_clear_all_recent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecapBlue500,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onClearAllClick,
                    ),
                )
            }
        }
        if (recentSearches.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                recentSearches.forEach { term ->
                    RecentSearchChip(
                        text = term,
                        onClick = { onRecentSearchClick(term) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSearchChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, RecapGray200),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

private object SearchScreenTokens {
    val HorizontalPadding = 16.dp
    val TopBarHeight = 56.dp
}

@Preview(name = "Search Screen Idle", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SearchScreenIdlePreview() {
    RECAPTheme {
        SearchScreen(
            uiState = SearchUiState(
                recentSearches = listOf("숙소 예약", "반품 절차", "파스타"),
            ),
        )
    }
}

@Preview(name = "Search Screen Results", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SearchScreenResultsPreview() {
    RECAPTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "파스타",
                phase = SearchContentPhase.Results,
                resultCount = 1L,
                results = listOf(
                    SearchResultItemUiModel(
                        captureId = 1L,
                        thumbnailModel = R.drawable.mock_home_screenshot_recipe,
                        categoryType = RecapCategoryType.InfoKnowledge,
                        title = "파스타 레시피 저장",
                        description = "한 줄 요약에 파스타가 포함됩니다",
                        titleHighlightRange = 0..2,
                        descriptionHighlightRange = 7..9,
                        organizedAtMillis = 0L,
                        isFavorite = false,
                    ),
                ),
            ),
        )
    }
}

@Preview(name = "Search Screen Empty", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SearchScreenEmptyPreview() {
    RECAPTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "없는검색어",
                phase = SearchContentPhase.Empty,
            ),
        )
    }
}
