package com.chalkak.recap.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.model.ImageAccessLevel

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

@Preview(name = "My Page Notification Settings", showBackground = true, widthDp = 360)
@Composable
private fun NotificationSettingsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        NotificationSettingsScreen(
            uiState = NotificationSettingsUiState(),
            onAction = {},
        )
    }
}

@Preview(name = "My Page Upload Guide", showBackground = true, widthDp = 360)
@Composable
private fun UploadGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        UploadGuideScreen(
            onBackClick = {},
            onOpenSettingsClick = {},
        )
    }
}

@Preview(name = "My Page Data Management", showBackground = true, widthDp = 360)
@Composable
private fun DataManagementScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            onBackClick = {},
            onAccountManagementClick = {},
        )
    }
}

@Preview(name = "My Page Privacy Guide", showBackground = true, widthDp = 360)
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

@Preview(name = "My Page Service Info", showBackground = true, widthDp = 360)
@Composable
private fun ServiceInfoScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ServiceInfoScreen(
            onBackClick = {},
            onContactClick = {},
            onNoticeClick = {},
            onTermsClick = {},
            onPrivacyPolicyClick = {},
            onOpenSourceLicenseClick = {},
        )
    }
}
