package com.chalkak.recap.core.design.component.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White

@Composable
fun RecapFullscreenIconButton(
    visible: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val fadeSpec = tween<Float>(
        durationMillis = RecapNavigationMotion.SlideDurationMillis,
        easing = FastOutSlowInEasing,
    )
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = fadeSpec),
        exit = fadeOut(animationSpec = fadeSpec),
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .size(RecapFullscreenIconButtonTokens.TouchTargetSize)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            val shape = RoundedCornerShape(RecapFullscreenIconButtonTokens.CornerRadius)
            Box(
                modifier = Modifier
                    .size(RecapFullscreenIconButtonTokens.ChipSize)
                    .background(color = White, shape = shape)
                    .border(
                        width = RecapFullscreenIconButtonTokens.BorderWidth,
                        color = RecapGray200,
                        shape = shape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fullscreen_24),
                    contentDescription = contentDescription,
                    tint = if (enabled) {
                        RecapGray900
                    } else {
                        RecapGray900.copy(
                            alpha = RecapFullscreenIconButtonTokens.DisabledAlpha,
                        )
                    },
                    modifier = Modifier.size(RecapFullscreenIconButtonTokens.IconSize),
                )
            }
        }
    }
}

@Preview(name = "Fullscreen Icon Button", showBackground = true)
@Composable
private fun RecapFullscreenIconButtonPreview() {
    RECAPTheme(dynamicColor = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecapFullscreenIconButton(
                visible = true,
                contentDescription = "Fullscreen",
                onClick = {},
            )
            RecapFullscreenIconButton(
                visible = true,
                contentDescription = "Fullscreen disabled",
                onClick = {},
                enabled = false,
            )
        }
    }
}

private object RecapFullscreenIconButtonTokens {
    val TouchTargetSize = 48.dp
    val ChipSize = 21.dp
    val IconSize = 13.5.dp
    val CornerRadius = 2.dp
    val BorderWidth = 0.5.dp
    const val DisabledAlpha = 0.38f
}
