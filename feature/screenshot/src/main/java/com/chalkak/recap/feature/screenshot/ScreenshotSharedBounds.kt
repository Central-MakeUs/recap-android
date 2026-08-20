package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal const val ScreenshotSharedImageKey = "screenshot-image"

/** Matches Detail/Fullscreen screenshot frame corner radius. */
internal val ScreenshotSharedImageCornerRadius = 10.dp

/**
 * Geometry proxy for the Detail/Edit ↔ Fullscreen image frame.
 *
 * [RemeasureToBounds] remasures content into the animated bounds so Crop can fill
 * the frame throughout (sharedElement would uniformly scale Fullscreen's tall Fit
 * frame into Detail's 3:4 hole and look letterboxed).
 *
 * Enter/exit None avoids frame crossfade blur. Detail/Edit keep only this geometry
 * while Fullscreen owns the raster or the transition is active.
 * The frame's child owns rounded clipping. Leaving the overlay itself unclipped lets
 * Fullscreen's Fit-sized frame draw its zoom layer outside those fixed bounds while
 * predictive back unwinds the transform.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.screenshotSharedImageBounds(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    return with(sharedTransitionScope) {
        this@screenshotSharedImageBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = ScreenshotSharedImageKey),
            animatedVisibilityScope = animatedVisibilityScope,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            resizeMode = RemeasureToBounds,
        )
    }
}

/**
 * Fullscreen owns the raster after the route-level handoff completes. The active
 * fallback keeps Detail/Edit empty through predictive back, including its committed tail.
 */
internal fun shouldSuppressSharedImageContent(
    enableSharedImageBounds: Boolean,
    fullscreenOwnsSharedImageRaster: Boolean,
    isSharedTransitionActive: Boolean,
): Boolean = enableSharedImageBounds &&
        (fullscreenOwnsSharedImageRaster || isSharedTransitionActive)

internal fun nextFullscreenRasterHandoffCompleted(
    currentValue: Boolean,
    fullscreenIsTop: Boolean,
    isSharedTransitionActive: Boolean,
): Boolean = when {
    fullscreenIsTop && isSharedTransitionActive -> true
    !fullscreenIsTop && !isSharedTransitionActive -> false
    else -> currentValue
}

/**
 * Recreated composition with Fullscreen already on top never sees a shared
 * transition, so the latch must start completed. Pending enter still starts
 * false because [ScreenshotRoute] is first composed on Detail or Edit.
 */
internal fun initialFullscreenRasterHandoffCompleted(
    fullscreenIsTop: Boolean,
): Boolean = fullscreenIsTop
