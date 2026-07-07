package com.chalkak.recap.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chalkak.recap.core.design.R


val PretendardFontFamily = FontFamily(
    Font(R.font.pretendard_light, weight = FontWeight.Light), // 300
    Font(R.font.pretendard_regular, weight = FontWeight.Normal), // 400
    Font(R.font.pretendard_medium, weight = FontWeight.Medium), // 500
    Font(R.font.pretendard_semibold, weight = FontWeight.SemiBold), // 600
    Font(R.font.pretendard_bold, weight = FontWeight.Bold), // 700
)

val Typography = Typography(
    displayLarge = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 28
    ),
    displayMedium = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 28
    ),
    displaySmall = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 22
    ),
    headlineLarge = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 22
    ),
    headlineMedium = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 18
    ),
    headlineSmall = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 16
    ),
    titleLarge = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 22
    ),
    titleMedium = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 18
    ),
    titleSmall = recapTextStyle(
        weight = FontWeight.SemiBold,
        size = 16
    ),
    bodyLarge = recapTextStyle(
        weight = FontWeight.Medium,
        size = 15
    ),
    bodyMedium = recapTextStyle(
        weight = FontWeight.Normal,
        size = 14
    ),
    bodySmall = recapTextStyle(
        weight = FontWeight.Medium,
        size = 13
    ),
    labelLarge = recapTextStyle(
        weight = FontWeight.Normal,
        size = 14
    ),
    labelMedium = recapTextStyle(
        weight = FontWeight.Medium,
        size = 13
    ),
    labelSmall = recapTextStyle(
        weight = FontWeight.Medium,
        size = 12
    ),
).applyFontFamily(PretendardFontFamily)

private fun recapTextStyle(
    weight: FontWeight,
    size: Int,
): TextStyle {
    return TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * 1.4f).sp,
        letterSpacing = (size * -0.02f).sp
    )
}

fun Typography.applyFontFamily(fontFamily: FontFamily): Typography {
    return this.copy(
        displayLarge = this.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = this.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = this.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = this.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = this.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = this.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = this.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = this.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = this.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = this.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = this.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = this.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = this.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = this.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = this.labelSmall.copy(fontFamily = fontFamily)
    )
}
