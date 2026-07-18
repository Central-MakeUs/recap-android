package com.chalkak.recap.feature.home

import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class HomeScreenshotMappersTest {
    @Test
    fun `toHomeUiState keeps four most recent favorites`() {
        val cards = listOf(
            storedCard(
                captureId = 1L,
                title = "즐겨찾기 1",
                contentType = ScreenshotContentType.SHOPPING,
                isFavorite = true,
                organizedAt = Instant.ofEpochMilli(4_000L),
            ),
            storedCard(
                captureId = 2L,
                title = "즐겨찾기 2",
                contentType = ScreenshotContentType.PLACE,
                isFavorite = true,
                organizedAt = Instant.ofEpochMilli(3_000L),
            ),
            storedCard(
                captureId = 3L,
                title = "즐겨찾기 3",
                contentType = ScreenshotContentType.KNOWLEDGE,
                isFavorite = true,
                organizedAt = Instant.ofEpochMilli(2_000L),
            ),
            storedCard(
                captureId = 4L,
                title = "즐겨찾기 4",
                contentType = ScreenshotContentType.CONTENT,
                isFavorite = true,
                organizedAt = Instant.ofEpochMilli(1_000L),
            ),
            storedCard(
                captureId = 5L,
                title = "즐겨찾기 5",
                contentType = ScreenshotContentType.RECORD,
                isFavorite = true,
                organizedAt = Instant.ofEpochMilli(500L),
            ),
            storedCard(
                captureId = 6L,
                title = "일반",
                contentType = ScreenshotContentType.SHOPPING,
                isFavorite = false,
                organizedAt = Instant.ofEpochMilli(5_000L),
            ),
        )

        val state = cards.toHomeUiState()

        assertEquals(listOf(1L, 2L, 3L, 4L), state.favoriteItems.map { it.id })
        assertTrue(state.favoriteItems.all { it.isFavorite })
    }

    @Test
    fun `toHomeUiState keeps top four frequent save types by count`() {
        val cards = buildList {
            repeat(5) { index ->
                add(
                    storedCard(
                        captureId = 100L + index,
                        title = "쇼핑 $index",
                        contentType = ScreenshotContentType.SHOPPING,
                        organizedAt = Instant.ofEpochMilli(1_000L + index),
                    ),
                )
            }
            repeat(4) { index ->
                add(
                    storedCard(
                        captureId = 200L + index,
                        title = "장소 $index",
                        contentType = ScreenshotContentType.PLACE,
                        organizedAt = Instant.ofEpochMilli(2_000L + index),
                    ),
                )
            }
            repeat(3) { index ->
                add(
                    storedCard(
                        captureId = 300L + index,
                        title = "정보 $index",
                        contentType = ScreenshotContentType.KNOWLEDGE,
                        organizedAt = Instant.ofEpochMilli(3_000L + index),
                    ),
                )
            }
            repeat(2) { index ->
                add(
                    storedCard(
                        captureId = 400L + index,
                        title = "책 $index",
                        contentType = ScreenshotContentType.CONTENT,
                        organizedAt = Instant.ofEpochMilli(4_000L + index),
                    ),
                )
            }
            add(
                storedCard(
                    captureId = 500L,
                    title = "기록",
                    contentType = ScreenshotContentType.RECORD,
                    organizedAt = Instant.ofEpochMilli(5_000L),
                ),
            )
        }

        val state = cards.toHomeUiState()

        assertEquals(
            listOf(
                RecapCategoryType.ShoppingProduct,
                RecapCategoryType.PlaceRestaurant,
                RecapCategoryType.InfoKnowledge,
                RecapCategoryType.BookContent,
            ),
            state.frequentSaveTypes.map { it.categoryType },
        )
        assertEquals(listOf(5, 4, 3, 2), state.frequentSaveTypes.map { it.recapCount })
    }

    @Test
    fun `toHomeUiState maps recent screenshots newest first`() {
        val cards = listOf(
            storedCard(
                captureId = 1L,
                title = "이전",
                contentType = ScreenshotContentType.SHOPPING,
                organizedAt = Instant.ofEpochMilli(1_000L),
            ),
            storedCard(
                captureId = 2L,
                title = "최근",
                contentType = ScreenshotContentType.PLACE,
                organizedAt = Instant.ofEpochMilli(2_000L),
            ),
        )

        val state = cards.toHomeUiState()

        assertEquals(listOf(2L, 1L), state.recentScreenshots.map { it.id })
    }

    @Test
    fun `toRecapCategoryType maps job career`() {
        assertEquals(RecapCategoryType.JobCareer, ScreenshotContentType.JOB.toRecapCategoryType())
    }

    private fun storedCard(
        captureId: Long,
        title: String,
        contentType: ScreenshotContentType,
        isFavorite: Boolean = false,
        organizedAt: Instant,
    ): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                captureId = captureId,
                typeCode = contentType,
                title = title,
                summary = "$title 요약",
                body = "body-$captureId",
                originalImageUrl = "mock://captures/$captureId",
                isFavorite = isFavorite,
                organizedAt = organizedAt,
            ),
            imageRefs = ScreenshotCardImageRefs(thumbnailPath = "thumb/$captureId"),
            updatedAtMillis = organizedAt.toEpochMilli(),
        )
    }
}
