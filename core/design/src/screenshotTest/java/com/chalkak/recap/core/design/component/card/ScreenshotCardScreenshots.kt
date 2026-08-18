package com.chalkak.recap.core.design.component.card

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotCardSwipeRevealedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = RecapCategoryType.ScheduleReservation,
            title = "파스타 레시피 저장",
            description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
            swipeActionsEnabled = true,
            swipeRevealed = true,
        )
    }
}
