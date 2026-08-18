package com.chalkak.recap.core.design.component.card

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapErrorContainer
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
import kotlin.math.roundToInt

internal enum class ScreenshotCardSwipeValue {
    Resting,
    Revealed,
}

@Composable
internal fun ScreenshotCardSwipePane(
    revealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSwipeDragStarted: () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val revealPx = remember(containerWidthPx) {
        containerWidthPx * ScreenshotCardTokens.SwipeRevealWidthFraction
    }
    val anchors = remember(revealPx) {
        DraggableAnchors {
            ScreenshotCardSwipeValue.Resting at 0f
            ScreenshotCardSwipeValue.Revealed at -revealPx
        }
    }
    val state = remember {
        AnchoredDraggableState(
            initialValue = if (revealed) {
                ScreenshotCardSwipeValue.Revealed
            } else {
                ScreenshotCardSwipeValue.Resting
            },
            anchors = anchors,
        )
    }
    SideEffect {
        state.updateAnchors(anchors)
    }

    val currentRevealed = rememberUpdatedState(revealed)
    val currentOnRevealedChange = rememberUpdatedState(onRevealedChange)
    val currentOnSwipeDragStarted = rememberUpdatedState(onSwipeDragStarted)
    val dragInteractionSource = remember { MutableInteractionSource() }
    val isDragged by dragInteractionSource.collectIsDraggedAsState()
    LaunchedEffect(state) {
        snapshotFlow { state.settledValue }
            .collect { settled ->
                val isRevealed = settled == ScreenshotCardSwipeValue.Revealed
                if (isRevealed != currentRevealed.value) {
                    currentOnRevealedChange.value(isRevealed)
                }
            }
    }
    LaunchedEffect(revealed) {
        val target = if (revealed) {
            ScreenshotCardSwipeValue.Revealed
        } else {
            ScreenshotCardSwipeValue.Resting
        }
        if (state.targetValue != target) {
            state.animateTo(target)
        }
    }
    LaunchedEffect(isDragged) {
        if (isDragged) {
            currentOnSwipeDragStarted.value()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        ScreenshotCardSwipeActions(
            revealed = revealed,
            onEditClick = {
                onRevealedChange(false)
                onEditClick()
            },
            onDeleteClick = {
                onRevealedChange(false)
                onDeleteClick()
            },
            modifier = Modifier.matchParentSize(),
        )
        content(
            Modifier
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
        )
    }
}

@Composable
internal fun ScreenshotCardSwipeActions(
    revealed: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionWidth = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    } * ScreenshotCardTokens.SwipeActionWidthFraction
    val actionsModifier = if (revealed) {
        modifier.fillMaxSize()
    } else {
        modifier
            .fillMaxSize()
            .clearAndSetSemantics { }
    }

    Row(
        modifier = actionsModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f))
        ScreenshotCardSwipeActionButton(
            text = stringResource(R.string.screenshot_card_swipe_edit),
            containerColor = RecapGray50,
            contentColor = RecapGray700,
            clickEnabled = revealed,
            onClick = onEditClick,
            modifier = Modifier
                .width(actionWidth)
                .fillMaxHeight(),
        )
        ScreenshotCardSwipeActionButton(
            text = stringResource(R.string.screenshot_card_swipe_delete),
            containerColor = RecapErrorContainer,
            contentColor = RecapError,
            clickEnabled = revealed,
            onClick = onDeleteClick,
            modifier = Modifier
                .width(actionWidth)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun ScreenshotCardSwipeActionButton(
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
