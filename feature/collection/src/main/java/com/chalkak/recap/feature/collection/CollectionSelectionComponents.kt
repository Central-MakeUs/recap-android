package com.chalkak.recap.feature.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500

@Composable
internal fun CollectionSelectionActions(
    selection: CollectionSelectionUiState,
    onStartSelection: () -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier,
    canStartSelection: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selection.isActive) {
            CollectionTextButton(
                text = stringResource(R.string.collection_cancel_selection_action),
                onClick = onCancelSelection,
                enabled = !selection.isDeleting,
            )
            CollectionTextButton(
                text = stringResource(
                    R.string.collection_delete_selected_action,
                    selection.selectedCount,
                ),
                onClick = onDeleteSelected,
                enabled = selection.selectedCount > 0 && !selection.isDeleting,
                primary = true,
            )
        } else {
            CollectionTextButton(
                text = stringResource(R.string.collection_select_action),
                onClick = onStartSelection,
                enabled = canStartSelection,
            )
        }
    }
}

@Composable
private fun CollectionTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = CollectionSelectionTokens.TextButtonMinHeight),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (primary) MaterialTheme.colorScheme.primary else RecapGray500,
            disabledContentColor = RecapGray300,
        ),
        contentPadding = PaddingValues(horizontal = CollectionSelectionTokens.TextButtonPadding),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun CollectionSelectionCheckbox(
    visible: Boolean,
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandHorizontally(
            animationSpec = collectionSelectionEnterTween(),
            expandFrom = Alignment.Start,
        ) + slideInHorizontally(
            animationSpec = collectionSelectionEnterTween(),
            initialOffsetX = { width -> -width },
        ) + fadeIn(animationSpec = collectionSelectionEnterTween()),
        exit = shrinkHorizontally(
            animationSpec = collectionSelectionExitTween(),
            shrinkTowards = Alignment.Start,
        ) + slideOutHorizontally(
            animationSpec = collectionSelectionExitTween(),
            targetOffsetX = { width -> -width },
        ) + fadeOut(animationSpec = collectionSelectionExitTween()),
    ) {
        Box(modifier = Modifier.padding(end = CollectionSelectionTokens.CheckboxEndSpacing)) {
            CollectionCheckboxIcon(
                checked = checked,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}

@Composable
internal fun CollectionAnimatedFavoriteVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        // Exit uses enter tween so it starts with the checkbox, not FastOutLinearIn's slow start.
        enter = expandHorizontally(
            animationSpec = collectionSelectionExitTween(),
            expandFrom = Alignment.End,
        ) + slideInHorizontally(
            animationSpec = collectionSelectionExitTween(),
            initialOffsetX = { width -> width },
        ) + fadeIn(animationSpec = collectionSelectionExitTween()),
        exit = shrinkHorizontally(
            animationSpec = collectionSelectionEnterTween(),
            shrinkTowards = Alignment.End,
        ) + slideOutHorizontally(
            animationSpec = collectionSelectionEnterTween(),
            targetOffsetX = { width -> width },
        ) + fadeOut(animationSpec = collectionSelectionEnterTween()),
    ) {
        content()
    }
}

@Composable
private fun CollectionCheckboxIcon(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(CollectionSelectionTokens.CheckboxContainerSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_checkbox_checked_16),
            contentDescription = null,
            modifier = Modifier.size(CollectionSelectionTokens.CheckboxIconSize),
            tint = if (checked) RecapBlue300 else RecapGray200,
        )
    }
}

@Composable
internal fun CollectionSelectableCaptureItem(
    item: CollectionCardItemUiModel,
    selection: CollectionSelectionUiState,
    onOpenClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSelectionToggle: () -> Unit,
    modifier: Modifier = Modifier,
    metadataMode: ScreenshotCardMetadataMode = ScreenshotCardMetadataMode.CategoryChip,
    showBottomDivider: Boolean = true,
) {
    val isSelected = item.imageId in selection.selectedImageIds
    val selectionContentDescription = stringResource(
        R.string.collection_selection_item_content_description,
        item.title,
        item.summary,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressAnimationSpec = tween<Float>(
        durationMillis = CollectionSelectionTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) CollectionSelectionTokens.PressedScale else 1f,
        animationSpec = pressAnimationSpec,
        label = "collection_capture_item_press_scale",
    )
    val rowShape = RoundedCornerShape(CollectionSelectionTokens.RowCornerRadius)
    val pressableModifier = if (selection.isActive) {
        Modifier
            .toggleable(
                value = isSelected,
                enabled = !selection.isDeleting,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = { onSelectionToggle() },
            )
            .semantics {
                contentDescription = selectionContentDescription
            }
    } else {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onOpenClick,
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(CollectionSelectionTokens.DividerGap))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CollectionSelectionTokens.ItemHorizontalPadding)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(rowShape)
                .background(RecapBackground)
                .then(pressableModifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CollectionSelectionCheckbox(
                visible = selection.isActive,
                checked = isSelected,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (selection.isActive) {
                            Modifier.clearAndSetSemantics { }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                ScreenshotCard(
                    thumbnailModel = item.thumbnailModel,
                    categoryType = item.categoryType,
                    organizedAtMillis = item.createdAtMillis,
                    metadataMode = metadataMode,
                    title = item.title,
                    description = item.summary,
                    isFavorite = item.isFavorite,
                    onClick = onOpenClick,
                    onFavoriteClick = onFavoriteClick,
                    horizontalContentPadding = 0.dp,
                    showFavoriteButton = !selection.isActive,
                    showBottomDivider = false,
                    containerClickEnabled = false,
                )
            }
        }
        Spacer(modifier = Modifier.height(CollectionSelectionTokens.DividerGap))
        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = CollectionSelectionTokens.ItemDividerThickness,
                color = RecapGray100,
            )
        }
    }
}

