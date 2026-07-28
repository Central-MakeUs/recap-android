package com.chalkak.recap.core.design.component.chip

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption2

object RecapSortToggleDefaults {
    val Shape = RoundedCornerShape(size = 10.dp)
    val Height = 32.dp
    val HorizontalPadding = 10.dp
    val IconLabelSpacing = 10.dp
    val IconSize = 16.dp
    const val PressAnimationDurationMillis = 50
    const val LabelFadeDurationMillis = 150
}

@Composable
fun RecapSortToggle(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) RecapGray100 else RecapGray50,
        animationSpec = tween(
            durationMillis = RecapSortToggleDefaults.PressAnimationDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "RecapSortToggleContainerColor",
    )
    val labelStyle = RecapCaption2.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )
    val resolvedContentDescription = contentDescription
        ?: stringResource(R.string.collection_sort_toggle_content_description)

    Surface(
        modifier = modifier
            .semantics {
                this.contentDescription = resolvedContentDescription
                stateDescription = label
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = RecapSortToggleDefaults.Shape,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier
                .height(RecapSortToggleDefaults.Height)
                .padding(horizontal = RecapSortToggleDefaults.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                RecapSortToggleDefaults.IconLabelSpacing,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sort_filter_16),
                contentDescription = null,
                modifier = Modifier.size(RecapSortToggleDefaults.IconSize),
                tint = RecapGray500,
            )
            AnimatedContent(
                targetState = label,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = RecapSortToggleDefaults.LabelFadeDurationMillis,
                            delayMillis = RecapSortToggleDefaults.LabelFadeDurationMillis,
                        ),
                    ) togetherWith fadeOut(
                        animationSpec = tween(
                            durationMillis = RecapSortToggleDefaults.LabelFadeDurationMillis,
                        ),
                    ) using SizeTransform(clip = false)
                },
                label = "RecapSortToggleLabel",
            ) { targetLabel ->
                Text(
                    text = targetLabel,
                    style = labelStyle,
                    color = RecapGray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(name = "RecapSortToggle Latest", showBackground = true, backgroundColor = 0xFF4A4A4A)
@Composable
private fun RecapSortToggleLatestPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapSortToggle(
            label = stringResource(R.string.collection_sort_latest),
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "RecapSortToggle Oldest", showBackground = true, backgroundColor = 0xFF4A4A4A)
@Composable
private fun RecapSortToggleOldestPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapSortToggle(
            label = stringResource(R.string.collection_sort_oldest),
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
