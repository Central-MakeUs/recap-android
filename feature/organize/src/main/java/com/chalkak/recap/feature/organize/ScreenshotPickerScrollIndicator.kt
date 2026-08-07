package com.chalkak.recap.feature.organize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun ScreenshotPickerScrollIndicator(
    gridState: LazyGridState,
    columnCount: Int,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val canScroll by remember {
        derivedStateOf {
            gridState.canScrollForward || gridState.canScrollBackward
        }
    }
    val scrollFraction by remember(columnCount) {
        derivedStateOf {
            gridState.scrollFraction(columnCount = columnCount)
        }
    }
    val displayFraction = if (isDragging) dragFraction else scrollFraction
    val currentScrollFraction by rememberUpdatedState(scrollFraction)

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.isScrollInProgress to isDragging }
            .distinctUntilChanged()
            .collectLatest { (scrolling, dragging) ->
                if (scrolling || dragging) {
                    visible = true
                } else {
                    delay(ScreenshotPickerScrollIndicatorTokens.HideDelay)
                    visible = false
                }
            }
    }

    LaunchedEffect(isDragging, dragFraction, columnCount) {
        if (!isDragging) return@LaunchedEffect
        gridState.scrollToFraction(
            fraction = dragFraction,
            columnCount = columnCount,
        )
    }

    val density = LocalDensity.current
    val pillHeightPx = with(density) {
        ScreenshotPickerScrollIndicatorTokens.PillHeight.toPx()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(ScreenshotPickerScrollIndicatorTokens.TrackWidth),
    ) {
        val maxOffsetPx = (constraints.maxHeight - pillHeightPx).coerceAtLeast(0f)
        val offsetY = (maxOffsetPx * displayFraction).roundToInt()
        val showIndicator = visible && canScroll

        AnimatedVisibility(
            visible = showIndicator,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = ScreenshotPickerScrollIndicatorTokens.EndPadding)
                .offset { IntOffset(0, offsetY) },
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = ScreenshotPickerScrollIndicatorTokens.AnimationDurationMillis,
                ),
            ) + scaleIn(
                animationSpec = tween(
                    durationMillis = ScreenshotPickerScrollIndicatorTokens.AnimationDurationMillis,
                ),
                initialScale = ScreenshotPickerScrollIndicatorTokens.HiddenScale,
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = ScreenshotPickerScrollIndicatorTokens.AnimationDurationMillis,
                ),
            ) + scaleOut(
                animationSpec = tween(
                    durationMillis = ScreenshotPickerScrollIndicatorTokens.AnimationDurationMillis,
                ),
                targetScale = ScreenshotPickerScrollIndicatorTokens.HiddenScale,
            ),
        ) {
            ScreenshotPickerScrollIndicatorPill(
                modifier = Modifier.pointerInput(maxOffsetPx) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragFraction = currentScrollFraction
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (maxOffsetPx <= 0f) return@detectDragGestures
                            dragFraction = (dragFraction + dragAmount.y / maxOffsetPx)
                                .coerceIn(0f, 1f)
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun ScreenshotPickerScrollIndicatorPill(
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    Column(
        modifier = modifier
            .size(
                width = ScreenshotPickerScrollIndicatorTokens.PillWidth,
                height = ScreenshotPickerScrollIndicatorTokens.PillHeight,
            )
            .shadow(
                elevation = ScreenshotPickerScrollIndicatorTokens.ShadowElevation,
                shape = shape,
            )
            .background(
                color = ScreenshotPickerScrollIndicatorTokens.PillBackground,
                shape = shape,
            )
            .border(
                width = ScreenshotPickerScrollIndicatorTokens.BorderWidth,
                color = ScreenshotPickerScrollIndicatorTokens.PillBorder,
                shape = shape,
            )
            // 터치 스크롤용 보조 UI라 TalkBack 포커스/안내에서 제외한다.
            .clearAndSetSemantics { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            ScreenshotPickerScrollIndicatorTokens.TriangleSpacing,
            Alignment.CenterVertically,
        ),
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowDropUp,
            contentDescription = null,
            modifier = Modifier.size(ScreenshotPickerScrollIndicatorTokens.IconSize),
            tint = White,
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(ScreenshotPickerScrollIndicatorTokens.IconSize),
            tint = White,
        )
    }
}

private suspend fun LazyGridState.scrollToFraction(
    fraction: Float,
    columnCount: Int,
) {
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems <= 0 || columnCount <= 0) return

    val totalRows = (totalItems + columnCount - 1) / columnCount
    val maxRowIndex = (totalRows - 1).coerceAtLeast(0)
    val targetRow = (maxRowIndex * fraction.coerceIn(0f, 1f)).roundToInt()
        .coerceIn(0, maxRowIndex)
    val targetIndex = (targetRow * columnCount).coerceIn(0, totalItems - 1)
    scrollToItem(targetIndex)
}

private fun LazyGridState.scrollFraction(columnCount: Int): Float {
    val layoutInfo = layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    if (totalItems <= 0 || columnCount <= 0) return 0f

    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return 0f

    val rowHeight = visibleItems.first().size.height + layoutInfo.mainAxisItemSpacing
    if (rowHeight <= 0) return 0f

    val totalRows = (totalItems + columnCount - 1) / columnCount
    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val totalContentHeight = (totalRows * rowHeight) - layoutInfo.mainAxisItemSpacing
    val maxScroll = (totalContentHeight - viewportHeight).coerceAtLeast(1)
    val firstVisibleRow = firstVisibleItemIndex / columnCount
    val scrollY = (firstVisibleRow * rowHeight) + firstVisibleItemScrollOffset
    return (scrollY.toFloat() / maxScroll).coerceIn(0f, 1f)
}

private object ScreenshotPickerScrollIndicatorTokens {
    val HideDelay = 1.seconds
    const val AnimationDurationMillis = 160
    const val HiddenScale = 0.82f

    val TrackWidth = 40.dp
    val EndPadding = 4.dp
    val PillWidth = 32.dp
    val PillHeight = 48.dp
    val IconSize = 16.dp
    val TriangleSpacing = 0.dp
    val ShadowElevation = 0.dp
    val BorderWidth = 0.5.dp

    // 화이트 테마 그리드 위에서도 대비가 나도록 레퍼런스와 같은 다크 반투명 pill을 사용한다.
    val PillBackground = RecapGray900.copy(alpha = 0.62f)
    val PillBorder = White.copy(alpha = 0.18f)
}

@Preview(
    name = "Screenshot Picker Scroll Indicator Pill",
    showBackground = true,
    widthDp = 80,
    heightDp = 80,
)
@Composable
private fun ScreenshotPickerScrollIndicatorPillPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(White),
            contentAlignment = Alignment.Center,
        ) {
            ScreenshotPickerScrollIndicatorPill()
        }
    }
}
