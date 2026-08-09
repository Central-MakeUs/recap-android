package com.chalkak.recap.core.design.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigationevent.NavigationEvent
import kotlin.math.roundToInt

/**
 * Navigation3 push/pop 공통 모션 정책.
 *
 * predictive 제스처 scrub / cancel / commit 소유권은 공식 [androidx.navigation3.ui.NavDisplay]에
 * 있고, 이 객체는 공유 시각 [ContentTransform]만 제공한다.
 */
object RecapNavigationMotion {
    const val SlideDurationMillis = 350

    /**
     * 뒤 화면(배경) parallax 이동량.
     *
     * 일반 push/pop에서 앞 화면은 화면 폭 100%를 미끄러지고,
     * 뒤 화면은 그보다 적게([ParallaxFraction]만큼) 이동해 깊이감을 만든다.
     * 예: 0.20 → 뒤 화면이 폭의 20%만 옆으로 밀림.
     */
    const val ParallaxFraction = 0.30f

    private val committedSlideSpec = tween<IntOffset>(
        durationMillis = SlideDurationMillis,
        easing = FastOutSlowInEasing,
    )

    fun none(): ContentTransform =
        EnterTransition.None togetherWith ExitTransition.None

    private val committedFadeSpec = tween<Float>(
        durationMillis = SlideDurationMillis,
        easing = FastOutSlowInEasing,
    )

    /** Fullscreen overlay-style push/pop. Keeps underlying slide motion for other destinations. */
    fun fade(): ContentTransform =
        fadeIn(animationSpec = committedFadeSpec) togetherWith
            fadeOut(animationSpec = committedFadeSpec)

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

    /**
     * Predictive back transform.
     *
     * - Edge gesture (`EDGE_LEFT` / `EDGE_RIGHT`): [pop]과 동일한 full-range transform.
     * - 3버튼/하드웨어 back (`EDGE_NONE`): preview 없음.
     */
    fun predictivePop(@NavigationEvent.SwipeEdge swipeEdge: Int): ContentTransform =
        if (swipeEdge == NavigationEvent.EDGE_NONE) {
            none()
        } else {
            pop()
        }
}
