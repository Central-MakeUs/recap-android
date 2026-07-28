package com.chalkak.recap.core.design.component.speechbubble

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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1


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
    val HorizontalPadding = 25.dp
    val VerticalPadding = 10.dp
    val ArrowWidth = 14.dp
    val ArrowHeight = 8.dp
    val BorderWidth = 1.5.dp
    val Elevation = 1.dp
    val TextStyle: TextStyle = RecapCaption1

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
) {
    val density = LocalDensity.current
    val shape = with(density) {
        val arrowHeightPx = RecapSpeechBubbleDefaults.ArrowHeight.toPx()
        SpeechBubbleShape(
            arrowWidthPx = RecapSpeechBubbleDefaults.ArrowWidth.toPx(),
            upArrowHeightPx = if (arrowDirection == RecapSpeechBubbleArrowDirection.Up) {
                arrowHeightPx
            } else {
                0f
            },
            downArrowHeightPx = if (arrowDirection == RecapSpeechBubbleArrowDirection.Down) {
                arrowHeightPx
            } else {
                0f
            },
            reservedArrowHeightPx = arrowHeightPx,
        )
    }
    // 꼬리 방향과 무관하게 상·하 패딩을 동일하게 유지해 레이아웃 크기를 고정한다.
    val contentPadding = speechBubbleContentPadding()

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
        Text(
            text = text,
            style = textStyle,
            color = colors.content,
            textAlign = TextAlign.Center,
        )
    }
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

private const val PreviewSpeechBubbleText =
    "5초만에 시작하기"
