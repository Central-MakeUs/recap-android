package com.chalkak.recap.feature.home.recent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.animation.recapScreenshotCardItemAnimation
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.OrganizedRelativeTimeFormatter
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.swipe.SwipeActionRow
import com.chalkak.recap.core.design.component.swipe.rememberEditDeleteSwipeActions
import com.chalkak.recap.core.design.component.topbar.RecentOrganizedScreenshotsTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun RecentOrganizedScreenshotsScreen(
    uiState: RecentOrganizedScreenshotsUiState,
    onAction: (RecentOrganizedScreenshotsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationBarBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

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
            when (uiState.phase) {
                RecentOrganizedScreenshotsPhase.Loading -> {
                    RecentOrganizedScreenshotsLoadingContent()
                }

                RecentOrganizedScreenshotsPhase.Empty -> {
                    RecentOrganizedScreenshotsEmptyContent(
                        onImportClick = {
                            onAction(RecentOrganizedScreenshotsAction.StartImport)
                        },
                    )
                }

                RecentOrganizedScreenshotsPhase.Error -> {
                    RecentOrganizedScreenshotsErrorContent(
                        onRetry = { onAction(RecentOrganizedScreenshotsAction.Retry) },
                    )
                }

                RecentOrganizedScreenshotsPhase.Content -> {
                    RecentOrganizedScreenshotsContent(
                        uiState = uiState,
                        navigationBarBottomPadding = navigationBarBottomPadding,
                        onAction = onAction,
                    )
                }
            }
        }
    }

    if (uiState.pendingDeleteCaptureId != null) {
        RecapPopup(
            title = stringResource(R.string.screenshot_delete_confirm_title),
            description = stringResource(R.string.screenshot_delete_confirm_description),
            confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onConfirmClick = { onAction(RecentOrganizedScreenshotsAction.ConfirmDeleteItem) },
            onCancelClick = { onAction(RecentOrganizedScreenshotsAction.DismissDeleteItem) },
            onDismissRequest = { onAction(RecentOrganizedScreenshotsAction.DismissDeleteItem) },
            confirmButtonColor = RecapError,
        )
    }
}

