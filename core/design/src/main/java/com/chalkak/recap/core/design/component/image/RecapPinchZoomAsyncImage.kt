package com.chalkak.recap.core.design.component.image

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil3.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 스크린샷 확대 뷰 공통 정책:
 * - 초기 표시는 가용 영역 대비 [contentPadding] 또는 [edgeInsetFraction] 여백을 둔 Fit
 * - 두 손가락 핀치/팬으로 [MinScale]~[MaxScale] 확대
 * - 팬은 뷰포트 각 가장자리에 [MaxEmptyEdgeFraction]을 넘는 빈 영역이 보이지 않게 제한한다
 * - 한 손가락 빠른 스와이프는 [MinFlingVelocity] 이상일 때 관성 감쇠로 이어진다
 * - 관성 중 클램프에 닿으면 그 축만 멈추고 다른 축은 벽을 따라 계속 감쇠한다
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
        val minFlingVelocityPx = with(density) {
            RecapPinchZoomImageTokens.MinFlingVelocity.toPx()
        }
        val flingDecay = remember {
            exponentialDecay<Float>(
                frictionMultiplier = RecapPinchZoomImageTokens.FlingFrictionMultiplier,
            )
        }

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
                    minFlingVelocityPx,
                ) {
                    coroutineScope {
                        val gestureScope = this
                        var flingJob: Job? = null
                        fun cancelFling() {
                            flingJob?.cancel()
                            flingJob = null
                        }

                        fun applyTransform(centroid: Offset, pan: Offset, zoom: Float) {
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

                        detectPinchZoomPanGestures(
                            onTouchDown = ::cancelFling,
                            onTransform = ::applyTransform,
                            onSinglePointerFling = { velocity ->
                                if (
                                    scale > RecapPinchZoomImageTokens.MinScale &&
                                    shouldStartPinchZoomFling(velocity, minFlingVelocityPx)
                                ) {
                                    val availableWidth = size.width - paddingLeftPx - paddingRightPx
                                    val availableHeight =
                                        size.height - paddingTopPx - paddingBottomPx
                                    val transformCenter = Offset(
                                        x = paddingLeftPx + availableWidth / 2f,
                                        y = paddingTopPx + availableHeight / 2f,
                                    )
                                    val (fitWidth, fitHeight) = pinchZoomFitSize(
                                        availableWidth = availableWidth,
                                        availableHeight = availableHeight,
                                        imageAspectRatio = imageAspectRatio,
                                    )
                                    val flingScale = scale
                                    flingJob = gestureScope.launch {
                                        animatePinchZoomFling(
                                            initialOffset = offset,
                                            velocity = velocity,
                                            decay = flingDecay,
                                            clamp = { candidate ->
                                                clampPinchZoomOffset(
                                                    offset = candidate,
                                                    visualWidth = fitWidth * flingScale,
                                                    visualHeight = fitHeight * flingScale,
                                                    viewportWidth = size.width.toFloat(),
                                                    viewportHeight = size.height.toFloat(),
                                                    restCenter = transformCenter,
                                                )
                                            },
                                            onOffset = { offset = it },
                                        )
                                    }
                                }
                            },
                        )
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

internal fun shouldStartPinchZoomFling(
    velocity: Offset,
    minSpeedPxPerSec: Float,
): Boolean = velocity.getDistance() >= minSpeedPxPerSec

internal suspend fun animatePinchZoomFling(
    initialOffset: Offset,
    velocity: Offset,
    decay: DecayAnimationSpec<Float>,
    clamp: (Offset) -> Offset,
    onOffset: (Offset) -> Unit,
) {
    coroutineScope {
        var currentX = initialOffset.x
        var currentY = initialOffset.y
        fun emit() {
            onOffset(Offset(currentX, currentY))
        }
        launch {
            AnimationState(
                initialValue = currentX,
                initialVelocity = velocity.x,
            ).animateDecay(decay) {
                val clampedX = clamp(Offset(value, currentY)).x
                currentX = clampedX
                emit()
                if (clampedX != value) {
                    cancelAnimation()
                }
            }
        }
        launch {
            AnimationState(
                initialValue = currentY,
                initialVelocity = velocity.y,
            ).animateDecay(decay) {
                val clampedY = clamp(Offset(currentX, value)).y
                currentY = clampedY
                emit()
                if (clampedY != value) {
                    cancelAnimation()
                }
            }
        }
    }
}

private suspend fun PointerInputScope.detectPinchZoomPanGestures(
    onTouchDown: () -> Unit,
    onTransform: (centroid: Offset, pan: Offset, zoom: Float) -> Unit,
    onSinglePointerFling: (velocity: Offset) -> Unit,
) {
    val velocityTracker = VelocityTracker()
    awaitEachGesture {
        velocityTracker.resetTracking()
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        var sawMultiPointer = false
        val touchSlop = viewConfiguration.touchSlop

        val down = awaitFirstDown(requireUnconsumed = false)
        onTouchDown()
        velocityTracker.addPosition(down.uptimeMillis, down.position)

        var canceled = false
        do {
            val event = awaitPointerEvent()
            if (event.changes.count { it.pressed } > 1) {
                sawMultiPointer = true
            }
            canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    pan += panChange
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val panMotion = pan.getDistance()
                    if (zoomMotion > touchSlop || panMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }
                event.trackPinchZoomVelocity(velocityTracker)
                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        onTransform(centroid, panChange, zoomChange)
                    }
                    event.changes.forEach { change ->
                        if (change.positionChanged()) {
                            change.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })

        if (!canceled && pastTouchSlop && !sawMultiPointer) {
            val maxVelocity = viewConfiguration.maximumFlingVelocity
            val velocity = velocityTracker.calculateVelocity(
                Velocity(maxVelocity, maxVelocity),
            )
            onSinglePointerFling(Offset(velocity.x, velocity.y))
        }
    }
}

private fun PointerEvent.trackPinchZoomVelocity(velocityTracker: VelocityTracker) {
    val currentCentroid = calculateCentroid(useCurrent = true)
    if (currentCentroid.isSpecified) {
        velocityTracker.addPosition(changes.maxOf { it.uptimeMillis }, currentCentroid)
        return
    }
    val released = changes.firstOrNull { it.previousPressed && !it.pressed } ?: return
    velocityTracker.addPosition(released.uptimeMillis, released.position)
}

object RecapPinchZoomImageTokens {
    const val EdgeInsetFraction = 0.1f
    const val MinScale = 1f
    const val MaxScale = 5f
    const val MaxEmptyEdgeFraction = 0.1f

    /** One-finger release faster than this (dp/s) starts a pan fling. */
    val MinFlingVelocity = 500.dp

    /** >1 stops sooner than Android's default fling; 1.2 keeps a short coast. */
    const val FlingFrictionMultiplier = 1.2f
}
