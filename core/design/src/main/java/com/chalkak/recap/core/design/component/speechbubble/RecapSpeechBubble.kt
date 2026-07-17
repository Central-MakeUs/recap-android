package com.chalkak.recap.core.design.component.speechbubble

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapTypography
import com.chalkak.recap.core.design.theme.White
import kotlinx.coroutines.delay


enum class RecapSpeechBubbleArrowDirection {
    Up,
    Down,
}

@Immutable
data class RecapSpeechBubbleColors(
    val container: Color,
    val border: Color,
    val content: Color,
)

object RecapSpeechBubbleDefaults {
    val HorizontalPadding = 20.dp
    val VerticalPadding = 8.dp
    val ArrowWidth = 14.dp
    val ArrowHeight = 8.dp
    val BorderWidth = 1.5.dp
    val Elevation = 6.dp
    val TextStyle: TextStyle = RecapTypography.RecapCaption1
    const val ArrowAnimationDurationMillis = 360
    const val TextChangeAnimationDurationMillis = 360

    fun colors(
        container: Color = White,
        border: Color = RecapBlue300,
        content: Color = RecapBlue300,
    ): RecapSpeechBubbleColors = RecapSpeechBubbleColors(
        container = container,
        border = border,
        content = content,
    )
}

@Composable
fun RecapSpeechBubble(
    text: String,
    arrowDirection: RecapSpeechBubbleArrowDirection,
    modifier: Modifier = Modifier,
    colors: RecapSpeechBubbleColors = RecapSpeechBubbleDefaults.colors(),
    textStyle: TextStyle = RecapSpeechBubbleDefaults.TextStyle,
    animationDurationMillis: Int = RecapSpeechBubbleDefaults.TextChangeAnimationDurationMillis,
) {
    val targetProgress = when (arrowDirection) {
        RecapSpeechBubbleArrowDirection.Up -> 0f
        RecapSpeechBubbleArrowDirection.Down -> 1f
    }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = animationDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "recapSpeechBubbleArrowProgress",
    )
    val (upFactor, downFactor) = arrowFactors(progress)
    val density = LocalDensity.current
    val shape = with(density) {
        val maxArrowHeightPx = RecapSpeechBubbleDefaults.ArrowHeight.toPx()
        SpeechBubbleShape(
            arrowWidthPx = RecapSpeechBubbleDefaults.ArrowWidth.toPx(),
            upArrowHeightPx = maxArrowHeightPx * upFactor,
            downArrowHeightPx = maxArrowHeightPx * downFactor,
            reservedArrowHeightPx = maxArrowHeightPx,
        )
    }
    // 꼬리 방향 애니메이션과 무관하게 상·하 패딩을 동일하게 유지해 레이아웃 크기를 고정한다.
    val contentPadding = speechBubbleContentPadding()
    val textChangeSpec = tween<Float>(
        durationMillis = animationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val textSizeChangeSpec = tween<IntSize>(
        durationMillis = animationDurationMillis,
        easing = FastOutSlowInEasing,
    )

    Box(
        modifier = modifier
            .wrapContentSize()
            .shadow(
                elevation = RecapSpeechBubbleDefaults.Elevation,
                shape = shape,
                clip = false,
            )
            .background(color = colors.container, shape = shape)
            .border(
                width = RecapSpeechBubbleDefaults.BorderWidth,
                color = colors.border,
                shape = shape,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                fadeIn(animationSpec = textChangeSpec) togetherWith
                    fadeOut(animationSpec = textChangeSpec) using
                    SizeTransform(clip = false) { _, _ -> textSizeChangeSpec }
            },
            label = "recapSpeechBubbleText",
        ) { targetText ->
            Text(
                text = targetText,
                style = textStyle,
                color = colors.content,
                textAlign = TextAlign.Center,
            )
        }
    }
}

internal fun arrowFactors(progress: Float): Pair<Float, Float> {
    val p = progress.coerceIn(0f, 1f)
    val upFactor = (1f - 1.5f * p).coerceIn(0f, 1f)
    val downFactor = (1.5f * p - 0.5f).coerceIn(0f, 1f)
    return upFactor to downFactor
}

private fun speechBubbleContentPadding(): PaddingValues {
    val arrowHeight = RecapSpeechBubbleDefaults.ArrowHeight
    return PaddingValues(
        start = RecapSpeechBubbleDefaults.HorizontalPadding,
        top = RecapSpeechBubbleDefaults.VerticalPadding + arrowHeight,
        end = RecapSpeechBubbleDefaults.HorizontalPadding,
        bottom = RecapSpeechBubbleDefaults.VerticalPadding + arrowHeight,
    )
}

private data class SpeechBubbleShape(
    private val arrowWidthPx: Float,
    private val upArrowHeightPx: Float,
    private val downArrowHeightPx: Float,
    private val reservedArrowHeightPx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Generic(
        createSpeechBubblePath(
            size = size,
            arrowWidth = arrowWidthPx,
            upArrowHeight = upArrowHeightPx,
            downArrowHeight = downArrowHeightPx,
            reservedArrowHeight = reservedArrowHeightPx,
        ),
    )
}

