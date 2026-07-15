package com.chalkak.recap.feature.settings.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.feature.settings.NotificationSettingsAction
import com.chalkak.recap.feature.settings.NotificationSettingsUiState
import com.chalkak.recap.feature.settings.SettingsDetailScreenScaffold
import com.chalkak.recap.feature.settings.SettingsDetailTokens

@Composable
fun NotificationSettingsScreen(
    uiState: NotificationSettingsUiState,
    onAction: (NotificationSettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreenScaffold(
        titleResId = R.string.settings_notification_settings_title,
        onBackClick = { onAction(NotificationSettingsAction.NavigateBack) },
        bottomContent = {},
        modifier = modifier,
    ) {
        NotificationSection(
            labelResId = R.string.settings_notification_service_label,
            items = listOf(
                NotificationItemData(
                    titleResId = R.string.settings_notification_organize_complete_title,
                    descriptionResId = R.string.settings_notification_organize_complete_description,
                    checked = uiState.organizeCompleteEnabled,
                    onCheckedChange = {
                        onAction(
                            NotificationSettingsAction
                                .OrganizeCompleteEnabledChanged(it),
                        )
                    },
                ),
                NotificationItemData(
                    titleResId = R.string.settings_notification_review_required_title,
                    descriptionResId = R.string.settings_notification_review_required_description,
                    checked = uiState.reviewRequiredEnabled,
                    onCheckedChange = {
                        onAction(
                            NotificationSettingsAction
                                .ReviewRequiredEnabledChanged(it),
                        )
                    },
                ),
            ),
        )
        NotificationSection(
            labelResId = R.string.settings_notification_reminder_label,
            items = listOf(
                NotificationItemData(
                    titleResId = R.string.settings_notification_review_reminder_title,
                    descriptionResId = R.string.settings_notification_review_reminder_description,
                    badgeResId = R.string.settings_notification_badge_preparing,
                    checked = false,
                    enabled = false,
                    footerResId = R.string.settings_notification_review_reminder_footer,
                    onCheckedChange = {},
                ),
            ),
        )
        NotificationSection(
            labelResId = R.string.settings_notification_marketing_label,
            items = listOf(
                NotificationItemData(
                    titleResId = R.string.settings_notification_marketing_title,
                    descriptionResId = R.string.settings_notification_marketing_description,
                    badgeResId = R.string.settings_notification_badge_optional,
                    checked = uiState.marketingEnabled,
                    onCheckedChange = {
                        onAction(
                            NotificationSettingsAction.MarketingEnabledChanged(it),
                        )
                    },
                ),
            ),
        )
        Text(
            text = stringResource(R.string.settings_notification_settings_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationSection(
    @StringRes labelResId: Int,
    items: List<NotificationItemData>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(labelResId),
            style = MaterialTheme.typography.labelMedium,
            color = RecapGray300,
            fontWeight = FontWeight.Bold,
        )
        NotificationGroupCard(items = items)
    }
}

@Composable
private fun NotificationGroupCard(
    items: List<NotificationItemData>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDetailTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, RecapGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                NotificationItem(item = item)
                if (index != items.lastIndex) {
                    HorizontalDivider(color = RecapGray100)
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    item: NotificationItemData,
    modifier: Modifier = Modifier,
) {
    val titleColor = if (item.enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        RecapGray300
    }
    val descriptionColor = if (item.enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        RecapGray300
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(item.titleResId),
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                )
                if (item.badgeResId != null) {
                    NotificationBadge(labelResId = item.badgeResId)
                }
            }
            Text(
                text = stringResource(item.descriptionResId),
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor,
            )
            if (item.footerResId != null) {
                Text(
                    text = stringResource(item.footerResId),
                    style = MaterialTheme.typography.bodySmall,
                    color = RecapGray300,
                )
            }
        }
        Switch(
            checked = item.checked,
            onCheckedChange = item.onCheckedChange,
            enabled = item.enabled,
        )
    }
}

@Composable
private fun NotificationBadge(
    @StringRes labelResId: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = stringResource(labelResId),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

private data class NotificationItemData(
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val enabled: Boolean = true,
    @get:StringRes val badgeResId: Int? = null,
    @get:StringRes val footerResId: Int? = null,
)

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
