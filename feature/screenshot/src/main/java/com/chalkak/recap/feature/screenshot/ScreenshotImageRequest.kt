package com.chalkak.recap.feature.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import coil3.request.ImageRequest
import coil3.size.Scale
import kotlin.math.sqrt

private const val ScreenshotDecodeRequestPixelBudget = 4_500_000L

/**
 * Keeps every Detail/Edit/Fullscreen request on one stable, window-bounded decode size.
 * Pinch zoom is a draw transform and must not change this request.
 */
@Composable
internal fun rememberBoundedScreenshotImageRequest(model: Any?): Any? {
    if (model == null) return null

    val context = LocalContext.current
    val containerSize = LocalWindowInfo.current.containerSize
    val requestSize = remember(containerSize) {
        boundedScreenshotDecodeSize(
            width = containerSize.width,
            height = containerSize.height,
        )
    }
    return remember(context, model, requestSize) {
        ImageRequest.Builder(context)
            .data(model)
            .size(requestSize.width, requestSize.height)
            .scale(Scale.FIT)
            .build()
    }
}

internal fun boundedScreenshotDecodeSize(
    width: Int,
    height: Int,
    pixelBudget: Long = ScreenshotDecodeRequestPixelBudget,
): IntSize {
    val safeWidth = width.coerceAtLeast(1)
    val safeHeight = height.coerceAtLeast(1)
    val pixels = safeWidth.toLong() * safeHeight
    if (pixels <= pixelBudget) return IntSize(safeWidth, safeHeight)

    val scale = sqrt(pixelBudget.toDouble() / pixels.toDouble())
    return IntSize(
        width = (safeWidth * scale).toInt().coerceAtLeast(1),
        height = (safeHeight * scale).toInt().coerceAtLeast(1),
    )
}
