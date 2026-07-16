package com.chalkak.recap.feature.settings

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography
import com.chalkak.recap.core.model.ImageAccessLevel

@Composable
internal fun SettingsNavRow(
    @StringRes titleResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        titleResId = titleResId,
        onClick = onClick,
        modifier = modifier,
        trailing = {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_24),
                contentDescription = null,
                modifier = Modifier.size(SettingsRowTokens.ChevronSize),
                tint = RecapGray300,
            )
        },
    )
}

@Composable
internal fun SettingsStatusRow(
    @StringRes titleResId: Int,
    photoAccessLevel: ImageAccessLevel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        titleResId = titleResId,
        onClick = onClick,
        modifier = modifier,
        descriptionResId = photoAccessLevel.toGuidanceResId(),
        trailing = {
            Text(
                text = stringResource(photoAccessLevel.toStatusResId()),
                style = RecapTypography.RecapHeading4,
                color = photoAccessLevel.toStatusColor(),
            )
        },
    )
}

@Composable
internal fun SettingsRow(
    @StringRes titleResId: Int,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes descriptionResId: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressAnimationSpec = tween<Float>(
        durationMillis = SettingsRowTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val colorAnimationSpec = tween<Color>(
        durationMillis = SettingsRowTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) SettingsRowTokens.PressedScale else 1f,
        animationSpec = pressAnimationSpec,
        label = "settings_row_press_scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) RecapGray50 else RecapBackground,
        animationSpec = colorAnimationSpec,
        label = "settings_row_container_color",
    )
    val rowShape = RoundedCornerShape(SettingsRowTokens.RowCornerRadius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsRowTokens.ClickAreaHorizontalPadding)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(rowShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = SettingsRowTokens.ContentInsetHorizontal,
                vertical = SettingsRowTokens.ClickAreaVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(SettingsRowTokens.DescriptionSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(titleResId),
                modifier = Modifier.weight(1f),
                style = RecapTypography.RecapBody1,
                color = RecapGray900,
            )
            trailing()
        }
        if (descriptionResId != null) {
            Text(
                text = stringResource(descriptionResId),
                style = RecapTypography.RecapCaption1,
                color = RecapGray300,
            )
        }
    }
}

@StringRes
private fun ImageAccessLevel.toStatusResId(): Int {
    return when (this) {
        ImageAccessLevel.Full -> R.string.settings_photo_access_allowed
        ImageAccessLevel.Selected -> R.string.settings_photo_access_partial
        ImageAccessLevel.Denied -> R.string.settings_photo_access_denied
    }
}

@StringRes
private fun ImageAccessLevel.toGuidanceResId(): Int? {
    return when (this) {
        ImageAccessLevel.Full -> null
        ImageAccessLevel.Selected -> R.string.settings_photo_access_partial_guidance
        ImageAccessLevel.Denied -> R.string.settings_photo_access_denied_guidance
    }
}

private fun ImageAccessLevel.toStatusColor(): Color {
    return when (this) {
        ImageAccessLevel.Full,
        ImageAccessLevel.Selected,
            -> RecapBlue500

        ImageAccessLevel.Denied -> RecapGray300
    }
}

internal object SettingsRowTokens {
    const val PressedScale = 0.9875f
    const val PressAnimationDurationMillis = 100
    val HorizontalPadding = 28.dp
    val ClickAreaHorizontalPadding = 16.dp
    val ContentItemSpacing = 23.dp
    val ClickAreaVerticalPadding = ContentItemSpacing / 2
    val ContentInsetHorizontal = HorizontalPadding - ClickAreaHorizontalPadding
    val DescriptionSpacing = 4.dp
    val SectionHeaderTopPadding = 32.dp
    val SectionHeaderBottomPadding = 25.dp - ClickAreaVerticalPadding
    val SectionContentBottomPadding = 32.dp - ClickAreaVerticalPadding
    val RowCornerRadius = 10.dp
    val ChevronSize = 16.dp
    val BottomSpacing = 24.dp
}

@Preview(name = "Settings Nav Row", showBackground = true, widthDp = 360)
@Composable
private fun SettingsNavRowPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsNavRow(
            titleResId = R.string.settings_item_privacy_guide,
            onClick = {},
        )
    }
}

@Preview(name = "Settings Status Row - Allowed", showBackground = true, widthDp = 360)
@Composable
private fun SettingsStatusRowAllowedPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsStatusRow(
            titleResId = R.string.settings_item_photo_access,
            photoAccessLevel = ImageAccessLevel.Full,
            onClick = {},
        )
    }
}

@Preview(name = "Settings Status Row - Partial", showBackground = true, widthDp = 360)
@Composable
private fun SettingsStatusRowPartialPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsStatusRow(
            titleResId = R.string.settings_item_photo_access,
            photoAccessLevel = ImageAccessLevel.Selected,
            onClick = {},
        )
    }
}

@Preview(name = "Settings Status Row - Denied", showBackground = true, widthDp = 360)
@Composable
private fun SettingsStatusRowDeniedPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsStatusRow(
            titleResId = R.string.settings_item_photo_access,
            photoAccessLevel = ImageAccessLevel.Denied,
            onClick = {},
        )
    }
}
