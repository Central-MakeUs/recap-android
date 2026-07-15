package com.chalkak.recap.feature.settings.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.feature.settings.SettingsDetailScreenScaffold
import com.chalkak.recap.feature.settings.SettingsDetailTokens
import com.chalkak.recap.feature.settings.SettingsDocumentButton
import com.chalkak.recap.feature.settings.SettingsGuideCard

@Composable
fun PrivacyGuideScreen(
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsDetailScreenScaffold(
        titleResId = R.string.settings_privacy_guide_title,
        onBackClick = onBackClick,
        bottomContent = {
            SettingsDocumentButton(
                text = stringResource(R.string.settings_privacy_guide_policy_button),
                onClick = onPrivacyPolicyClick,
            )
            SettingsDocumentButton(
                text = stringResource(R.string.settings_privacy_guide_terms_button),
                onClick = onTermsClick,
            )
        },
        modifier = modifier,
    ) {
        SettingsGuideCard(
            icon = Icons.Outlined.CheckBox,
            titleResId = R.string.settings_privacy_guide_selected_only_title,
            descriptionResId = R.string.settings_privacy_guide_selected_only_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
        )
        SettingsGuideCard(
            icon = Icons.Outlined.ErrorOutline,
            titleResId = R.string.settings_privacy_guide_no_auto_filter_title,
            descriptionResId = R.string.settings_privacy_guide_no_auto_filter_description,
            iconTint = SettingsDetailTokens.WarningIconColor,
            iconContainerColor = SettingsDetailTokens.WarningContainerColor,
        )
        SettingsGuideCard(
            icon = Icons.Outlined.Cancel,
            titleResId = R.string.settings_privacy_guide_user_exclusion_title,
            descriptionResId = R.string.settings_privacy_guide_user_exclusion_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
        )
        SettingsGuideCard(
            icon = Icons.Outlined.CreditCard,
            titleResId = R.string.settings_privacy_guide_result_data_title,
            descriptionResId = R.string.settings_privacy_guide_result_data_description,
        )
    }
}

@Preview(name = "Privacy Guide", showBackground = true, widthDp = 360)
@Composable
private fun PrivacyGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        PrivacyGuideScreen(
            onBackClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}
