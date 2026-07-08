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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray900
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
    val selectedScreenshots = uiState.availableScreenshots.filter { screenshot ->
        screenshot.uri in uiState.selectedUris
    }.sortedBy { screenshot ->
        uiState.selectedUris.indexOf(screenshot.uri)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenshotConfirmationTopBar(
                selectionCount = uiState.selectionCount,
                onBackClick = onBackClick,
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(ScreenshotConfirmationTokens.GridColumns),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = ScreenshotConfirmationTokens.HorizontalPadding,
                    vertical = ScreenshotConfirmationTokens.GridVerticalPadding,
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
                item(span = { GridItemSpan(1) }) {
                    ScreenshotConfirmationAddItem(
                        onClick = onAddMoreClick,
                    )
                }
            }
            RecapButton(
                text = stringResource(R.string.organize_start_organizing),
                onClick = onStartOrganizingClick,
                enabled = uiState.canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ScreenshotConfirmationTokens.HorizontalPadding,
                        vertical = ScreenshotConfirmationTokens.BottomPadding,
                    ),
            )
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.organize_confirm_count, selectionCount),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray300,
                modifier = Modifier.padding(end = 12.dp),
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
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(ScreenshotConfirmationTokens.ItemCornerRadius)),
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
            imageVector = Icons.Outlined.Close,
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
    val borderColor = MaterialTheme.colorScheme.primary
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
            painter = painterResource(R.drawable.ic_plus_30),
            contentDescription = stringResource(R.string.organize_add_more_content_description),
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

private object ScreenshotConfirmationTokens {
    const val GridColumns = 3
    val TopBarHeight = 56.dp
    val HorizontalPadding = 16.dp
    val BottomPadding = 20.dp
    val GridVerticalPadding = 12.dp
    val GridSpacing = 4.dp
    val ItemCornerRadius = 8.dp
    val RemoveButtonSize = 16.dp
    val RemoveButtonPadding = 6.dp
    val RemoveButtonCornerRadius = 50
    val DashedBorderWidth = 1.5.dp
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
                availableScreenshots = OrganizePreviewScreenshots,
                selectedUris = listOf(
                    OrganizePreviewScreenshots[0].uri,
                    OrganizePreviewScreenshots[1].uri,
                    OrganizePreviewScreenshots[2].uri,
                ),
            ),
            onAction = {},
            onBackClick = {},
            onAddMoreClick = {},
            onStartOrganizingClick = {},
        )
    }
}
