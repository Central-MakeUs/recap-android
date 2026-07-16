package com.chalkak.recap.core.design.component.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4

@Composable
fun RecapStatusActionCard(
    @DrawableRes iconResId: Int,
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContentDescription: String? = null,
    containerColor: Color = RecapGray50,
    messageColor: Color = RecapGray900,
    iconTint: Color = RecapBlue300,
    actionColor: Color = RecapBlue300,
) {
    val contentDescription = "$message, $actionLabel"
    Surface(
        onClick = onActionClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(RecapStatusActionCardTokens.CornerRadius),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RecapStatusActionCardTokens.HorizontalPadding,
                vertical = RecapStatusActionCardTokens.VerticalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(
                RecapStatusActionCardTokens.ContentSpacing,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = iconContentDescription,
                modifier = Modifier.size(RecapStatusActionCardTokens.IconSize),
                tint = iconTint,
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = RecapHeading4,
                color = messageColor,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = actionLabel,
                    style = RecapHeading4,
                    color = actionColor,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right_24),
                    contentDescription = null,
                    modifier = Modifier.size(RecapStatusActionCardTokens.ActionChevronSize),
                    tint = actionColor,
                )
            }
        }
    }
}

private object RecapStatusActionCardTokens {
    val CornerRadius = 14.dp
    val HorizontalPadding = 16.dp
    val VerticalPadding = 14.dp
    val ContentSpacing = 10.dp
    val IconSize = 24.dp
    val ActionChevronSize = 16.dp
}

@Preview(name = "Status Action Card", showBackground = true, widthDp = 360)
@Composable
private fun RecapStatusActionCardPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapStatusActionCard(
            iconResId = R.drawable.ic_no_notification_permission_24,
            message = stringResource(R.string.settings_notification_device_off_message),
            actionLabel = stringResource(R.string.settings_notification_device_off_action),
            onActionClick = {},
            iconContentDescription = stringResource(
                R.string.notification_disabled_icon_content_description,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
