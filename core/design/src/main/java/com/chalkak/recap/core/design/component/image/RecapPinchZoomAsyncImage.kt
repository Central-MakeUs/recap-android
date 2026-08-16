package com.chalkak.recap.core.design.component.image

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage

/**
 * 스크린샷 확대 뷰 공통 정책:
 * - 초기 표시는 가용 영역 대비 [contentPadding] 또는 [edgeInsetFraction] 여백을 둔 Fit
 * - 두 손가락 핀치/팬으로 [MinScale]~[MaxScale] 확대
 * - 팬은 뷰포트 각 가장자리에 [MaxEmptyEdgeFraction]을 넘는 빈 영역이 보이지 않게 제한한다
 * - 확대는 Fit 프레임(clip/border/shadow) 전체를 스케일해 bound가 같이 커진다
 *
 * Modifier order for shared transitions (docs):
 * graphicsLayer → dropShadow → size → [imageFrameModifier] → clip → border.
 * [dropShadow] stays outside shared bounds so it does not inflate measured frame.
 * [graphicsLayer] stays outside clip/sharedBounds so the rounded frame grows with pinch.
 *
 * [expandLayoutToZoom] bakes pan/scale into layout so [imageFrameModifier] (sharedBounds)
 * reports the visual zoomed rect, including corners that sit off-screen.
 */
