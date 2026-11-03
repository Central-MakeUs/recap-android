package com.chalkak.recap.feature.organize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
import com.chalkak.recap.core.model.LocalImage

@Composable
fun ScreenshotConfirmationScreen(
    uiState: OrganizeUiState,
    onAction: (OrganizeAction) -> Unit,
    onBackClick: () -> Unit,
    onAddMoreClick: () -> Unit,
    onStartOrganizingClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomBackground = MaterialTheme.colorScheme.background
    val density = LocalDensity.current
    var bottomContentHeightPx by remember { mutableIntStateOf(0) }
    val bottomContentHeight = with(density) { bottomContentHeightPx.toDp() }
    val selectedScreenshots = uiState.availableScreenshots.filter { screenshot ->
        screenshot.uri in uiState.selectedUris
    }.sortedBy { screenshot ->
        uiState.selectedUris.indexOf(screenshot.uri)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            ScreenshotConfirmationTopBar(
                selectionCount = uiState.selectionCount,
                onBackClick = onBackClick,
            )
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(ScreenshotConfirmationTokens.GridColumns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = ScreenshotConfirmationTokens.HorizontalPadding,
                        end = ScreenshotConfirmationTokens.HorizontalPadding,
                        top = ScreenshotConfirmationTokens.GridVerticalPadding,
                        bottom = bottomContentHeight + ScreenshotConfirmationTokens.GridVerticalPadding,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        ScreenshotConfirmationTokens.GridSpacing,
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        ScreenshotConfirmationTokens.GridSpacing,
                    ),
                ) {
                    items(
                        items = selectedScreenshots,
                        key = { it.uri },
                    ) { screenshot ->
                        val selectionOrder = uiState.selectionOrder(screenshot.uri) ?: 0
                        ScreenshotConfirmationGridItem(
                            screenshot = screenshot,
                            selectionOrder = selectionOrder,
                            onRemoveClick = {
                                onAction(OrganizeAction.RemoveSelection(screenshot.uri))
                            },
                        )
                    }
                    if (uiState.selectionCount < MAX_SELECTION_COUNT) {
                        item(span = { GridItemSpan(1) }) {
                            ScreenshotConfirmationAddItem(
                                onClick = onAddMoreClick,
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onSizeChanged { bottomContentHeightPx = it.height }
                        .drawBehind {
                            val fadeHeight = ScreenshotConfirmationTokens.BottomFadeHeight.toPx()
                            drawRect(
                                color = bottomBackground,
                                topLeft = Offset(0f, fadeHeight),
                                size = Size(
                                    size.width,
                                    (size.height - fadeHeight).coerceAtLeast(0f)
                                ),
                            )
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        bottomBackground.copy(alpha = 0f),
                                        bottomBackground
                                    ),
                                    startY = 0f,
                                    endY = fadeHeight,
                                ),
                                size = Size(size.width, fadeHeight),
                            )
                        }
                        .padding(
                            start = ScreenshotConfirmationTokens.HorizontalPadding,
                            end = ScreenshotConfirmationTokens.HorizontalPadding,
                            top = ScreenshotConfirmationTokens.BottomFadeHeight + ScreenshotConfirmationTokens.BottomContentTopPadding,
                            bottom = ScreenshotConfirmationTokens.BottomPadding,
                        ),
                ) {
                    AnimatedVisibility(
                        visible = uiState.monthlyRemaining != null && uiState.monthlyRemaining < MAX_SELECTION_COUNT,
                        enter = fadeIn(tween(200)),
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                val remaining = uiState.monthlyRemaining ?: 0
                                val message =
                                    stringResource(R.string.organize_monthly_remaining, remaining)
                                val count = stringResource(
                                    R.string.organize_monthly_remaining_count,
                                    remaining
                                )
                                append(message)
                                val start = message.indexOf(count)
                                if (start >= 0) {
                                    addStyle(
                                        RecapHeading4.toSpanStyle().copy(color = RecapBlue300),
                                        start,
                                        start + count.length,
                                    )
                                }
                            },
                            style = RecapBody2,
                            color = RecapGray500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                        )
                    }
                    RecapButton(
                        text = stringResource(
                            R.string.organize_start_count,
                            uiState.selectionCount
                        ),
                        onClick = onStartOrganizingClick,
                        enabled = uiState.canProceed,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotConfirmationTopBar(
    selectionCount: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ScreenshotConfirmationTokens.TopBarHeight)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onBackClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left_24),
                    contentDescription = stringResource(R.string.organize_back_content_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = stringResource(R.string.organize_confirm_title),
                style = RecapHeading2,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
            )
            Text(
                text = stringResource(R.string.organize_confirm_count, selectionCount),
                style = RecapHeading2,
                fontWeight = FontWeight.Bold,
                color = RecapBlue500,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun ScreenshotConfirmationGridItem(
    screenshot: LocalImage,
    selectionOrder: Int,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ScreenshotConfirmationTokens.ItemCornerRadius)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .border(
                width = ScreenshotConfirmationTokens.ItemBorderWidth,
                color = RecapGray100,
                shape = shape,
            ),
    ) {
        AsyncImage(
            model = screenshot.uri.toUri(),
            contentDescription = stringResource(
                R.string.organize_selected_screenshot_item_content_description,
                selectionOrder,
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        ScreenshotConfirmationRemoveButton(
            onClick = onRemoveClick,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun ScreenshotConfirmationRemoveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .padding(ScreenshotConfirmationTokens.RemoveButtonPadding)
            .size(ScreenshotConfirmationTokens.RemoveButtonSize)
            .background(
                color = RecapGray300,
                shape = RoundedCornerShape(percent = ScreenshotConfirmationTokens.RemoveButtonCornerRadius),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close_24),
            contentDescription = stringResource(R.string.organize_remove_selection_content_description),
            modifier = Modifier.size(14.dp),
            tint = RecapGray50,
        )
    }
}

@Composable
private fun ScreenshotConfirmationAddItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = RecapGray200
    val shape = RoundedCornerShape(ScreenshotConfirmationTokens.ItemCornerRadius)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .drawBehind {
                val strokeWidth = ScreenshotConfirmationTokens.DashedBorderWidth.toPx()
                val dashPathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 8f),
                    0f,
                )
                val inset = strokeWidth / 2f
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(inset, inset),
                    size = Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth,
                    ),
                    cornerRadius = CornerRadius(
                        ScreenshotConfirmationTokens.ItemCornerRadius.toPx(),
                    ),
                    style = Stroke(width = strokeWidth, pathEffect = dashPathEffect),
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus_32),
            contentDescription = stringResource(R.string.organize_add_more_content_description),
            modifier = Modifier.size(24.dp),
            tint = RecapGray200,
        )
    }
}

