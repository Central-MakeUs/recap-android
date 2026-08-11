package com.chalkak.recap.feature.settings.guide

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun PrivacyGuideScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        PrivacyGuideScreen(
            onBackClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}
