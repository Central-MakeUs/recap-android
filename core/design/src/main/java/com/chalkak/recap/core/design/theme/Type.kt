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

/**
 * RECAP 디자인 타이포 토큰.
 * Figma: Heading 1–4 / Body 1–2 / Caption 1–3
 * 공통: lineHeight 140%, letterSpacing -2%
 *
 * 사용: `style = RecapTypography.RecapBody1`
 */
object RecapTypography {
    val RecapHeading1 = recapTextStyle(weight = FontWeight.SemiBold, size = 22)
    val RecapHeading2 = recapTextStyle(weight = FontWeight.SemiBold, size = 18)
    val RecapHeading3 = recapTextStyle(weight = FontWeight.SemiBold, size = 16)
    val RecapHeading4 = recapTextStyle(weight = FontWeight.SemiBold, size = 14)

    val RecapBody1 = recapTextStyle(weight = FontWeight.Medium, size = 15)
    val RecapBody2 = recapTextStyle(weight = FontWeight.Normal, size = 14)

    val RecapCaption1 = recapTextStyle(weight = FontWeight.Medium, size = 13)
    val RecapCaption2 = recapTextStyle(weight = FontWeight.Medium, size = 12)
    val RecapCaption3 = recapTextStyle(weight = FontWeight.Medium, size = 10)
}

/** Material3 호환용. 앱 UI는 [RecapTypography]를 우선 사용한다. */
val Typography = Typography(
    displayLarge = recapTextStyle(weight = FontWeight.SemiBold, size = 28),
    displayMedium = recapTextStyle(weight = FontWeight.SemiBold, size = 28),
    displaySmall = RecapTypography.RecapHeading1,
    headlineLarge = RecapTypography.RecapHeading1,
    headlineMedium = RecapTypography.RecapHeading2,
    headlineSmall = RecapTypography.RecapHeading3,
    titleLarge = RecapTypography.RecapHeading1,
    titleMedium = RecapTypography.RecapHeading2,
    titleSmall = RecapTypography.RecapHeading3,
    bodyLarge = RecapTypography.RecapBody1,
    bodyMedium = RecapTypography.RecapBody2,
    bodySmall = RecapTypography.RecapCaption1,
    labelLarge = RecapTypography.RecapCaption1,
    labelMedium = RecapTypography.RecapCaption2,
    labelSmall = RecapTypography.RecapCaption3,
)

private fun recapTextStyle(
    weight: FontWeight,
    size: Int,
): TextStyle {
    return TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * 1.4f).sp,
        letterSpacing = (size * -0.02f).sp,
    )
}
