package com.chalkak.recap.feature.organize

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.LocalImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotPicker(
    uiState: OrganizeUiState,
    onAction: (OrganizeAction) -> Unit,
    onDismissRequest: () -> Unit,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = ScreenshotPickerTokens.SheetTopCornerRadius,
            topEnd = ScreenshotPickerTokens.SheetTopCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null,
    ) {
        ScreenshotPickerContent(
            uiState = uiState,
            onAction = onAction,
            onCloseClick = onCloseClick,
            onConfirmClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * ScreenshotPickerTokens.SheetHeightFraction)
                .navigationBarsPadding(),
        )
    }
}

@Composable
fun ScreenshotPickerContent(
    uiState: OrganizeUiState,
    onAction: (OrganizeAction) -> Unit,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .background(MaterialTheme.colorScheme.surface),
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
                            .padding(horizontal = ScreenshotPickerTokens.EmptyHorizontalPadding),
                    )
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(ScreenshotPickerTokens.GridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = ScreenshotPickerTokens.ToolbarHeight,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(
                            ScreenshotPickerTokens.GridSpacing,
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            ScreenshotPickerTokens.GridSpacing,
                        ),
                    ) {
                        itemsIndexed(
                            items = uiState.availableScreenshots,
                            key = { _, screenshot -> screenshot.uri },
                        ) { index, screenshot ->
                            ScreenshotPickerGridItem(
                                imageModel = screenshot.toSheetImageModel(),
                                itemIndex = index + 1,
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

        ScreenshotPickerToolbar(
            hazeState = hazeState,
            canProceed = uiState.canProceed,
            onCloseClick = onCloseClick,
            onConfirmClick = onConfirmClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun ScreenshotPickerToolbar(
    hazeState: HazeState,
    canProceed: Boolean,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(ScreenshotPickerTokens.ToolbarHeight)
            .padding(horizontal = ScreenshotPickerTokens.ToolbarHorizontalPadding),
    ) {
        ScreenshotPickerGlassIconButton(
            hazeState = hazeState,
            iconResId = R.drawable.ic_close_24,
            contentDescription = stringResource(
                R.string.screenshot_picker_close_content_description,
            ),
            enabled = true,
            iconTint = RecapGray900,
            onClick = onCloseClick,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        Text(
            text = stringResource(R.string.organize_screenshot_selection_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RecapGray900,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )

        ScreenshotPickerGlassIconButton(
            hazeState = hazeState,
            iconResId = R.drawable.ic_check_24,
            contentDescription = stringResource(
                R.string.screenshot_picker_confirm_content_description,
            ),
            enabled = canProceed,
            iconTint = if (canProceed) RecapGray900 else RecapGray300,
            onClick = onConfirmClick,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun ScreenshotPickerGlassIconButton(
    hazeState: HazeState,
    iconResId: Int,
    contentDescription: String,
    enabled: Boolean,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val glassStyle = CupertinoMaterials.ultraThin()

    Box(
        modifier = modifier
            .size(ScreenshotPickerTokens.ControlMinSize)
            .shadow(
                elevation = ScreenshotPickerTokens.GlassShadowElevation,
                shape = CircleShape,
                ambientColor = ScreenshotPickerTokens.GlassShadowColor,
                spotColor = ScreenshotPickerTokens.GlassShadowColor,
            )
            .hazeEffect(state = hazeState) {
                blurEffect {
                    blurEnabled = true
                    blurRadius = ScreenshotPickerTokens.GlassBlurRadius
                    style = glassStyle
                    colorEffects = listOf(
                        HazeColorEffect.tint(
                            White.copy(alpha = ScreenshotPickerTokens.GlassTintAlpha),
                        ),
                    )
                    noiseFactor = ScreenshotPickerTokens.GlassNoiseFactor
                }
            }
            .border(
                width = 1.dp,
                color = ScreenshotPickerTokens.GlassBorderColor,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .semantics {
                if (!enabled) {
                    disabled()
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(ScreenshotPickerTokens.IconSize),
            tint = iconTint,
        )
    }
}

@Composable
private fun ScreenshotPickerGridItem(
    imageModel: Any,
    itemIndex: Int,
    selectionOrder: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentDescription = if (selectionOrder != null) {
        stringResource(
            R.string.organize_selected_screenshot_item_content_description,
            selectionOrder,
        )
    } else {
        stringResource(
            R.string.organize_screenshot_item_content_description,
            itemIndex,
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
        if (selectionOrder != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(ScreenshotPickerTokens.BadgePadding)
                    .size(ScreenshotPickerTokens.BadgeSize)
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

private fun LocalImage.toSheetImageModel(): Any {
    val drawableResId = uri.toPreviewDrawableResIdOrNull()
    return drawableResId ?: uri.toUri()
}

private fun String.toPreviewDrawableResIdOrNull(): Int? {
    if (!startsWith(PreviewDrawableUriPrefix)) return null
    return removePrefix(PreviewDrawableUriPrefix).toIntOrNull()
}

private fun previewLocalImage(
    @DrawableRes drawableResId: Int,
    displayName: String,
    dateAddedMillis: Long,
): LocalImage = LocalImage(
    uri = "$PreviewDrawableUriPrefix$drawableResId",
    displayName = displayName,
    dateAddedMillis = dateAddedMillis,
)

private const val PreviewDrawableUriPrefix = "drawable://"

val ScreenshotPickerPreviewScreenshots = listOf(
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_return,
        displayName = "screenshot-return",
        dateAddedMillis = 6L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_hotel,
        displayName = "screenshot-hotel",
        dateAddedMillis = 5L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_recipe,
        displayName = "screenshot-recipe",
        dateAddedMillis = 4L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_tax,
        displayName = "screenshot-tax",
        dateAddedMillis = 3L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_restaurant,
        displayName = "screenshot-restaurant",
        dateAddedMillis = 2L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_return,
        displayName = "screenshot-return-2",
        dateAddedMillis = 1L,
    ),
)

private object ScreenshotPickerTokens {
    const val SheetHeightFraction = 0.88f
    const val GridColumns = 3
    const val GlassTintAlpha = 0.62f
    const val GlassNoiseFactor = 0.12f

    val SheetTopCornerRadius = 40.dp
    val ToolbarHeight = 64.dp
    val ToolbarHorizontalPadding = 16.dp
    val ControlMinSize = 48.dp
    val IconSize = 24.dp
    val GridSpacing = 2.dp
    val EmptyHorizontalPadding = 24.dp
    val BadgeSize = 24.dp
    val BadgePadding = 6.dp
    val GlassBlurRadius: Dp = 24.dp
    val GlassShadowElevation: Dp = 12.dp
    val GlassBorderColor: Color = White.copy(alpha = 0.55f)
    val GlassShadowColor: Color = Black.copy(alpha = 0.18f)
}

@Preview(
    name = "Screenshot Picker - Populated",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(
                isLoading = false,
                availableScreenshots = ScreenshotPickerPreviewScreenshots,
                selectedUris = listOf(
                    ScreenshotPickerPreviewScreenshots[0].uri,
                    ScreenshotPickerPreviewScreenshots[1].uri,
                    ScreenshotPickerPreviewScreenshots[2].uri,
                ),
            ),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}

@Preview(
    name = "Screenshot Picker - Empty",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(isLoading = false),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}

@Preview(
    name = "Screenshot Picker - Loading",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerLoadingPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(isLoading = true),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}
