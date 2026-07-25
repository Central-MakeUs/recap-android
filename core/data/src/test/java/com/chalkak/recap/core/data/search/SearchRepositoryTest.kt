package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.data.search.remote.SearchApi
import com.chalkak.recap.core.data.search.remote.SearchResponseDto
import com.chalkak.recap.core.data.search.remote.SearchResultResponseDto
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SearchRepositoryTest {
    private val searchApi = mockk<SearchApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private lateinit var repository: RemoteSearchRepository

    @BeforeEach
    fun setUp() {
        coEvery { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }
        repository = RemoteSearchRepository(
            searchApi = searchApi,
            thumbnailCache = thumbnailCache,
        )
    }

    @Test
    fun `search maps success response and forwards query params`() = runTest {
        coEvery {
            searchApi.search(
                q = "카드",
                scope = "TYPE",
                typeCode = "KNOWLEDGE",
                page = 1,
                size = 10,
            )
        } returns ApiResponseDto(
            success = true,
            data = SearchResponseDto(
                count = 1L,
                hasNext = false,
                items = listOf(
                    SearchResultResponseDto(
                        captureId = 42L,
                        typeCode = CardTypeDto.KNOWLEDGE,
                        thumbnailUrl = "https://example.com/t.png",
                        titleHighlighted = "<em>카드</em>",
                        summaryHighlighted = "요약",
                        ocrExcerptHighlighted = "OCR <em>카드</em>",
                        isFavorite = true,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )

        val result = repository.search(
            query = "카드",
            scope = SearchScope.TYPE,
            typeCode = ScreenshotContentType.KNOWLEDGE,
            page = 1,
            size = 10,
        )

        val page = result.getOrThrow()
        assertEquals(1L, page.count)
        assertFalse(page.hasNext)
        val item = page.items.single()
        assertEquals(42L, item.captureId)
        assertEquals(ScreenshotContentType.KNOWLEDGE, item.typeCode)
        assertEquals("https://example.com/t.png", item.thumbnailUrl)
        assertEquals("<em>카드</em>", item.titleHighlighted)
        assertEquals("요약", item.summaryHighlighted)
        assertEquals("OCR <em>카드</em>", item.ocrExcerptHighlighted)
        assertTrue(item.isFavorite)
        assertEquals("2026-07-19T00:00:00Z", item.organizedAt)
        coVerify(exactly = 1) {
            searchApi.search(
                q = "카드",
                scope = "TYPE",
                typeCode = "KNOWLEDGE",
                page = 1,
                size = 10,
            )
        }
    }

    @Test
    fun `search omits typeCode when null`() = runTest {
        coEvery {
            searchApi.search(
                q = "숙소",
                scope = "ALL",
                typeCode = null,
                page = 0,
                size = 20,
            )
        } returns ApiResponseDto(
            success = true,
            data = SearchResponseDto(count = 0L, hasNext = false, items = emptyList()),
        )

        val result = repository.search(
            query = "숙소",
            scope = SearchScope.ALL,
        )

        assertEquals(0L, result.getOrThrow().count)
        assertNull(result.getOrThrow().items.firstOrNull())
        coVerify(exactly = 1) {
            searchApi.search(
                q = "숙소",
                scope = "ALL",
                typeCode = null,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `search maps server error`() = runTest {
        coEvery {
            searchApi.search(any(), any(), any(), any(), any())
        } returns ApiResponseDto(
            success = false,
            data = null,
            error = ApiErrorDto(code = "INVALID_INPUT", message = "bad"),
        )

        val result = repository.search(query = "", scope = SearchScope.ALL)

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("INVALID_INPUT", error.code)
    }

    @Test
    fun `search maps network failure`() = runTest {
        coEvery {
            searchApi.search(any(), any(), any(), any(), any())
        } throws IOException("offline")

        val result = repository.search(query = "카드", scope = SearchScope.FAVORITE)

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }
}
