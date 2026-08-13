package com.chalkak.recap.feature.settings.account

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun AccountManagementScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        AccountManagementScreen(
            uiState = AccountManagementUiState(
                platform = LoginPlatform.Kakao,
                joinedDate = stringResource(R.string.settings_account_preview_joined_date),
            ),
            onAction = {},
        )
    }
}
