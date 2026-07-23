package com.chalkak.recap.core.design.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * Shared Navigation3 push/pop motion policy.
 *
 * Navigation3 owns predictive gesture scrub, cancellation, commit completion, scene lifecycle,
 * and back stack mutation. This policy only supplies the visual transforms.
 */
object RecapNavigationMotion {
    const val SlideDurationMillis = 250
    const val ParallaxFraction = 0.20f

    private val committedSlideSpec = tween<IntOffset>(
        durationMillis = SlideDurationMillis,
        easing = FastOutSlowInEasing,
    )

    private val predictiveSlideSpec = tween<IntOffset>(
        durationMillis = SlideDurationMillis,
        easing = FastOutSlowInEasing,
    )

    fun none(): ContentTransform =
        EnterTransition.None togetherWith ExitTransition.None

    fun forward(): ContentTransform =
        slideInHorizontally(
            animationSpec = committedSlideSpec,
            initialOffsetX = { fullWidth -> fullWidth },
        ) togetherWith slideOutHorizontally(
            animationSpec = committedSlideSpec,
            targetOffsetX = { fullWidth ->
                (-fullWidth * ParallaxFraction).roundToInt()
            },
        )

    fun pop(): ContentTransform =
        slideInHorizontally(
            animationSpec = committedSlideSpec,
            initialOffsetX = { fullWidth ->
                (-fullWidth * ParallaxFraction).roundToInt()
            },
        ) togetherWith slideOutHorizontally(
            animationSpec = committedSlideSpec,
            targetOffsetX = { fullWidth -> fullWidth },
        )

    fun predictivePop(): ContentTransform =
        slideInHorizontally(
            animationSpec = predictiveSlideSpec,
            initialOffsetX = { fullWidth ->
                (-fullWidth * ParallaxFraction).roundToInt()
            },
        ) togetherWith slideOutHorizontally(
            animationSpec = predictiveSlideSpec,
            targetOffsetX = { fullWidth -> fullWidth },
        )
}