internal fun createSpeechBubblePath(
    size: Size,
    arrowWidth: Float,
    upArrowHeight: Float,
    downArrowHeight: Float,
    reservedArrowHeight: Float,
): Path {
    val width = size.width
    val height = size.height
    val inset = reservedArrowHeight.coerceAtLeast(0f)
    val bodyTop = inset
    val bodyBottom = (height - inset).coerceAtLeast(bodyTop)
    val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(0f)
    val radius = bodyHeight / 2f
    val arrowCenterX = width / 2f
    val arrowHalfWidth = arrowWidth / 2f
    val minArrowX = if (radius > 0f) radius else 0f
    val maxArrowX = if (radius > 0f) width - radius else width
    val arrowLeft = (arrowCenterX - arrowHalfWidth).coerceIn(minArrowX, maxArrowX)
    val arrowRight = (arrowCenterX + arrowHalfWidth).coerceIn(minArrowX, maxArrowX)
    val drawUpArrow = upArrowHeight > 0.5f
    val drawDownArrow = downArrowHeight > 0.5f
    val upTipY = bodyTop - upArrowHeight.coerceAtLeast(0f)
    val downTipY = bodyBottom + downArrowHeight.coerceAtLeast(0f)

    return Path().apply {
        moveTo(radius, bodyTop)
        if (drawUpArrow) {
            lineTo(arrowLeft, bodyTop)
            lineTo(arrowCenterX, upTipY)
            lineTo(arrowRight, bodyTop)
        }
        lineTo(width - radius, bodyTop)
        if (radius > 0f) {
            arcTo(
                rect = Rect(
                    left = width - 2 * radius,
                    top = bodyTop,
                    right = width,
                    bottom = bodyTop + 2 * radius,
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
        }
        lineTo(width, bodyBottom - radius)
        if (radius > 0f) {
            arcTo(
                rect = Rect(
                    left = width - 2 * radius,
                    top = bodyBottom - 2 * radius,
                    right = width,
                    bottom = bodyBottom,
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
        }
        if (drawDownArrow) {
            lineTo(arrowRight, bodyBottom)
            lineTo(arrowCenterX, downTipY)
            lineTo(arrowLeft, bodyBottom)
        }
        lineTo(radius, bodyBottom)
        if (radius > 0f) {
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = bodyBottom - 2 * radius,
                    right = 2 * radius,
                    bottom = bodyBottom,
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
        }
        lineTo(0f, bodyTop + radius)
        if (radius > 0f) {
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = bodyTop,
                    right = 2 * radius,
                    bottom = bodyTop + 2 * radius,
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
        }
        close()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F5F8)
@Composable
private fun RecapSpeechBubbleArrowUpPreview() {
    RECAPTheme {
        RecapSpeechBubble(
            text = PreviewSpeechBubbleText,
            arrowDirection = RecapSpeechBubbleArrowDirection.Up,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F5F8)
@Composable
private fun RecapSpeechBubbleArrowDownPreview() {
    RECAPTheme {
        RecapSpeechBubble(
            text = PreviewSpeechBubbleText,
            arrowDirection = RecapSpeechBubbleArrowDirection.Down,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F5F8)
@Composable
private fun RecapSpeechBubbleVariantsPreview() {
    RECAPTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecapSpeechBubble(
                text = PreviewSpeechBubbleText,
                arrowDirection = RecapSpeechBubbleArrowDirection.Up,
            )
            RecapSpeechBubble(
                text = PreviewSpeechBubbleText,
                arrowDirection = RecapSpeechBubbleArrowDirection.Down,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F5F8)
@Composable
private fun RecapSpeechBubbleArrowTogglePreview() {
    RECAPTheme {
        var direction by remember { mutableStateOf(RecapSpeechBubbleArrowDirection.Up) }
        var text by remember { mutableStateOf(PreviewSpeechBubbleText) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1_200)
                direction = when (direction) {
                    RecapSpeechBubbleArrowDirection.Up -> RecapSpeechBubbleArrowDirection.Down
                    RecapSpeechBubbleArrowDirection.Down -> RecapSpeechBubbleArrowDirection.Up
                }
                text = if (text == PreviewSpeechBubbleText) {
                    PreviewSpeechBubbleShortText
                } else {
                    PreviewSpeechBubbleText
                }
            }
        }
        Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            RecapSpeechBubble(
                text = text,
                arrowDirection = direction,
            )
        }
    }
}

private const val PreviewSpeechBubbleText =
    "이제 앨범에서 헤맬 필요 없이,\n바로 찾을 수 있어요!"

private const val PreviewSpeechBubbleShortText =
    "카카오로 시작해 보세요"
