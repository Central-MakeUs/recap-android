package com.chalkak.recap.core.design.component.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapErrorContainer
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
import kotlin.math.abs
import kotlin.math.roundToInt

val LocalSwipeRevealActions = compositionLocalOf<List<SwipeAction>> { emptyList() }

val LocalSwipeActionRowActive = compositionLocalOf { false }

@Immutable
data class SwipeAction(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit,
)

private enum class SwipeActionRowValue {
    Resting,
    Revealed,
}

@Composable
fun rememberEditDeleteSwipeActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
): List<SwipeAction> {
    val currentOnEditClick = rememberUpdatedState(onEditClick)
    val currentOnDeleteClick = rememberUpdatedState(onDeleteClick)
    val editLabel = stringResource(R.string.screenshot_card_swipe_edit)
    val deleteLabel = stringResource(R.string.screenshot_card_swipe_delete)
    return remember(editLabel, deleteLabel) {
        listOf(
            SwipeAction(
                label = editLabel,
                containerColor = RecapGray50,
                contentColor = RecapGray700,
                onClick = { currentOnEditClick.value() },
            ),
            SwipeAction(
                label = deleteLabel,
                containerColor = RecapErrorContainer,
                contentColor = RecapError,
                onClick = { currentOnDeleteClick.value() },
            ),
        )
    }
}