@Composable
fun RecapPinchZoomAsyncImage(
    model: Any,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentPaddingTop: Dp = 0.dp,
    edgeInsetFraction: Float = RecapPinchZoomImageTokens.EdgeInsetFraction,
    contentPadding: PaddingValues? = null,
    shape: Shape? = null,
    borderWidth: Dp = Dp.Unspecified,
    borderColor: Color = Color.Unspecified,
    dropShadow: Shadow? = null,
    /** Applied after size constraints and before clip/border (e.g. sharedBounds). */
    imageFrameModifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    /**
     * When true, measured frame size/offset match the pinch visual so sharedBounds
     * can morph from the zoomed rect. Pinch state is not reset.
     */
    expandLayoutToZoom: Boolean = false,
    onTap: (() -> Unit)? = null,
    onError: (() -> Unit)? = null,
) {
    var scale by remember(model) {
        mutableFloatStateOf(RecapPinchZoomImageTokens.MinScale)
    }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    var imageAspectRatio by remember(model) { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(top = contentPaddingTop),
        contentAlignment = Alignment.Center,
    ) {
        val resolvedPadding = contentPadding ?: PaddingValues(
            horizontal = maxWidth * edgeInsetFraction,
            vertical = maxHeight * edgeInsetFraction,
        )
        val paddingLeftPx = with(density) {
            resolvedPadding.calculateLeftPadding(layoutDirection).toPx()
        }
        val paddingTopPx = with(density) { resolvedPadding.calculateTopPadding().toPx() }
        val paddingRightPx = with(density) {
            resolvedPadding.calculateRightPadding(layoutDirection).toPx()
        }
        val paddingBottomPx = with(density) { resolvedPadding.calculateBottomPadding().toPx() }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    model,
                    imageAspectRatio,
                    paddingLeftPx,
                    paddingTopPx,
                    paddingRightPx,
                    paddingBottomPx,
                ) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(
                            RecapPinchZoomImageTokens.MinScale,
                            RecapPinchZoomImageTokens.MaxScale,
                        )
                        val availableWidth = size.width - paddingLeftPx - paddingRightPx
                        val availableHeight = size.height - paddingTopPx - paddingBottomPx
                        val transformCenter = Offset(
                            x = paddingLeftPx + availableWidth / 2f,
                            y = paddingTopPx + availableHeight / 2f,
                        )
                        val (fitWidth, fitHeight) = pinchZoomFitSize(
                            availableWidth = availableWidth,
                            availableHeight = availableHeight,
                            imageAspectRatio = imageAspectRatio,
                        )
                        offset = clampPinchZoomOffset(
                            offset = pinchZoomOffset(
                                currentOffset = offset,
                                currentScale = scale,
                                newScale = newScale,
                                centroid = centroid,
                                pan = pan,
                                transformCenter = transformCenter,
                            ),
                            visualWidth = fitWidth * newScale,
                            visualHeight = fitHeight * newScale,
                            viewportWidth = size.width.toFloat(),
                            viewportHeight = size.height.toFloat(),
                            restCenter = transformCenter,
                        )
                        scale = newScale
                    }
                }
                .then(
                    if (onTap != null) {
                        Modifier.pointerInput(onTap) {
                            detectTapGestures(onTap = { onTap() })
                        }
                    } else {
                        Modifier
                    },
                )
                .padding(resolvedPadding),
            contentAlignment = Alignment.Center,
        ) {
            val layoutScale = if (expandLayoutToZoom) scale else RecapPinchZoomImageTokens.MinScale
            val layerScale = if (expandLayoutToZoom) RecapPinchZoomImageTokens.MinScale else scale
            val imageSizeModifier = if (imageAspectRatio > 0f && maxWidth > 0.dp && maxHeight > 0.dp) {
                val availableAspect = maxWidth / maxHeight
                val fitWidth: Dp
                val fitHeight: Dp
                if (imageAspectRatio > availableAspect) {
                    fitWidth = maxWidth
                    fitHeight = maxWidth / imageAspectRatio
                } else {
                    fitHeight = maxHeight
                    fitWidth = maxHeight * imageAspectRatio
                }
                if (expandLayoutToZoom) {
                    Modifier
                        .requiredWidth(fitWidth * layoutScale)
                        .requiredHeight(fitHeight * layoutScale)
                } else {
                    Modifier
                        .width(fitWidth)
                        .height(fitHeight)
                }
            } else {
                Modifier.fillMaxSize()
            }

            val clipShape = shape ?: RectangleShape
            val hasBorder = borderWidth != Dp.Unspecified && borderColor != Color.Unspecified

            Box(
                modifier = Modifier
                    .offset {
                        if (expandLayoutToZoom) {
                            offset.round()
                        } else {
                            IntOffset.Zero
                        }
                    }
                    .graphicsLayer {
                        scaleX = layerScale
                        scaleY = layerScale
                        if (expandLayoutToZoom) {
                            translationX = 0f
                            translationY = 0f
                        } else {
                            translationX = offset.x
                            translationY = offset.y
                        }
                    }
                    .then(
                        if (dropShadow != null && shape != null) {
                            Modifier.dropShadow(shape = clipShape, shadow = dropShadow)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Box(
                    modifier = imageSizeModifier
                        .then(imageFrameModifier)
                        .then(if (shape != null) Modifier.clip(clipShape) else Modifier)
                        .then(
                            if (hasBorder && shape != null) {
                                Modifier.border(
                                    width = borderWidth,
                                    color = borderColor,
                                    shape = clipShape,
                                )
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    AsyncImage(
                        model = model,
                        contentDescription = contentDescription,
                        contentScale = contentScale,
                        onSuccess = { state ->
                            val size = state.painter.intrinsicSize
                            if (size.isSpecified && size.height > 0f) {
                                imageAspectRatio = size.width / size.height
                            }
                        },
                        onError = if (onError != null) {
                            { onError() }
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

internal fun pinchZoomFitSize(
    availableWidth: Float,
    availableHeight: Float,
    imageAspectRatio: Float,
): Pair<Float, Float> {
    if (imageAspectRatio <= 0f || availableWidth <= 0f || availableHeight <= 0f) {
        return availableWidth to availableHeight
    }
    val availableAspect = availableWidth / availableHeight
    return if (imageAspectRatio > availableAspect) {
        availableWidth to availableWidth / imageAspectRatio
    } else {
        availableHeight * imageAspectRatio to availableHeight
    }
}

internal fun pinchZoomOffset(
    currentOffset: Offset,
    currentScale: Float,
    newScale: Float,
    centroid: Offset,
    pan: Offset,
    transformCenter: Offset,
): Offset {
    if (newScale <= RecapPinchZoomImageTokens.MinScale) return Offset.Zero
    val scaleChange = newScale / currentScale
    return currentOffset * scaleChange +
            (centroid - transformCenter) * (1f - scaleChange) +
            pan
}

/**
 * Keeps pan from revealing more than [maxEmptyFraction] of the viewport beyond
 * any image edge. If the visual size is too small to satisfy both edges, the
 * rest (Fit-centered) position is kept on that axis.
 */
internal fun clampPinchZoomOffset(
    offset: Offset,
    visualWidth: Float,
    visualHeight: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    restCenter: Offset,
    maxEmptyFraction: Float = RecapPinchZoomImageTokens.MaxEmptyEdgeFraction,
): Offset {
    return Offset(
        x = clampPinchZoomAxis(
            offset = offset.x,
            visualSize = visualWidth,
            viewportSize = viewportWidth,
            restCenter = restCenter.x,
            maxEmptyFraction = maxEmptyFraction,
        ),
        y = clampPinchZoomAxis(
            offset = offset.y,
            visualSize = visualHeight,
            viewportSize = viewportHeight,
            restCenter = restCenter.y,
            maxEmptyFraction = maxEmptyFraction,
        ),
    )
}

private fun clampPinchZoomAxis(
    offset: Float,
    visualSize: Float,
    viewportSize: Float,
    restCenter: Float,
    maxEmptyFraction: Float,
): Float {
    if (viewportSize <= 0f || visualSize <= 0f) return 0f
    val maxEmpty = viewportSize * maxEmptyFraction
    val restStart = restCenter - visualSize / 2f
    val restEnd = restCenter + visualSize / 2f
    val maxOffset = maxEmpty - restStart
    val minOffset = (viewportSize - maxEmpty) - restEnd
    if (minOffset > maxOffset) return 0f
    return offset.coerceIn(minOffset, maxOffset)
}

object RecapPinchZoomImageTokens {
    const val EdgeInsetFraction = 0.1f
    const val MinScale = 1f
    const val MaxScale = 5f
    const val MaxEmptyEdgeFraction = 0.1f
}
