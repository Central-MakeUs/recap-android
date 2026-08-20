package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
 * Enter/exit None avoids frame crossfade blur. Detail/Edit suppress their raster while
 * the transition is active, leaving Fullscreen as the single raster owner.
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
 * Fades the Detail/Edit fullscreen chip while it rides the shared image frame
 * (apply [screenshotSharedImageBounds] on the chip's parent so BottomEnd tracks
 * the morphing bounds).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.screenshotFullscreenChipTransition(
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (animatedVisibilityScope == null) return this
    return with(animatedVisibilityScope) {
        this@screenshotFullscreenChipTransition.animateEnterExit(
            enter = fadeIn(),
            exit = fadeOut(),
        )
    }
}