@Composable
fun SwipeActionRow(
    actions: List<SwipeAction>,
    revealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDragStarted: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (!enabled || actions.isEmpty()) {
        Column(modifier = modifier.fillMaxWidth()) {
            content()
            SwipeActionRowDivider()
        }
        return
    }

    var containerWidthPx by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                if (containerWidthPx != size.width) {
                    containerWidthPx = size.width
                }
            },
    ) {
        val revealPx = remember(containerWidthPx, actions.size) {
            containerWidthPx * SwipeActionRowTokens.ActionWidthFraction * actions.size
        }
        val anchors = remember(revealPx) {
            DraggableAnchors {
                SwipeActionRowValue.Resting at 0f
                SwipeActionRowValue.Revealed at -revealPx
            }
        }
        val state = remember {
            AnchoredDraggableState(
                initialValue = if (revealed) {
                    SwipeActionRowValue.Revealed
                } else {
                    SwipeActionRowValue.Resting
                },
                anchors = anchors,
            )
        }
        SideEffect {
            state.updateAnchors(anchors)
        }

        val currentRevealed = rememberUpdatedState(revealed)
        val currentOnRevealedChange = rememberUpdatedState(onRevealedChange)
        val currentOnDragStarted = rememberUpdatedState(onDragStarted)
        val dragInteractionSource = remember { MutableInteractionSource() }
        val closeInteractionSource = remember { MutableInteractionSource() }
        val isDragged by dragInteractionSource.collectIsDraggedAsState()
        val offsetActive by remember {
            derivedStateOf {
                val offset = state.offset
                !offset.isNaN() && abs(offset) > SwipeActionRowTokens.OffsetVisibleEpsilon
            }
        }
        val showActions = revealed || offsetActive
        val suppressPressScale = isDragged || offsetActive
        LaunchedEffect(state) {
            snapshotFlow { state.settledValue }
                .collect { settled ->
                    val isRevealed = settled == SwipeActionRowValue.Revealed
                    if (isRevealed != currentRevealed.value) {
                        currentOnRevealedChange.value(isRevealed)
                    }
                }
        }
        LaunchedEffect(revealed) {
            val target = if (revealed) {
                SwipeActionRowValue.Revealed
            } else {
                SwipeActionRowValue.Resting
            }
            if (state.targetValue != target) {
                state.animateTo(target)
            }
        }
        LaunchedEffect(isDragged) {
            if (isDragged) {
                currentOnDragStarted.value()
            }
        }

        CompositionLocalProvider(
            LocalSwipeRevealActions provides actions,
            LocalSwipeActionRowActive provides suppressPressScale,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (showActions) {
                    SwipeActionRowActions(
                        actions = actions,
                        revealed = revealed,
                        containerWidthPx = containerWidthPx,
                        modifier = Modifier.matchParentSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            val rawOffset = state.offset
                            IntOffset(
                                x = if (rawOffset.isNaN()) 0 else rawOffset.roundToInt(),
                                y = 0,
                            )
                        }
                        .anchoredDraggable(
                            state = state,
                            orientation = Orientation.Horizontal,
                            interactionSource = dragInteractionSource,
                        ),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        content()
                        SwipeActionRowDivider()
                    }
                    if (revealed) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = closeInteractionSource,
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { onRevealedChange(false) },
                                )
                                .clearAndSetSemantics { },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeActionRowActions(
    actions: List<SwipeAction>,
    revealed: Boolean,
    containerWidthPx: Int,
    modifier: Modifier = Modifier,
) {
    val actionWidth = with(LocalDensity.current) {
        (containerWidthPx * SwipeActionRowTokens.ActionWidthFraction).toDp()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .clearAndSetSemantics { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f))
        actions.forEach { action ->
            key(action.label) {
                SwipeActionButton(
                    text = action.label,
                    containerColor = action.containerColor,
                    contentColor = action.contentColor,
                    clickEnabled = revealed,
                    onClick = action.onClick,
                    modifier = Modifier
                        .width(actionWidth)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun SwipeActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    clickEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = if (clickEnabled) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .background(containerColor)
            .then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = RecapHeading4,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SwipeActionRowDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = SwipeActionRowTokens.DividerThickness,
        color = RecapGray100,
    )
}

private object SwipeActionRowTokens {
    const val ActionWidthFraction = 0.2f
    const val OffsetVisibleEpsilon = 0.5f
    val DividerThickness = 1.dp
}

@Preview(name = "Swipe Action Row revealed", showBackground = true, widthDp = 360)
@Composable
private fun SwipeActionRowRevealedPreview() {
    RECAPTheme(dynamicColor = false) {
        SwipeActionRow(
            actions = rememberEditDeleteSwipeActions(
                onEditClick = {},
                onDeleteClick = {},
            ),
            revealed = true,
            onRevealedChange = {},
            modifier = Modifier.padding(24.dp),
        ) {
            ScreenshotCard(
                thumbnailModel = R.drawable.bid_landscape_24px,
                categoryType = RecapCategoryType.ScheduleReservation,
                title = SwipeActionRowPreviewTitle,
                description = SwipeActionRowPreviewDescription,
                isFavorite = false,
                onClick = {},
                onFavoriteClick = {},
            )
        }
    }
}

@Preview(name = "Swipe Action Row stacked", showBackground = true, widthDp = 360)
@Composable
private fun SwipeActionRowStackedPreview() {
    RECAPTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            SwipeActionRow(
                actions = rememberEditDeleteSwipeActions(
                    onEditClick = {},
                    onDeleteClick = {},
                ),
                revealed = true,
                onRevealedChange = {},
            ) {
                ScreenshotCard(
                    thumbnailModel = R.drawable.bid_landscape_24px,
                    categoryType = RecapCategoryType.ScheduleReservation,
                    title = SwipeActionRowPreviewTitle,
                    description = SwipeActionRowPreviewDescription,
                    isFavorite = false,
                    onClick = {},
                    onFavoriteClick = {},
                )
            }
            SwipeActionRow(
                actions = rememberEditDeleteSwipeActions(
                    onEditClick = {},
                    onDeleteClick = {},
                ),
                revealed = false,
                onRevealedChange = {},
            ) {
                ScreenshotCard(
                    thumbnailModel = R.drawable.bid_landscape_24px,
                    categoryType = RecapCategoryType.InfoKnowledge,
                    title = SwipeActionRowPreviewTitle,
                    description = SwipeActionRowPreviewDescription,
                    isFavorite = true,
                    onClick = {},
                    onFavoriteClick = {},
                )
            }
        }
    }
}

private const val SwipeActionRowPreviewTitle = "파스타 레시피 저장"
private const val SwipeActionRowPreviewDescription =
    "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약"