@Composable
private fun RecentOrganizedScreenshotsContent(
    uiState: RecentOrganizedScreenshotsUiState,
    navigationBarBottomPadding: Dp,
    onAction: (RecentOrganizedScreenshotsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nowMillis = remember { System.currentTimeMillis() }
    val visibleItems = remember(uiState.items, nowMillis) {
        uiState.items.filter { item ->
            OrganizedRelativeTimeFormatter.isVisible(
                organizedAtMillis = item.organizedAtMillis,
                nowMillis = nowMillis,
            )
        }
    }
    val listState = rememberLazyListState()
    val displayCount = uiState.resultCount.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    val countHeaderItemCount = if (displayCount > 0) 1 else 0
    var lastRequestedPage by remember { mutableIntStateOf(-1) }
    var revealedCaptureId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(
        listState,
        visibleItems.size,
        countHeaderItemCount,
        uiState.hasNext,
        uiState.isLoadingMore,
        uiState.nextPage,
    ) {
        snapshotFlow {
            val secondLastIndex = countHeaderItemCount + visibleItems.lastIndex - 1
            secondLastIndex >= countHeaderItemCount &&
                listState.layoutInfo.visibleItemsInfo.any { item -> item.index == secondLastIndex }
        }
            .distinctUntilChanged()
            .filter { isSecondLastVisible -> isSecondLastVisible }
            .collect {
                if (
                    uiState.hasNext &&
                    !uiState.isLoadingMore &&
                    lastRequestedPage != uiState.nextPage
                ) {
                    lastRequestedPage = uiState.nextPage
                    onAction(RecentOrganizedScreenshotsAction.LoadMore)
                }
            }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            bottom = RecentOrganizedScreenshotsTokens.ListVerticalPadding +
                    navigationBarBottomPadding,
        ),
    ) {
        if (displayCount > 0) {
            item(key = "recent_organized_count") {
                RecentOrganizedScreenshotsCountText(displayCount = displayCount)
            }
        }
        items(
            items = visibleItems,
            key = { item -> item.id },
            contentType = { "recent_organized_screenshot" },
        ) { item ->
            SwipeActionRow(
                actions = rememberEditDeleteSwipeActions(
                    onEditClick = {
                        revealedCaptureId = null
                        onAction(RecentOrganizedScreenshotsAction.EditItem(item.id))
                    },
                    onDeleteClick = {
                        onAction(RecentOrganizedScreenshotsAction.RequestDeleteItem(item.id))
                    },
                ),
                revealed = revealedCaptureId == item.id,
                onRevealedChange = { revealed ->
                    revealedCaptureId = when {
                        revealed -> item.id
                        revealedCaptureId == item.id -> null
                        else -> revealedCaptureId
                    }
                },
                onDragStarted = {
                    if (revealedCaptureId != null && revealedCaptureId != item.id) {
                        revealedCaptureId = null
                    }
                },
                modifier = Modifier
                    .recapScreenshotCardItemAnimation()
                    .fillMaxWidth(),
            ) {
                ScreenshotCard(
                    thumbnailModel = item.thumbnailModel,
                    categoryType = item.categoryType,
                    title = item.title,
                    description = item.description,
                    isFavorite = item.isFavorite,
                    onClick = {
                        onAction(RecentOrganizedScreenshotsAction.SelectItem(item.id))
                    },
                    onFavoriteClick = {
                        onAction(RecentOrganizedScreenshotsAction.ToggleFavorite(item.id))
                    },
                    horizontalContentPadding = RecentOrganizedScreenshotsTokens.HorizontalPadding,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (visibleItems.isEmpty()) {
            item(key = "recent_organized_empty") {
                RecentOrganizedScreenshotsEmptyContent(
                    onImportClick = {
                        onAction(RecentOrganizedScreenshotsAction.StartImport)
                    },
                    modifier = Modifier.fillParentMaxSize(),
                )
            }
        }
        if (uiState.isLoadingMore) {
            item(key = "recent_loading_more") {
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
private fun RecentOrganizedScreenshotsCountText(
    displayCount: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(RecapHeading4.toSpanStyle().copy(color = RecapGray700)) {
                append(displayCount.toString())
            }
            append(" ")
            withStyle(RecapBody2.toSpanStyle().copy(color = RecapGray500)) {
                append(
                    pluralStringResource(
                        R.plurals.recap_haze_folder_card_recap_label,
                        displayCount,
                    ),
                )
            }
        },
        modifier = modifier.padding(
            horizontal = RecentOrganizedScreenshotsTokens.HorizontalPadding,
            vertical = RecentOrganizedScreenshotsTokens.CountVerticalPadding,
        ),
        style = RecapBody2,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RecentOrganizedScreenshotsLoadingContent(
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
            style = RecapHeading3,
            fontWeight = FontWeight.Bold,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.home_empty_description),
            style = RecapBody2,
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
            textStyle = RecapHeading3,
        )
    }
}

@Composable
private fun RecentOrganizedScreenshotsErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = RecentOrganizedScreenshotsTokens.HorizontalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_error_circle_60),
            contentDescription = stringResource(
                R.string.home_recent_organized_screenshots_error_character_content_description,
            ),
            modifier = Modifier.size(RecentOrganizedScreenshotsTokens.ErrorIconSize),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.EmptyCharacterSpacing))
        Text(
            text = stringResource(R.string.home_recent_organized_screenshots_error),
            style = RecapHeading3,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.EmptyTitleSpacing))
        Text(
            text = stringResource(R.string.home_recent_organized_screenshots_error_description),
            style = RecapBody2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(RecentOrganizedScreenshotsTokens.ErrorDescriptionSpacing))
        RecapButton(
            text = stringResource(R.string.home_recent_organized_screenshots_retry),
            onClick = onRetry,
            size = RecapButtonSize.Large,
            colors = RecapButtonDefaults.secondaryColors(),
            contentPadding = PaddingValues(
                horizontal = RecentOrganizedScreenshotsTokens.ErrorRetryHorizontalPadding,
                vertical = RecentOrganizedScreenshotsTokens.ErrorRetryVerticalPadding,
            ),
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
    val ErrorIconSize = 60.dp
    val ErrorDescriptionSpacing = 23.dp
    val ErrorRetryHorizontalPadding = 52.dp
    val ErrorRetryVerticalPadding = 12.5.dp
}

@Preview(
    name = "Recent Organized Screenshots Empty",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RecentOrganizedScreenshotsScreenEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(
                phase = RecentOrganizedScreenshotsPhase.Empty,
            ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Recent Organized Screenshots Loading",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RecentOrganizedScreenshotsScreenLoadingPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(
                phase = RecentOrganizedScreenshotsPhase.Loading,
            ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Recent Organized Screenshots Error",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RecentOrganizedScreenshotsScreenErrorPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsScreen(
            uiState = RecentOrganizedScreenshotsUiState(
                phase = RecentOrganizedScreenshotsPhase.Error,
            ),
            onAction = {},
        )
    }
}
