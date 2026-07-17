package com.chalkak.recap.core.design.component.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.White

/*
 * RecapButton parameter mini docs
 * - text: Button label. It is always visually centered in the button.
 * - compactText: Optional shorter label used when a fixed-start icon may collide with the full label.
 * - onClick/enabled: Click callback and disabled state.
 * - modifier: Caller-owned layout. Use Modifier.fillMaxWidth() for full-width buttons.
 * - size: Height, corner radius, padding, icon size, and default text style preset.
 * - colors: Enabled and disabled container/content colors, plus optional pressed colors.
 *   When pressed colors are omitted, the enabled colors remain unchanged while pressed.
 *   Pass RecapButtonColors, or a Color for container (content defaults to White).
 * - border: Optional outline drawn with the same shape.
 * - leadingIcon: Optional icon slot. The slot is constrained to the size preset's icon size.
 * - iconPlacement: Inline keeps icon+text centered together; FixedStart pins the icon at the left.
 * - fixedIconStartPadding: Left inset for FixedStart icons.
 * - pressedScale: Draw-phase touch feedback scale. Use 1f to disable shrink feedback.
 * - shadowElevation: Optional Surface shadow for raised action buttons.
 * - pressedShadowElevationScale: Shadow elevation multiplier while pressed.
 * - dynamicShadowColor: Uses the enabled/disabled container color for shadow when true.
 *   Set false to use the default black shadow color.
 * - textStyle/fontWeight: Label typography override.
 * - contentPadding: Horizontal content padding for inline/text-only layouts.
 * - interactionSource: Optional hoisted source for tests or coordinated interactions.
 */
@Composable
fun RecapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RecapButtonSize = RecapButtonSize.Large,
    colors: RecapButtonColors = RecapButtonDefaults.primaryColors(),
    border: BorderStroke? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    iconPlacement: RecapButtonIconPlacement = RecapButtonIconPlacement.Inline,
    compactText: String? = null,
    fixedIconStartPadding: Dp = RecapButtonDefaults.FixedIconStartPadding,
    pressedScale: Float = RecapButtonDefaults.PressedScale,
    shadowElevation: Dp = 0.dp,
    pressedShadowElevationScale: Float = RecapButtonDefaults.PressedShadowElevationScale,
    dynamicShadowColor: Boolean = true,
    shape: Shape = RoundedCornerShape(size.cornerRadius),
    textStyle: TextStyle = RecapButtonDefaults.textStyle(size),
    fontWeight: FontWeight? = null,
    contentPadding: PaddingValues = RecapButtonDefaults.contentPadding(size),
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by resolvedInteractionSource.collectIsPressedAsState()
    val pressAnimationSpec = tween<Float>(
        durationMillis = RecapButtonDefaults.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val elevationAnimationSpec = tween<Dp>(
        durationMillis = RecapButtonDefaults.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val colorAnimationSpec = tween<Color>(
        durationMillis = RecapButtonDefaults.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) pressedScale else 1f,
        animationSpec = pressAnimationSpec,
        label = "recap_button_press_scale",
    )
    val animatedShadowElevation by animateDpAsState(
        targetValue = if (enabled && isPressed) {
            shadowElevation * pressedShadowElevationScale
        } else {
            shadowElevation
        },
        animationSpec = elevationAnimationSpec,
        label = "recap_button_shadow_elevation",
    )
    val contentColor by animateColorAsState(
        targetValue = colors.contentColorFor(enabled, isPressed),
        animationSpec = colorAnimationSpec,
        label = "recap_button_content_color",
    )
    val containerColor by animateColorAsState(
        targetValue = colors.containerColorFor(enabled, isPressed),
        animationSpec = colorAnimationSpec,
        label = "recap_button_container_color",
    )
    val shadowColor = if (dynamicShadowColor) containerColor else Color.Black
    val resolvedTextStyle = textStyle.merge(TextStyle(fontWeight = fontWeight))

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(size.height)
            .shadow(
                elevation = animatedShadowElevation,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = 0.dp,
        interactionSource = resolvedInteractionSource,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            if (leadingIcon != null && iconPlacement == RecapButtonIconPlacement.FixedStart) {
                RecapButtonFixedStartContent(
                    text = text,
                    compactText = compactText,
                    size = size,
                    textStyle = resolvedTextStyle,
                    contentPadding = contentPadding,
                    fixedIconStartPadding = fixedIconStartPadding,
                    leadingIcon = leadingIcon,
                )
            } else {
                RecapButtonInlineContent(
                    text = text,
                    size = size,
                    textStyle = resolvedTextStyle,
                    contentPadding = contentPadding,
                    leadingIcon = leadingIcon,
                )
            }
        }
    }
}

