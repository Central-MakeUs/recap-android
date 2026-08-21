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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4
import kotlin.math.abs
import kotlin.math.roundToInt

@Immutable
internal data class SwipeRevealAction(
    val label: String,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit,
)

private enum class SwipeRevealValue {
    Resting,
    Revealed,
}

@Composable
internal fun SwipeRevealRow(
    actions: List<SwipeRevealAction>,
    revealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDragStarted: () -> Unit = {},
    content: @Composable (isGestureActive: Boolean) -> Unit,
) {
    if (!enabled || actions.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth()) { content(false) }
        return
    }

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val target = if (revealed) SwipeRevealValue.Revealed else SwipeRevealValue.Resting
    val state = remember { AnchoredDraggableState(initialValue = target) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                val revealPx = size.width * SwipeRevealTokens.ActionWidthFraction * actions.size
                if (revealPx > 0f) {
                    state.updateAnchors(swipeRevealAnchors(revealPx), target)
                }
                containerWidthPx = size.width
            },
    ) {
        val revealPx = remember(containerWidthPx, actions.size) {
            containerWidthPx * SwipeRevealTokens.ActionWidthFraction * actions.size
        }
        val anchors = remember(revealPx) { swipeRevealAnchors(revealPx) }
        SideEffect {
            if (revealPx > 0f) state.updateAnchors(anchors, target)
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
                !offset.isNaN() && abs(offset) > SwipeRevealTokens.OffsetVisibleEpsilon
            }
        }
        val showActions = revealed || offsetActive
        val gestureActive = isDragged || offsetActive

        LaunchedEffect(state) {
            snapshotFlow { state.settledValue }.collect { settled ->
                val isRevealed = settled == SwipeRevealValue.Revealed
                if (isRevealed != currentRevealed.value) {
                    currentOnRevealedChange.value(isRevealed)
                }
            }
        }
        LaunchedEffect(revealed) {
            if (state.targetValue != target) state.animateTo(target)
        }
        LaunchedEffect(isDragged) {
            if (isDragged) currentOnDragStarted.value()
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            if (showActions) {
                SwipeRevealActions(
                    actions = actions,
                    revealed = revealed,
                    modifier = Modifier.matchParentSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset {
                        val rawOffset = state.offset
                        IntOffset(if (rawOffset.isNaN()) 0 else rawOffset.roundToInt(), 0)
                    }
                    .anchoredDraggable(
                        state = state,
                        orientation = Orientation.Horizontal,
                        interactionSource = dragInteractionSource,
                    ),
            ) {
                content(gestureActive)
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

@Composable
private fun SwipeRevealActions(
    actions: List<SwipeRevealAction>,
    revealed: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize().clearAndSetSemantics { },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val contentWeight =
            (1f - SwipeRevealTokens.ActionWidthFraction * actions.size).coerceAtLeast(0f)
        if (contentWeight > 0f) Box(modifier = Modifier.weight(contentWeight))
        actions.forEach { action ->
            key(action.label) {
                SwipeRevealActionButton(
                    action = action,
                    clickEnabled = revealed,
                    modifier = Modifier
                        .weight(SwipeRevealTokens.ActionWidthFraction)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun SwipeRevealActionButton(
    action: SwipeRevealAction,
    clickEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = if (clickEnabled) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = action.onClick,
        )
    } else {
        Modifier
    }
    Box(
        modifier = modifier.background(action.containerColor).then(clickModifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = action.label,
            style = RecapHeading4,
            color = action.contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

private object SwipeRevealTokens {
    const val ActionWidthFraction = 0.2f
    const val OffsetVisibleEpsilon = 0.5f
}

private fun swipeRevealAnchors(revealPx: Float): DraggableAnchors<SwipeRevealValue> =
    DraggableAnchors {
        SwipeRevealValue.Resting at 0f
        SwipeRevealValue.Revealed at -revealPx
    }