private object ScreenshotConfirmationTokens {
    const val GridColumns = 3
    val TopBarHeight = 56.dp
    val HorizontalPadding = 16.dp
    val BottomFadeHeight = 72.dp
    val BottomContentTopPadding = 4.dp
    val BottomPadding = 20.dp
    val GridVerticalPadding = 12.dp
    val GridSpacing = 4.dp
    val ItemCornerRadius = 10.dp
    val ItemBorderWidth = 0.5.dp
    val RemoveButtonSize = 16.dp
    val RemoveButtonPadding = 6.dp
    val RemoveButtonCornerRadius = 50
    val DashedBorderWidth = 1.5.dp
}

@Preview(name = "Screenshot Confirmation Top Bar", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotConfirmationTopBarPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotConfirmationTopBar(
            selectionCount = 15,
            onBackClick = {},
        )
    }
}

@Preview(
    name = "Screenshot Confirmation Screen",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun ScreenshotConfirmationScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotConfirmationScreen(
            uiState = OrganizeUiState(
                isLoading = false,
                availableScreenshots = OrganizeConfirmationPreviewScreenshots,
                selectedUris = listOf(
                    OrganizeConfirmationPreviewScreenshots[0].uri,
                    OrganizeConfirmationPreviewScreenshots[1].uri,
                    OrganizeConfirmationPreviewScreenshots[2].uri,
                ),
            ),
            onAction = {},
            onBackClick = {},
            onAddMoreClick = {},
            onStartOrganizingClick = {},
        )
    }
}

internal val OrganizeConfirmationPreviewScreenshots = listOf(
    LocalImage(
        uri = "content://com.chalkak.recap.preview/screenshot/1",
        displayName = "screenshot-1",
        dateAddedMillis = 0L,
    ),
    LocalImage(
        uri = "content://com.chalkak.recap.preview/screenshot/2",
        displayName = "screenshot-2",
        dateAddedMillis = 0L,
    ),
    LocalImage(
        uri = "content://com.chalkak.recap.preview/screenshot/3",
        displayName = "screenshot-3",
        dateAddedMillis = 0L,
    ),
)

@Preview(name = "Monthly remaining - within limit", widthDp = 360, heightDp = 720)
@Composable
private fun MonthlyWithinLimitPreview() = MonthlyConfirmationPreview(5)

@Preview(name = "Monthly remaining - exceeded", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun MonthlyExceededPreview() = MonthlyConfirmationPreview(2)

@Preview(name = "Monthly remaining - zero", widthDp = 360, heightDp = 720)
@Composable
private fun MonthlyZeroPreview() = MonthlyConfirmationPreview(0)

@Composable
private fun MonthlyConfirmationPreview(remaining: Int) {
    RECAPTheme(dynamicColor = false) {
        ScreenshotConfirmationScreen(
            uiState = OrganizeUiState(
                isLoading = false,
                monthlyRemaining = remaining,
                hasEnteredConfirmation = true,
                availableScreenshots = OrganizeConfirmationPreviewScreenshots,
                selectedUris = OrganizeConfirmationPreviewScreenshots.map { it.uri },
            ),
            onAction = {},
            onBackClick = {},
            onAddMoreClick = {},
            onStartOrganizingClick = {},
        )
    }
}