@Composable
fun RecapButton(
    text: String,
    onClick: () -> Unit,
    colors: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: RecapButtonSize = RecapButtonSize.Large,
    contentColor: Color = White,
    border: BorderStroke? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    iconPlacement: RecapButtonIconPlacement = RecapButtonIconPlacement.Inline,
    compactText: String? = null,
    fixedIconStartPadding: Dp = RecapButtonDefaults.FixedIconStartPadding,
    pressedScale: Float = RecapButtonDefaults.PressedScale,
    shadowElevation: Dp = 0.dp,
    pressedShadowElevationScale: Float = RecapButtonDefaults.PressedShadowElevationScale,
    dynamicShadowColor: Boolean = true,
    shape: Shape = RoundedCornerShape(size.cornerRadius),
    textStyle: TextStyle = RecapButtonDefaults.textStyle(size),
    fontWeight: FontWeight? = null,
    contentPadding: PaddingValues = RecapButtonDefaults.contentPadding(size),
    interactionSource: MutableInteractionSource? = null,
) {
    RecapButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        colors = RecapButtonDefaults.colors(
            containerColor = colors,
            contentColor = contentColor,
        ),
        border = border,
        leadingIcon = leadingIcon,
        iconPlacement = iconPlacement,
        compactText = compactText,
        fixedIconStartPadding = fixedIconStartPadding,
        pressedScale = pressedScale,
        shadowElevation = shadowElevation,
        pressedShadowElevationScale = pressedShadowElevationScale,
        dynamicShadowColor = dynamicShadowColor,
        shape = shape,
        textStyle = textStyle,
        fontWeight = fontWeight,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}

@Immutable
data class RecapButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val pressedContainerColor: Color? = null,
    val pressedContentColor: Color? = null,
) {
    fun containerColorFor(enabled: Boolean, pressed: Boolean = false): Color = when {
        !enabled -> disabledContainerColor
        pressed -> pressedContainerColor ?: containerColor
        else -> containerColor
    }

    fun contentColorFor(enabled: Boolean, pressed: Boolean = false): Color = when {
        !enabled -> disabledContentColor
        pressed -> pressedContentColor ?: contentColor
        else -> contentColor
    }
}

enum class RecapButtonSize(
    val height: Dp,
    val minWidth: Dp,
    val cornerRadius: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val iconSpacing: Dp,
) {
    Large(
        height = 56.dp,
        minWidth = 64.dp,
        cornerRadius = 16.dp,
        horizontalPadding = 20.dp,
        iconSize = 24.dp,
        iconSpacing = 5.dp,
    ),
    Medium(
        height = 45.dp,
        minWidth = 155.dp,
        cornerRadius = 14.dp,
        horizontalPadding = 18.dp,
        iconSize = 24.dp,
        iconSpacing = 5.dp,
    ),
}

enum class RecapButtonIconPlacement {
    Inline,
    FixedStart,
}

object RecapButtonDefaults {
    const val PressedScale = 0.975f
    const val PressedShadowElevationScale = 0.75f
    const val PressAnimationDurationMillis = 100
    val FixedIconStartPadding = 28.dp

    fun colors(
        containerColor: Color,
        contentColor: Color = White,
        disabledContainerColor: Color = RecapGray100,
        disabledContentColor: Color = RecapGray300,
        pressedContainerColor: Color? = null,
        pressedContentColor: Color? = null,
    ): RecapButtonColors = RecapButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
        pressedContainerColor = pressedContainerColor,
        pressedContentColor = pressedContentColor,
    )

    @Composable
    fun primaryColors(): RecapButtonColors = colors(
        containerColor = RecapBlue300,
        contentColor = RecapGray100,
    )

    fun secondaryColors(): RecapButtonColors = colors(
        containerColor = RecapBlue50,
        contentColor = RecapBlue300,
    )

    fun neutralColors(): RecapButtonColors = colors(
        containerColor = RecapGray50,
        contentColor = RecapGray900,
    )

    fun destructiveColors(): RecapButtonColors = colors(
        containerColor = RecapError,
        contentColor = White,
    )

    @Composable
    fun textColors(): RecapButtonColors = RecapButtonColors(
        containerColor = Color.Transparent,
        contentColor = RecapGray500,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = RecapGray900.copy(alpha = 0.38f),
    )

    @Composable
    fun outlinedColors(): RecapButtonColors = RecapButtonColors(
        containerColor = White,
        contentColor = RecapBlue500,
        disabledContainerColor = White,
        disabledContentColor = RecapGray900.copy(alpha = 0.38f),
    )

    @Composable
    fun kakaoColors(): RecapButtonColors = RecapButtonColors(
        containerColor = Color(0xFFFEE500),
        contentColor = Color(0xD9000000),
        disabledContainerColor = RecapGray900.copy(alpha = 0.12f),
        disabledContentColor = RecapGray900.copy(alpha = 0.38f),
    )

    fun textStyle(@Suppress("UNUSED_PARAMETER") size: RecapButtonSize): TextStyle = RecapHeading3

    fun contentPadding(size: RecapButtonSize): PaddingValues =
        PaddingValues(horizontal = size.horizontalPadding)
}

