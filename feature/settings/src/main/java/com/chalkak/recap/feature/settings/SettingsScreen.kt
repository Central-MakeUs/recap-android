package com.chalkak.recap.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.ImageAccessLevel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState = SettingsUiState(),
    onAction: (SettingsAction) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = White,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_title),
                onBackClick = { onAction(SettingsAction.NavigateBack) },
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = White,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                SettingsSection(titleResId = R.string.settings_section_account) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_account_management,
                        onClick = { onAction(SettingsAction.OpenAccountManagement) },
                    )
                }
                SettingsSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_notification_permission) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_app_notification,
                        onClick = { onAction(SettingsAction.OpenNotificationSettings) },
                    )
                    SettingsRowDivider()
                    SettingsStatusRow(
                        titleResId = R.string.settings_item_photo_access,
                        statusResId = uiState.photoAccessLevel.toStatusResId(),
                        onClick = { onAction(SettingsAction.OpenPhotoAccessPermission) },
                    )
                }
                SettingsSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_data) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_data_management,
                        onClick = { onAction(SettingsAction.OpenDataManagement) },
                    )
                }
                SettingsSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_guide) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_usage_guide,
                        onClick = { onAction(SettingsAction.OpenUsageGuide) },
                    )
                    SettingsRowDivider()
                    SettingsNavRow(
                        titleResId = R.string.settings_item_privacy_guide,
                        onClick = { onAction(SettingsAction.OpenPrivacyGuide) },
                    )
                }
                SettingsSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_support) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_contact,
                        onClick = { onAction(SettingsAction.OpenContact) },
                    )
                    SettingsRowDivider()
                    SettingsNavRow(
                        titleResId = R.string.settings_item_open_source_licenses,
                        onClick = { onAction(SettingsAction.OpenOpenSourceLicenses) },
                    )
                }
                Spacer(modifier = Modifier.height(SettingsTokens.BottomSpacing))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    @StringRes titleResId: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(titleResId),
            modifier = Modifier.padding(
                start = SettingsTokens.HorizontalPadding,
                end = SettingsTokens.HorizontalPadding,
                top = SettingsTokens.SectionHeaderTopPadding,
                bottom = SettingsTokens.SectionHeaderBottomPadding,
            ),
            style = RecapTypography.RecapCaption2,
            color = RecapGray300,
        )
        content()
    }
}

@Composable
private fun SettingsNavRow(
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
                modifier = Modifier.size(SettingsTokens.ChevronSize),
                tint = RecapGray100,
            )
        },
    )
}

@Composable
private fun SettingsStatusRow(
    @StringRes titleResId: Int,
    @StringRes statusResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsRow(
        titleResId = titleResId,
        onClick = onClick,
        modifier = modifier,
        trailing = {
            Text(
                text = stringResource(statusResId),
                style = RecapTypography.RecapBody2,
                color = RecapBlue500,
            )
        },
    )
}

@Composable
private fun SettingsRow(
    @StringRes titleResId: Int,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = SettingsTokens.HorizontalPadding,
                vertical = SettingsTokens.RowVerticalPadding,
            ),
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
}

@Composable
private fun SettingsRowDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = SettingsTokens.HorizontalPadding),
        thickness = 0.5.dp,
        color = RecapGray100,
    )
}

@Composable
private fun SettingsSectionDivider(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(SettingsTokens.SectionDividerHeight)
            .background(RecapGray50),
    )
}

@StringRes
private fun ImageAccessLevel.toStatusResId(): Int {
    return when (this) {
        ImageAccessLevel.Full -> R.string.settings_photo_access_allowed
        ImageAccessLevel.Selected -> R.string.settings_photo_access_partial
        ImageAccessLevel.Denied -> R.string.settings_photo_access_denied
    }
}

private object SettingsTokens {
    val HorizontalPadding = 20.dp
    val SectionHeaderTopPadding = 16.dp
    val SectionHeaderBottomPadding = 8.dp
    val RowVerticalPadding = 16.dp
    val SectionDividerHeight = 8.dp
    val ChevronSize = 20.dp
    val BottomSpacing = 24.dp
}

@Preview(name = "Settings - Allowed", showBackground = true, widthDp = 360)
@Composable
private fun SettingsAllowedPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(photoAccessLevel = ImageAccessLevel.Full),
        )
    }
}

@Preview(name = "Settings - Denied", showBackground = true, widthDp = 360)
@Composable
private fun SettingsDeniedPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(photoAccessLevel = ImageAccessLevel.Denied),
        )
    }
}
