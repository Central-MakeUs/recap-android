package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.swipe.ScreenshotCardSwipeRow
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.core.design.theme.RECAPTheme

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotCardSwipeRevealedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCardSwipeRow(
            onEditClick = {},
            onDeleteClick = {},
            revealed = true,
            onRevealedChange = {},
        ) {
            ScreenshotCard(
                thumbnailModel = R.drawable.bid_landscape_24px,
                categoryType = RecapCategoryType.ScheduleReservation,
                title = "파스타 레시피 저장",
                description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
                isFavorite = false,
                onClick = {},
                onFavoriteClick = {},
                suppressPressScale = isGestureActive,
                modifier = Modifier.screenshotCardSwipeSemantics(),
            )
        }
    }
}

@PreviewTest
@QaPhoneMatrix
@Composable
fun ScreenshotCardSwipeStackedScreenshot() {
    RECAPTheme(dynamicColor = false) {
        Column {
            ScreenshotCardSwipeRow(
                onEditClick = {},
                onDeleteClick = {},
                revealed = true,
                onRevealedChange = {},
            ) {
                ScreenshotCard(
                    thumbnailModel = R.drawable.bid_landscape_24px,
                    categoryType = RecapCategoryType.ScheduleReservation,
                    title = "파스타 레시피 저장",
                    description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
                    isFavorite = false,
                    onClick = {},
                    onFavoriteClick = {},
                    suppressPressScale = isGestureActive,
                    modifier = Modifier.screenshotCardSwipeSemantics(),
                )
            }
            ScreenshotCardSwipeRow(
                onEditClick = {},
                onDeleteClick = {},
                revealed = false,
                onRevealedChange = {},
            ) {
                ScreenshotCard(
                    thumbnailModel = R.drawable.bid_landscape_24px,
                    categoryType = RecapCategoryType.InfoKnowledge,
                    title = "파스타 레시피 저장",
                    description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
                    isFavorite = true,
                    onClick = {},
                    onFavoriteClick = {},
                    suppressPressScale = isGestureActive,
                    modifier = Modifier.screenshotCardSwipeSemantics(),
                )
            }
        }
    }
}
