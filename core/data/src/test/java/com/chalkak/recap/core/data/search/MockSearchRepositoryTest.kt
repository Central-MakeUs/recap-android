package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchScope
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MockSearchRepositoryTest {
    private val cardRepository = mockk<ScreenshotCardRepository>()
    private val cards = MutableStateFlow(listOf(storedCard(1L, "제주 숙소")))
    private val repository = MockSearchRepository(cardRepository)

    @Test
    fun `observeSearch maps room updates`() = runTest {
        every { cardRepository.observeStoredCards() } returns cards
        val pages = async {
            repository.observeSearch(query = "숙소", scope = SearchScope.ALL)
                .take(2)
                .toList()
        }
        runCurrent()

        cards.value = emptyList()

        assertEquals(listOf(1L, 0L), pages.await().map { it.getOrThrow().count })
    }

    private fun storedCard(captureId: Long, title: String): StoredScreenshotCard =
        StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                captureId = captureId,
                typeCode = ScreenshotContentType.PLACE,
                title = title,
                summary = "요약",
                body = "본문",
                originalImageUrl = "mock://captures/$captureId",
                isFavorite = false,
                organizedAt = Instant.ofEpochMilli(captureId),
            ),
            imageRefs = ScreenshotCardImageRefs(),
            updatedAtMillis = captureId,
        )
}
