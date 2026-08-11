package com.chalkak.recap.feature.home.search

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
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
                autoFocus = uiState.autoFocus,
                onQueryChange = { onAction(SearchAction.UpdateQuery(it)) },
                onSearch = { onAction(SearchAction.SubmitSearch) },
                onCloseClick = { onAction(SearchAction.NavigateBack) },
            )
            when (uiState.phase) {
                SearchContentPhase.Idle -> SearchIdleContent(
                    recentSearches = uiState.recentSearches,
                    onAction = onAction,
                )

                SearchContentPhase.Loading -> SearchLoadingContent()

                SearchContentPhase.Results -> SearchResultsContent(
                    results = uiState.results,
                    resultCount = uiState.resultCount,
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
    autoFocus: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCloseClick: () -> Unit,
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
                .padding(horizontal = SearchScreenTokens.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RecapSearchBar(
                value = query,
                onValueChange = onQueryChange,
                onSearch = onSearch,
                autoFocus = autoFocus,
                modifier = Modifier.weight(1f),
                placeholder = stringResource(R.string.search_screen_placeholder),
            )
            SearchCloseButton(onClick = onCloseClick)
        }
    }
}

@Composable
private fun SearchCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.search_screen_close),
        style = RecapBody2,
        color = RecapGray500,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick,
        ),
    )
}

@Composable
private fun SearchIdleContent(
    recentSearches: List<String>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    RecentSearchesSection(
        recentSearches = recentSearches,
        onClearAllClick = { onAction(SearchAction.ClearAllRecentSearches) },
        onRecentSearchClick = { onAction(SearchAction.SelectRecentSearch(it)) },
        onRemoveRecentSearchClick = { onAction(SearchAction.RemoveRecentSearch(it)) },
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp),
    )
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
    resultCount: Long,
    hasNext: Boolean,
    isLoadingMore: Boolean,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val displayCount = resultCount.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

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

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = SearchScreenTokens.HorizontalPadding,
                    vertical = SearchScreenTokens.CountVerticalPadding,
                )
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = displayCount.toString(),
                style = RecapHeading4,
                color = RecapGray900,
                maxLines = 1,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.recap_haze_folder_card_recap_label,
                    displayCount,
                ),
                style = RecapBody2,
                color = RecapGray500,
                maxLines = 1,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            items(
                items = results,
                key = { item -> item.captureId },
            ) { item ->
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
                    horizontalContentPadding = SearchScreenTokens.HorizontalPadding,
                    modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun SearchEmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SearchScreenTokens.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_character_1),
            contentDescription = stringResource(
                R.string.search_screen_empty_character_content_description,
            ),
            modifier = Modifier
                .size(
                    width = SearchScreenTokens.EmptyCharacterWidth,
                    height = SearchScreenTokens.EmptyCharacterHeight,
                )
                .offset(x = SearchScreenTokens.EmptyCharacterOffsetX),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(SearchScreenTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.search_screen_empty_results),
            style = RecapHeading3,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SearchScreenTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.search_screen_empty_results_description),
            style = RecapBody2,
            color = RecapGray300,
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
        Image(
            painter = painterResource(R.drawable.ic_error_circle_60),
            contentDescription = stringResource(
                R.string.search_screen_error_character_content_description,
            ),
            modifier = Modifier.size(SearchScreenTokens.ErrorIconSize),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(SearchScreenTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.search_screen_error),
            style = RecapHeading3,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SearchScreenTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.search_screen_error_description),
            style = RecapBody2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SearchScreenTokens.ErrorDescriptionSpacing))
        RecapButton(
            text = stringResource(R.string.search_screen_retry),
            onClick = onRetry,
            size = RecapButtonSize.Large,
            colors = RecapButtonDefaults.secondaryColors(),
            contentPadding = PaddingValues(
                horizontal = SearchScreenTokens.ErrorRetryHorizontalPadding,
                vertical = SearchScreenTokens.ErrorRetryVerticalPadding,
            ),
        )
    }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<String>,
    onClearAllClick: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onRemoveRecentSearchClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SearchScreenTokens.HorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.search_screen_recent_searches_title),
                style = RecapHeading3,
                color = RecapGray700,
            )
            if (recentSearches.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.search_screen_clear_all_recent),
                    style = RecapCaption1,
                    color = RecapGray500,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onClearAllClick,
                    ),
                )
            }
        }
        if (recentSearches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SearchScreenTokens.HorizontalPadding)
                    .padding(vertical = 34.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = stringResource(R.string.search_screen_empty_recent),
                    style = RecapCaption1,
                    color = RecapGray300,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = SearchScreenTokens.HorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = recentSearches,
                    key = { term -> term },
                ) { term ->
                    RecentSearchChip(
                        text = term,
                        onClick = { onRecentSearchClick(term) },
                        onRemoveClick = { onRemoveRecentSearchClick(term) },
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
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
        shape = RoundedCornerShape(percent = 50),
        color = RecapGray50,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 30.dp)
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = text,
                style = RecapBody2,
                color = RecapGray500,
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onRemoveClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cancel_circle_16),
                    contentDescription = stringResource(
                        R.string.search_screen_remove_recent_content_description,
                    ),
                    modifier = Modifier.size(16.dp),
                    tint = RecapGray300,
                )
            }
        }
    }
}

private object SearchScreenTokens {
    val HorizontalPadding = 16.dp
    val TopBarHeight = 56.dp
    val CountVerticalPadding = 8.dp
    val EmptyCharacterWidth = 122.67.dp
    val EmptyCharacterHeight = 89.dp
    val EmptyCharacterOffsetX = 6.dp
    val EmptyCharacterSpacing = 20.dp
    val EmptyTitleSpacing = 13.dp
    val ErrorIconSize = 60.dp
    val ErrorDescriptionSpacing = 23.dp
    val ErrorRetryHorizontalPadding = 52.dp
    val ErrorRetryVerticalPadding = 12.5.dp
}

@Preview(name = "Search Screen Idle Empty", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SearchScreenIdleEmptyPreview() {
    RECAPTheme {
        SearchScreen()
    }
}

@Preview(name = "Search Screen Idle", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SearchScreenIdlePreview() {
    RECAPTheme {
        SearchScreen(
            uiState = SearchUiState(
                recentSearches = listOf("검색어", "검색어 01234", "검색검색검색"),
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

@Preview(name = "Search Screen Error", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SearchScreenErrorPreview() {
    RECAPTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "숙소예약",
                phase = SearchContentPhase.Error,
            ),
        )
    }
}