private fun <T> collectionSelectionEnterTween() = tween<T>(
    durationMillis = CollectionSelectionTokens.EnterAnimationDurationMillis,
    easing = LinearOutSlowInEasing,
)

private fun <T> collectionSelectionExitTween() = tween<T>(
    durationMillis = CollectionSelectionTokens.ExitAnimationDurationMillis,
    easing = FastOutLinearInEasing,
)

private object CollectionSelectionTokens {
    const val EnterAnimationDurationMillis = 180
    const val ExitAnimationDurationMillis = 150
    const val PressedScale = 0.9875f
    const val PressAnimationDurationMillis = 100
    val RowCornerRadius = 10.dp
    val DividerGap = 2.dp
    val CheckboxContainerSize = 24.dp
    val CheckboxIconSize = 16.dp
    val CheckboxEndSpacing = 8.dp
    val ItemHorizontalPadding = 16.dp
    val ItemDividerThickness = 1.dp
    val TextButtonMinHeight = 40.dp
    val TextButtonPadding = 4.dp
}

@Preview(name = "Collection Selection Actions Idle", showBackground = true, widthDp = 360)
@Composable
private fun CollectionSelectionActionsIdlePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionSelectionActions(
            selection = CollectionSelectionUiState(),
            onStartSelection = {},
            onCancelSelection = {},
            onDeleteSelected = {},
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Preview(name = "Collection Selection Actions Active", showBackground = true, widthDp = 360)
@Composable
private fun CollectionSelectionActionsActivePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionSelectionActions(
            selection = CollectionSelectionUiState(
                isActive = true,
                selectedImageIds = setOf("1", "2"),
            ),
            onStartSelection = {},
            onCancelSelection = {},
            onDeleteSelected = {},
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Preview(name = "Collection Selection Actions Deleting", showBackground = true, widthDp = 360)
@Composable
private fun CollectionSelectionActionsDeletingPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionSelectionActions(
            selection = CollectionSelectionUiState(
                isActive = true,
                selectedImageIds = setOf("1"),
                isDeleting = true,
            ),
            onStartSelection = {},
            onCancelSelection = {},
            onDeleteSelected = {},
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Preview(name = "Collection Selection Checkbox", showBackground = true, widthDp = 360)
@Composable
private fun CollectionSelectionCheckboxPreview() {
    RECAPTheme(dynamicColor = false) {
        Row(modifier = Modifier.padding(horizontal = 20.dp)) {
            CollectionSelectionCheckbox(
                visible = true,
                checked = false,
            )
            CollectionSelectionCheckbox(
                visible = true,
                checked = true,
            )
        }
    }
}

@Preview(name = "Collection Selectable Capture Item", showBackground = true, widthDp = 360)
@Composable
private fun CollectionSelectableCaptureItemPreview() {
    RECAPTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CollectionSelectableCaptureItem(
                item = previewCollectionCaptureItem(
                    categoryType = RecapCategoryType.ShoppingProduct,
                ),
                selection = CollectionSelectionUiState(),
                metadataMode = ScreenshotCardMetadataMode.CategoryChip,
                onOpenClick = {},
                onFavoriteClick = {},
                onSelectionToggle = {},
            )
            CollectionSelectableCaptureItem(
                item = previewCollectionCaptureItem(
                    categoryType = RecapCategoryType.Other,
                ),
                selection = CollectionSelectionUiState(),
                metadataMode = ScreenshotCardMetadataMode.OrganizedDate,
                onOpenClick = {},
                onFavoriteClick = {},
                onSelectionToggle = {},
            )
            CollectionSelectableCaptureItem(
                item = previewCollectionCaptureItem(
                    imageId = "preview-capture-selected",
                    categoryType = RecapCategoryType.Other,
                ),
                selection = CollectionSelectionUiState(
                    isActive = true,
                    selectedImageIds = setOf("preview-capture-selected"),
                ),
                metadataMode = ScreenshotCardMetadataMode.OrganizedDate,
                onOpenClick = {},
                onFavoriteClick = {},
                onSelectionToggle = {},
            )
        }
    }
}

private fun previewCollectionCaptureItem(
    imageId: String = "preview-capture",
    categoryType: RecapCategoryType,
): CollectionCardItemUiModel {
    return CollectionCardItemUiModel(
        imageId = imageId,
        title = "미분류 메모",
        summary = "카테고리 없이 저장된 캡처",
        contentTypeLabelResId = R.string.collection_content_type_other,
        categoryType = categoryType,
        createdAtMillis = 1_719_446_400_000L,
        isFavorite = false,
        thumbnailModel = null,
    )
}
