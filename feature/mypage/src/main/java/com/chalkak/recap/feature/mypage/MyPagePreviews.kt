package com.chalkak.recap.feature.mypage

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
private fun MyPageNotificationSettingsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        MyPageNotificationSettingsScreen(
            uiState = MyPageNotificationSettingsUiState(),
            onAction = {},
        )
    }
}

@Preview(name = "My Page Upload Guide", showBackground = true, widthDp = 360)
@Composable
private fun MyPageUploadGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        MyPageUploadGuideScreen(
            onBackClick = {},
            onOpenSettingsClick = {},
        )
    }
}

@Preview(name = "My Page Data Management", showBackground = true, widthDp = 360)
@Composable
private fun MyPageDataManagementScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        MyPageDataManagementScreen(
            onBackClick = {},
            onAccountManagementClick = {},
        )
    }
}

@Preview(name = "My Page Privacy Guide", showBackground = true, widthDp = 360)
@Composable
private fun MyPagePrivacyGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        MyPagePrivacyGuideScreen(
            onBackClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}

@Preview(name = "My Page Service Info", showBackground = true, widthDp = 360)
@Composable
private fun MyPageServiceInfoScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        MyPageServiceInfoScreen(
            onBackClick = {},
            onContactClick = {},
            onNoticeClick = {},
            onTermsClick = {},
            onPrivacyPolicyClick = {},
            onOpenSourceLicenseClick = {},
        )
    }
}
