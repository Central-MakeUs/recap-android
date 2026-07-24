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
 * Navigation3 push/pop 공통 모션 정책.
 *
 * predictive 제스처 scrub / cancel / commit 소유권은 [RecapNavDisplay]에 있고,
 * 이 객체는 공유 시각 transform과 progress 리매핑 상수만 제공한다.
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

    /**
     * predictive back scrub 중 허용하는 최대 seek 비율.
     *
     * [pop] transform 전체(1.0) 중 이 비율까지만 preview로 보여 주고,
     * commit 시 그 지점에서 1.0까지 이어서 재생한다.
     * [ParallaxFraction]과 별개: parallax는 “뒤 화면이 얼마나 덜 움직이는지”,
     * 이 값은 “뒤로가기 preview가 pop을 얼마나 미리 보여줄지”다.
     */
    const val PredictiveMaxFraction = 0.35f

    private val committedSlideSpec = tween<IntOffset>(
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

    /**
     * predictive scrub용 transform. [pop]과 동일한 오프셋/타이밍을 쓰고,
     * 저항은 transform이 아니라 [RecapNavigationMotionOffsets.previewPopFraction]으로 만든다.
     */
    fun predictivePop(): ContentTransform = pop()
}

/**
 * predictive back scrub progress를 [RecapNavigationMotion.pop] seek fraction으로 리매핑한다.
 *
 * ease-out 곡선으로 초반에는 더 크게, 후반에는 [RecapNavigationMotion.PredictiveMaxFraction]
 * 근처에서 저항이 커지도록 매핑한다.
 */
object RecapNavigationMotionOffsets {
    fun previewPopFraction(gestureProgress: Float): Float {
        val progress = gestureProgress.coerceIn(0f, 1f)
        // ease-out 이차곡선: f(p) = 1 - (1 - p)^2
        val eased = 1f - (1f - progress) * (1f - progress)
        return eased * RecapNavigationMotion.PredictiveMaxFraction
    }
}
