package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenshotMappersTest {
    @Test
    fun `toHomeUiState keeps three most recent favorites`() {
        val cards = listOf(
            storedCard(
                imageId = "fav-1",
                title = "즐겨찾기 1",
                contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                isFavorite = true,
                createdAtMillis = 3_000L,
            ),
            storedCard(
                imageId = "fav-2",
                title = "즐겨찾기 2",
                contentType = ScreenshotContentType.PLACE_RESTAURANT,
                isFavorite = true,
                createdAtMillis = 2_000L,
            ),
            storedCard(
                imageId = "fav-3",
                title = "즐겨찾기 3",
                contentType = ScreenshotContentType.INFO_KNOWLEDGE,
                isFavorite = true,
                createdAtMillis = 1_000L,
            ),
            storedCard(
                imageId = "fav-4",
                title = "즐겨찾기 4",
                contentType = ScreenshotContentType.BOOK_CONTENT,
                isFavorite = true,
                createdAtMillis = 500L,
            ),
            storedCard(
                imageId = "normal",
                title = "일반",
                contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                isFavorite = false,
                createdAtMillis = 4_000L,
            ),
        )

        val state = cards.toHomeUiState()

        assertEquals(listOf("fav-1", "fav-2", "fav-3"), state.favoriteItems.map { it.id })
        assertTrue(state.favoriteItems.all { it.isFavorite })
    }

    @Test
    fun `toHomeUiState keeps top three frequent save types by count`() {
        val cards = buildList {
            repeat(5) { index ->
                add(
                    storedCard(
                        imageId = "shopping-$index",
                        title = "쇼핑 $index",
                        contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                        createdAtMillis = 1_000L + index,
                    ),
                )
            }
            repeat(3) { index ->
                add(
                    storedCard(
                        imageId = "place-$index",
                        title = "장소 $index",
                        contentType = ScreenshotContentType.PLACE_RESTAURANT,
                        createdAtMillis = 2_000L + index,
                    ),
                )
            }
            repeat(2) { index ->
                add(
                    storedCard(
                        imageId = "book-$index",
                        title = "책 $index",
                        contentType = ScreenshotContentType.BOOK_CONTENT,
                        createdAtMillis = 3_000L + index,
                    ),
                )
            }
            add(
                storedCard(
                    imageId = "info-0",
                    title = "정보",
                    contentType = ScreenshotContentType.INFO_KNOWLEDGE,
                    createdAtMillis = 4_000L,
                ),
            )
        }

        val state = cards.toHomeUiState()

        assertEquals(
            listOf(
                RecapCategoryType.ShoppingProduct,
                RecapCategoryType.PlaceRestaurant,
                RecapCategoryType.BookContent,
            ),
            state.frequentSaveTypes.map { it.categoryType },
        )
        assertEquals(listOf(5, 3, 2), state.frequentSaveTypes.map { it.recapCount })
    }

    @Test
    fun `toHomeUiState maps recent screenshots newest first`() {
        val cards = listOf(
            storedCard(
                imageId = "older",
                title = "이전",
                contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                createdAtMillis = 1_000L,
            ),
            storedCard(
                imageId = "newer",
                title = "최근",
                contentType = ScreenshotContentType.PLACE_RESTAURANT,
                createdAtMillis = 2_000L,
            ),
        )

        val state = cards.toHomeUiState()

        assertEquals(listOf("newer", "older"), state.recentScreenshots.map { it.id })
    }

    @Test
    fun `toRecapCategoryType maps job career`() {
        assertEquals(RecapCategoryType.JobCareer, ScreenshotContentType.JOB_CAREER.toRecapCategoryType())
    }

    private fun storedCard(
        imageId: String,
        title: String,
        contentType: ScreenshotContentType,
        isFavorite: Boolean = false,
        createdAtMillis: Long,
    ): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                imageId = imageId,
                title = title,
                summary = "$title 요약",
                contentTypes = ScreenshotContentTypes(primaryContentType = contentType),
                keyFields = emptyList(),
                confidence = ScreenshotAnalysisConfidence.HIGH,
                isFavorite = isFavorite,
            ),
            imageRefs = ScreenshotCardImageRefs(thumbnailPath = "thumb/$imageId"),
            createdAtMillis = createdAtMillis,
            updatedAtMillis = createdAtMillis,
        )
    }
}
