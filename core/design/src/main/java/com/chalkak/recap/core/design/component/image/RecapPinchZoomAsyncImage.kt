package com.chalkak.recap.core.design.component.image

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * 스크린샷 확대 뷰 공통 정책:
 * - 초기 표시는 가용 영역 대비 [contentPadding] 또는 [edgeInsetFraction] 여백을 둔 Fit
 * - 두 손가락 핀치/팬으로 [MinScale]~[MaxScale] 확대
 *
 * Modifier order for shared transitions (docs): size → [imageFrameModifier] → clip → border.
 * [dropShadow] stays on an outer wrapper so it does not inflate shared bounds.
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
    onTap: (() -> Unit)? = null,
    onError: (() -> Unit)? = null,
) {
    var scale by remember(model) { mutableFloatStateOf(RecapPinchZoomImageTokens.MinScale) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    var imageAspectRatio by remember(model) { mutableFloatStateOf(0f) }

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

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(resolvedPadding),
            contentAlignment = Alignment.Center,
        ) {
            val imageSizeModifier = if (imageAspectRatio > 0f && maxWidth > 0.dp && maxHeight > 0.dp) {
                val availableAspect = maxWidth / maxHeight
                if (imageAspectRatio > availableAspect) {
                    Modifier
                        .width(maxWidth)
                        .aspectRatio(imageAspectRatio)
                } else {
                    Modifier
                        .height(maxHeight)
                        .aspectRatio(imageAspectRatio)
                }
            } else {
                Modifier.fillMaxSize()
            }

            val clipShape = shape ?: RectangleShape
            val hasBorder = borderWidth != Dp.Unspecified && borderColor != Color.Unspecified

            // dropShadow outside shared bounds so it does not inflate measured frame.
            Box(
                modifier = Modifier.then(
                    if (dropShadow != null && shape != null) {
                        Modifier.dropShadow(shape = clipShape, shadow = dropShadow)
                    } else {
                        Modifier
                    },
                ),
            ) {
                // Docs order: size → sharedBounds → clip → border → content
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
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
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
                        .pointerInput(model) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(
                                    RecapPinchZoomImageTokens.MinScale,
                                    RecapPinchZoomImageTokens.MaxScale,
                                )
                                scale = newScale
                                if (newScale > RecapPinchZoomImageTokens.MinScale) {
                                    offset += pan
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
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

object RecapPinchZoomImageTokens {
    const val EdgeInsetFraction = 0.1f
    const val MinScale = 1f
    const val MaxScale = 4f
}
