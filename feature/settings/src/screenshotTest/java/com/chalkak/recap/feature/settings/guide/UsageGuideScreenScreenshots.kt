package com.chalkak.recap.feature.settings.guide

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun UsageGuideScreenScreenshot() {
    RECAPTheme(dynamicColor = false) {
        UsageGuideScreen(
            onBackClick = {},
            onShareFavoriteGuideClick = {},
        )
    }
}
