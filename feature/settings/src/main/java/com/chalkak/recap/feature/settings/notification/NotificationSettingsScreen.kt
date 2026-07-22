package com.chalkak.recap.feature.settings.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.card.RecapStatusActionCard
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3

@Composable
fun NotificationSettingsScreen(
    uiState: NotificationSettingsUiState,
    onAction: (NotificationSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val switchEnabled = uiState.deviceNotificationsEnabled
    val switchChecked = switchEnabled && uiState.organizeCompleteEnabled

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_notification_settings_title),
                onBackClick = { onAction(NotificationSettingsAction.NavigateBack) },
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = RecapBackground,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(23.dp),
            ) {
                if (!uiState.deviceNotificationsEnabled) {
                    RecapStatusActionCard(
                        iconResId = R.drawable.ic_no_notification_permission_24,
                        message = stringResource(R.string.settings_notification_device_off_message),
                        actionLabel = stringResource(R.string.settings_notification_device_off_action),
                        onActionClick = {
                            onAction(NotificationSettingsAction.OpenDeviceNotificationSettings)
                        },
                        iconContentDescription = stringResource(
                            R.string.notification_disabled_icon_content_description,
                        ),
                    )
                }
                OrganizeNotificationToggleRow(
                    checked = switchChecked,
                    enabled = switchEnabled,
                    onCheckedChange = {
                        onAction(NotificationSettingsAction.OrganizeCompleteEnabledChanged(it))
                    },
                )
            }
        }
    }
}

@Composable
private fun OrganizeNotificationToggleRow(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_notification_organize_complete_title),
                style = RecapHeading3,
                color = RecapGray900,
            )
            Text(
                text = stringResource(
                    R.string.settings_notification_organize_complete_description,
                ),
                style = RecapBody2,
                color = RecapGray300,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Preview(name = "Notification Settings", showBackground = true, widthDp = 360)
@Composable
private fun NotificationSettingsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(),
            onAction = {},
        )
    }
}

@Preview(
    name = "Notification Settings Device Off",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun NotificationSettingsScreenDeviceOffPreview() {
    RECAPTheme(dynamicColor = false) {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(deviceNotificationsEnabled = false),
            onAction = {},
        )
    }
}
