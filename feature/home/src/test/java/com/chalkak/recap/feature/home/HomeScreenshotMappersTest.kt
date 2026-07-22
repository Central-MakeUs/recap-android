package com.chalkak.recap.feature.home

import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.home.HomeSummary
import com.chalkak.recap.core.model.home.TopType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenshotMappersTest {
    @Test
    fun `toHomeUiState maps summary favorites and recent captures`() {
        val summary = HomeSummary(
            recentCaptures = listOf(
                captureSummary(captureId = 2L, title = "최근", typeCode = ScreenshotContentType.PLACE),
                captureSummary(captureId = 1L, title = "이전", typeCode = ScreenshotContentType.SHOPPING),
            ),
            favorites = listOf(
                captureSummary(
                    captureId = 10L,
                    title = "즐겨찾기",
                    typeCode = ScreenshotContentType.SHOPPING,
                    isFavorite = true,
                ),
            ),
            topTypes = listOf(
                TopType(
                    typeCode = ScreenshotContentType.SHOPPING,
                    count = 5L,
                    representativeThumbnailUrl = null,
                ),
            ),
            hasAnyCapture = true,
        )

        val state = summary.toHomeUiState()

        assertEquals(listOf(2L, 1L), state.recentScreenshots.map { it.id })
        assertEquals(listOf(10L), state.favoriteItems.map { it.id })
        assertTrue(state.favoriteItems.single().isFavorite)
        assertEquals(
            listOf(RecapCategoryType.ShoppingProduct),
            state.frequentSaveTypes.map { it.categoryType },
        )
        assertEquals(listOf(5), state.frequentSaveTypes.map { it.recapCount })
    }

    @Test
    fun `toRecapCategoryType maps job career`() {
        assertEquals(RecapCategoryType.JobCareer, ScreenshotContentType.JOB.toRecapCategoryType())
    }

    private fun captureSummary(
        captureId: Long,
        title: String,
        typeCode: ScreenshotContentType,
        isFavorite: Boolean = false,
    ): CaptureSummary {
        return CaptureSummary(
            captureId = captureId,
            title = title,
            summary = "$title 요약",
            typeCode = typeCode,
            thumbnailUrl = "thumb/$captureId",
            isFavorite = isFavorite,
            organizedAt = "2026-07-19T00:00:00Z",
        )
    }
}
