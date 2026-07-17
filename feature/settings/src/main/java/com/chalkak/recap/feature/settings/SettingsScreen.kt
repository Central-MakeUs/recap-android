package com.chalkak.recap.feature.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.divider.RecapSectionDivider
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography
import com.chalkak.recap.core.model.ImageAccessLevel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState = SettingsUiState(),
    onAction: (SettingsAction) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_title),
                onBackClick = { onAction(SettingsAction.NavigateBack) },
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = RecapBackground,
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
                RecapSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_notification_permission) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_app_notification,
                        onClick = { onAction(SettingsAction.OpenNotificationSettings) },
                    )
                    SettingsStatusRow(
                        titleResId = R.string.settings_item_photo_access,
                        photoAccessLevel = uiState.photoAccessLevel,
                        onClick = { onAction(SettingsAction.OpenPhotoAccessPermission) },
                    )
                }
                RecapSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_data) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_data_management,
                        onClick = { onAction(SettingsAction.OpenDataManagement) },
                    )
                }
                RecapSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_guide) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_usage_guide,
                        onClick = { onAction(SettingsAction.OpenUsageGuide) },
                    )
                    SettingsNavRow(
                        titleResId = R.string.settings_item_privacy_guide,
                        onClick = { onAction(SettingsAction.OpenPrivacyGuide) },
                    )
                }
                RecapSectionDivider()
                SettingsSection(titleResId = R.string.settings_section_support) {
                    SettingsNavRow(
                        titleResId = R.string.settings_item_contact,
                        onClick = { onAction(SettingsAction.OpenContact) },
                    )
                    SettingsNavRow(
                        titleResId = R.string.settings_item_open_source_licenses,
                        onClick = { onAction(SettingsAction.OpenOpenSourceLicenses) },
                    )
                }
                Spacer(modifier = Modifier.height(SettingsRowTokens.BottomSpacing))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    @StringRes titleResId: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(titleResId),
            modifier = Modifier.padding(
                start = SettingsRowTokens.HorizontalPadding,
                end = SettingsRowTokens.HorizontalPadding,
                top = SettingsRowTokens.SectionHeaderTopPadding,
                bottom = SettingsRowTokens.SectionHeaderBottomPadding,
            ),
            style = RecapTypography.RecapBody2,
            color = RecapGray500,
        )
        Column(
            modifier = Modifier.padding(bottom = SettingsRowTokens.SectionContentBottomPadding),
            content = content,
        )
    }
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

@Preview(name = "Settings - Partial", showBackground = true, widthDp = 360)
@Composable
private fun SettingsPartialPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState(photoAccessLevel = ImageAccessLevel.Selected),
        )
    }
}
