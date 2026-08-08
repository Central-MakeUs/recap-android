package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal const val ScreenshotSharedImageKey = "screenshot-image"

/** Matches Detail/Fullscreen screenshot frame corner radius. */
internal val ScreenshotSharedImageCornerRadius = 10.dp

/**
 * Shared image frame for Detail/Edit ↔ Fullscreen.
 *
 * [RemeasureToBounds] remasures content into the animated bounds so Crop can fill
 * the frame throughout (sharedElement would uniformly scale Fullscreen's tall Fit
 * frame into Detail's 3:4 hole and look letterboxed).
 *
 * Enter/exit None avoids crossfade blur; both ends should use ContentScale.Crop
 * while [SharedTransitionScope.isTransitionActive] so the two layers match.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.screenshotSharedImageBounds(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    val shape = RoundedCornerShape(ScreenshotSharedImageCornerRadius)
    return with(sharedTransitionScope) {
        this@screenshotSharedImageBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = ScreenshotSharedImageKey),
            animatedVisibilityScope = animatedVisibilityScope,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            resizeMode = RemeasureToBounds,
            clipInOverlayDuringTransition = OverlayClip(shape),
        )
    }
}
