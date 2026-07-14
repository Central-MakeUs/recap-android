package com.chalkak.recap.feature.organize

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.model.LocalImage

// UNUSED
@Composable
fun ScreenshotSelectionScreen(
    uiState: OrganizeUiState,
    onAction: (OrganizeAction) -> Unit,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenshotSelectionTopBar(
                selectionCount = uiState.selectionCount,
                canProceed = uiState.canProceed,
                onCancelClick = onCancelClick,
                onNextClick = onNextClick,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    uiState.availableScreenshots.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.organize_selection_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RecapGray300,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = ScreenshotSelectionTokens.HorizontalPadding),
                        )
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(ScreenshotSelectionTokens.GridColumns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = ScreenshotSelectionTokens.HorizontalPadding,
                                vertical = ScreenshotSelectionTokens.GridVerticalPadding,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(
                                ScreenshotSelectionTokens.GridSpacing,
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                ScreenshotSelectionTokens.GridSpacing,
                            ),
                        ) {
                            items(
                                items = uiState.availableScreenshots,
                                key = { it.uri },
                            ) { screenshot ->
                                ScreenshotSelectionGridItem(
                                    screenshot = screenshot,
                                    selectionOrder = uiState.selectionOrder(screenshot.uri),
                                    onClick = {
                                        onAction(OrganizeAction.ToggleSelection(screenshot.uri))
                                    },
                                )
                            }
                        }
                    }
                }
            }
            RecapButton(
                text = stringResource(
                    R.string.organize_selection_complete_with_count,
                    uiState.selectionCount,
                ),
                onClick = onNextClick,
                enabled = uiState.canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ScreenshotSelectionTokens.HorizontalPadding,
                        vertical = ScreenshotSelectionTokens.BottomPadding,
                    ),
            )
        }
    }
}

@Composable
private fun ScreenshotSelectionTopBar(
    selectionCount: Int,
    canProceed: Boolean,
    onCancelClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ScreenshotSelectionTokens.TopBarHeight)
                .padding(horizontal = ScreenshotSelectionTokens.TopBarHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScreenshotSelectionTopBarTextButton(
                text = stringResource(R.string.organize_cancel),
                color = RecapGray900,
                onClick = onCancelClick,
            )
            Text(
                text = stringResource(R.string.organize_screenshot_selection_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            ScreenshotSelectionTopBarTextButton(
                text = stringResource(R.string.organize_next_with_count, selectionCount),
                color = if (canProceed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    RecapGray300
                },
                enabled = canProceed,
                onClick = onNextClick,
            )
        }
    }
}

@Composable
private fun ScreenshotSelectionTopBarTextButton(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
    )
}

@Composable
private fun ScreenshotSelectionGridItem(
    screenshot: LocalImage,
    selectionOrder: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(ScreenshotSelectionTokens.ItemCornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        AsyncImage(
            model = screenshot.uri.toUri(),
            contentDescription = stringResource(
                R.string.organize_screenshot_item_content_description,
                selectionOrder ?: 0,
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        if (selectionOrder != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(ScreenshotSelectionTokens.BadgePadding)
                    .size(ScreenshotSelectionTokens.BadgeSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = selectionOrder.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

private object ScreenshotSelectionTokens {
    const val GridColumns = 3
    val TopBarHeight = 56.dp
    val TopBarHorizontalPadding = 12.dp
    val HorizontalPadding = 16.dp
    val BottomPadding = 20.dp
    val GridVerticalPadding = 12.dp
    val GridSpacing = 4.dp
    val ItemCornerRadius = 8.dp
    val BadgeSize = 24.dp
    val BadgePadding = 6.dp
}

@Preview(name = "Screenshot Selection Screen", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ScreenshotSelectionScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotSelectionScreen(
            uiState = OrganizeUiState(
                isLoading = false,
                availableScreenshots = OrganizePreviewScreenshots,
                selectedUris = listOf(
                    OrganizePreviewScreenshots[0].uri,
                    OrganizePreviewScreenshots[1].uri,
                    OrganizePreviewScreenshots[2].uri,
                ),
            ),
            onAction = {},
            onCancelClick = {},
            onNextClick = {},
        )
    }
}

@Preview(name = "Screenshot Selection Screen - Empty", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotSelectionScreenEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotSelectionScreen(
            uiState = OrganizeUiState(isLoading = false),
            onAction = {},
            onCancelClick = {},
            onNextClick = {},
        )
    }
}

internal val OrganizePreviewScreenshots = listOf(
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
    LocalImage(
        uri = "content://com.chalkak.recap.preview/screenshot/4",
        displayName = "screenshot-4",
        dateAddedMillis = 0L,
    ),
    LocalImage(
        uri = "content://com.chalkak.recap.preview/screenshot/5",
        displayName = "screenshot-5",
        dateAddedMillis = 0L,
    ),
    LocalImage(
        uri = "content://com.chalkak.recap.preview/screenshot/6",
        displayName = "screenshot-6",
        dateAddedMillis = 0L,
    ),
)
