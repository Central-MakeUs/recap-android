package com.chalkak.recap.feature.settings.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun DataManagementScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            uiState = DataManagementUiState(
                organizedCount = 128,
                isAiDataTransferConsented = true,
                aiDataTransferConsentDate = stringResource(
                    R.string.settings_data_management_ai_consent_preview_date,
                ),
            ),
            onAction = {},
        )
    }
}
