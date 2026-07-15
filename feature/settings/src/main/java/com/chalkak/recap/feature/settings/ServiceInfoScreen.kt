package com.chalkak.recap.feature.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RecapGray300

@Composable
fun ServiceInfoScreen(
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onOpenSourceLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreenScaffold(
        titleResId = R.string.settings_service_info_title,
        onBackClick = onBackClick,
        bottomContent = {
            Text(
                text = stringResource(R.string.settings_service_info_copyright),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = RecapGray300,
            )
        },
        modifier = modifier,
    ) {
        SettingsServiceSummaryCard()
        SettingsServiceMenuGroup(
            items = listOf(
                SettingsServiceMenuItemData(
                    titleResId = R.string.settings_service_info_contact_title,
                    descriptionResId = R.string.settings_service_info_contact_description,
                    onClick = onContactClick,
                ),
                SettingsServiceMenuItemData(
                    titleResId = R.string.settings_service_info_notice_title,
                    descriptionResId = R.string.settings_service_info_notice_description,
                    onClick = onNoticeClick,
                ),
                SettingsServiceMenuItemData(
                    titleResId = R.string.settings_service_info_terms_title,
                    descriptionResId = R.string.settings_service_info_terms_description,
                    onClick = onTermsClick,
                ),
                SettingsServiceMenuItemData(
                    titleResId = R.string.settings_service_info_privacy_title,
                    descriptionResId = R.string.settings_service_info_privacy_description,
                    onClick = onPrivacyPolicyClick,
                ),
                SettingsServiceMenuItemData(
                    titleResId = R.string.settings_service_info_open_source_title,
                    descriptionResId = R.string.settings_service_info_open_source_description,
                    onClick = onOpenSourceLicenseClick,
                ),
            ),
        )
    }
}