@Composable
private fun RecapButtonInlineContent(
    text: String,
    size: RecapButtonSize,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    leadingIcon: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minWidth = size.minWidth, minHeight = size.height)
            .fillMaxHeight()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier.size(size.iconSize),
                contentAlignment = Alignment.Center,
            ) {
                leadingIcon()
            }
            Spacer(modifier = Modifier.width(size.iconSpacing))
        }
        Text(
            text = text,
            color = LocalContentColor.current,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecapButtonFixedStartContent(
    text: String,
    compactText: String?,
    size: RecapButtonSize,
    textStyle: TextStyle,
    contentPadding: PaddingValues,
    fixedIconStartPadding: Dp,
    leadingIcon: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .defaultMinSize(minWidth = size.minWidth, minHeight = size.height)
            .fillMaxHeight(),
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val fixedIconProtectedWidth = fixedIconStartPadding + size.iconSize + size.iconSpacing
        val textHorizontalPadding = maxOfDp(
            fixedIconProtectedWidth,
            contentPadding.calculateLeftPadding(layoutDirection),
            contentPadding.calculateRightPadding(layoutDirection),
        )
        val buttonText = if (compactText != null) {
            val textMeasurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val fullTextWidth = with(density) {
                textMeasurer.measure(text = text, style = textStyle).size.width.toDp()
            }
            val fullTextStart = (maxWidth - fullTextWidth) / 2

            if (fullTextStart < fixedIconProtectedWidth) compactText else text
        } else {
            text
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = fixedIconStartPadding)
                    .size(size.iconSize),
                contentAlignment = Alignment.Center,
            ) {
                leadingIcon()
            }
            Text(
                text = buttonText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = textHorizontalPadding),
                color = LocalContentColor.current,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun maxOfDp(first: Dp, second: Dp, third: Dp): Dp {
    var max = first
    if (second > max) max = second
    if (third > max) max = third
    return max
}

@Preview(name = "RecapButton variants", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun RecapButtonPreview() {
    RECAPTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecapButton(
                text = stringResource(R.string.home_empty_import_button),
                onClick = {},
                modifier = Modifier.width(188.dp),
                size = RecapButtonSize.Medium,
                colors = RecapButtonDefaults.secondaryColors(),
                textStyle = RecapHeading3,
            )
            RecapButton(
                text = stringResource(R.string.recap_button_preview_reload),
                onClick = {},
                modifier = Modifier.width(188.dp),
                size = RecapButtonSize.Medium,
                colors = RecapButtonDefaults.secondaryColors(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.recap_arrow_retry_24),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
            RecapButton(
                text = stringResource(R.string.recap_button_preview_label),
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            RecapButtonPressedPreview()
            RecapButton(
                text = stringResource(R.string.recap_button_preview_label),
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
            RecapButton(
                text = stringResource(R.string.recap_button_preview_kakao_login),
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = RecapButtonDefaults.kakaoColors(),
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.kakao_96px),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                iconPlacement = RecapButtonIconPlacement.Inline,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RecapButton(
                    text = stringResource(R.string.deletion_confirmation_cancel_button),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = RecapButtonDefaults.neutralColors(),
                )
                RecapButton(
                    text = stringResource(R.string.deletion_confirmation_delete_button),
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = RecapButtonDefaults.destructiveColors(),
                )
            }
        }
    }
}

@Composable
private fun RecapButtonPressedPreview() {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.emit(PressInteraction.Press(Offset.Zero))
    }
    RecapButton(
        text = stringResource(R.string.recap_button_preview_label),
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        colors = RecapButtonDefaults.colors(
            containerColor = RecapBlue300,
            contentColor = RecapGray100,
            pressedContainerColor = RecapBlue500,
        ),
        interactionSource = interactionSource,
    )
}
