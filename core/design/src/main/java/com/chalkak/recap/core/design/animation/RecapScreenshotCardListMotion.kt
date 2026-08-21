package com.chalkak.recap.core.design.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

object RecapScreenshotCardListMotion {
    const val ItemRemovalDurationMillis = 220
}

context(scope: LazyItemScope)
fun Modifier.recapScreenshotCardItemAnimation(): Modifier {
    val fadeSpec = tween<Float>(
        durationMillis = RecapScreenshotCardListMotion.ItemRemovalDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val placementSpec = tween<IntOffset>(
        durationMillis = RecapScreenshotCardListMotion.ItemRemovalDurationMillis,
        easing = FastOutSlowInEasing,
    )
    return with(scope) {
        this@recapScreenshotCardItemAnimation.animateItem(
            fadeInSpec = null,
            fadeOutSpec = fadeSpec,
            placementSpec = placementSpec,
        )
    }
}
