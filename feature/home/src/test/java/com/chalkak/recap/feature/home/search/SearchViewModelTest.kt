package com.chalkak.recap.feature.home.search

import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.search.RecentSearchStore
import com.chalkak.recap.core.data.search.SearchRepository
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchResult
import com.chalkak.recap.core.model.search.SearchScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val searchRepository = mockk<SearchRepository>()
    private val captureMutationRepository = mockk<CaptureMutationRepository>()
    private val recentSearchesFlow = MutableStateFlow<List<String>>(emptyList())
    private val recentSearchStore = mockk<RecentSearchStore>(relaxed = true)
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { recentSearchStore.recentSearches } returns recentSearchesFlow
        coEvery { recentSearchStore.remember(any()) } coAnswers {
            val term = firstArg<String>().trim()
            recentSearchesFlow.value = (
                listOf(term) + recentSearchesFlow.value.filterNot {
                    it.equals(term, ignoreCase = true)
                }
                ).take(10)
        }
        coEvery { recentSearchStore.clearAll() } coAnswers {
            recentSearchesFlow.value = emptyList()
        }
        coEvery { recentSearchStore.remove(any()) } coAnswers {
            val term = firstArg<String>().trim()
            recentSearchesFlow.value = recentSearchesFlow.value.filterNot {
                it.equals(term, ignoreCase = true)
            }
        }
        viewModel = SearchViewModel(
            searchRepository = searchRepository,
            captureMutationRepository = captureMutationRepository,
            recentSearchStore = recentSearchStore,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit search maps query highlight and remembers term on attempt`() = runTest {
        coEvery {
            searchRepository.search(
                query = "숙소",
                scope = SearchScope.ALL,
                typeCode = null,
                page = 0,
                size = 20,
            )
        } returns Result.success(
            SearchPage(
                count = 1L,
                hasNext = false,
                items = listOf(
                    SearchResult(
                        captureId = 7L,
                        typeCode = ScreenshotContentType.PLACE,
                        thumbnailUrl = "https://example.com/t.png",
                        titleHighlighted = "제주 <mark>숙소</mark> 예약",
                        summaryHighlighted = "한 줄 <mark>숙소</mark> 요약",
                        ocrExcerptHighlighted = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )

        viewModel.onAction(SearchAction.UpdateQuery(" 숙소 "))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SearchContentPhase.Results, state.phase)
        assertEquals("숙소", state.submittedQuery)
        assertEquals(1L, state.resultCount)
        val item = state.results.single()
        assertEquals(7L, item.captureId)
        assertEquals("제주 숙소 예약", item.title)
        assertEquals(3..4, item.titleHighlightRange)
        assertEquals("한 줄 숙소 요약", item.description)
        assertEquals(4..5, item.descriptionHighlightRange)
        assertEquals(listOf("숙소"), state.recentSearches)
        coVerify(exactly = 1) { recentSearchStore.remember("숙소") }
        coVerify(exactly = 1) {
            searchRepository.search(
                query = "숙소",
                scope = SearchScope.ALL,
                typeCode = null,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `submit search with blank query is no-op`() = runTest {
        viewModel.onAction(SearchAction.UpdateQuery("   "))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()

        assertEquals(SearchContentPhase.Idle, viewModel.uiState.value.phase)
        coVerify(exactly = 0) { recentSearchStore.remember(any()) }
        coVerify(exactly = 0) {
            searchRepository.search(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `submit search empty page becomes Empty phase`() = runTest {
        coEvery {
            searchRepository.search(any(), any(), any(), any(), any())
        } returns Result.success(
            SearchPage(count = 0L, hasNext = false, items = emptyList()),
        )

        viewModel.onAction(SearchAction.UpdateQuery("없는검색어"))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()

        assertEquals(SearchContentPhase.Empty, viewModel.uiState.value.phase)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertEquals(listOf("없는검색어"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun `submit search failure becomes Error phase but still remembers term`() = runTest {
        coEvery {
            searchRepository.search(any(), any(), any(), any(), any())
        } returns Result.failure(IllegalStateException("offline"))

        viewModel.onAction(SearchAction.UpdateQuery("숙소"))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()

        assertEquals(SearchContentPhase.Error, viewModel.uiState.value.phase)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertEquals(listOf("숙소"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun `load more appends next page`() = runTest {
        coEvery {
            searchRepository.search(query = "숙소", scope = SearchScope.ALL, typeCode = null, page = 0, size = 20)
        } returns Result.success(
            SearchPage(
                count = 2L,
                hasNext = true,
                items = listOf(
                    SearchResult(
                        captureId = 1L,
                        typeCode = ScreenshotContentType.PLACE,
                        thumbnailUrl = null,
                        titleHighlighted = "숙소 A",
                        summaryHighlighted = "요약",
                        ocrExcerptHighlighted = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )
        coEvery {
            searchRepository.search(query = "숙소", scope = SearchScope.ALL, typeCode = null, page = 1, size = 20)
        } returns Result.success(
            SearchPage(
                count = 2L,
                hasNext = false,
                items = listOf(
                    SearchResult(
                        captureId = 2L,
                        typeCode = ScreenshotContentType.PLACE,
                        thumbnailUrl = null,
                        titleHighlighted = "숙소 B",
                        summaryHighlighted = "요약",
                        ocrExcerptHighlighted = null,
                        isFavorite = false,
                        organizedAt = "2026-07-18T00:00:00Z",
                    ),
                ),
            ),
        )

        viewModel.onAction(SearchAction.UpdateQuery("숙소"))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()
        viewModel.onAction(SearchAction.LoadMore)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L), state.results.map { it.captureId })
        assertFalse(state.hasNext)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `clearing query returns to Idle and clears results`() = runTest {
        coEvery {
            searchRepository.search(any(), any(), any(), any(), any())
        } returns Result.success(
            SearchPage(
                count = 1L,
                hasNext = false,
                items = listOf(
                    SearchResult(
                        captureId = 7L,
                        typeCode = ScreenshotContentType.PLACE,
                        thumbnailUrl = null,
                        titleHighlighted = "숙소",
                        summaryHighlighted = "요약",
                        ocrExcerptHighlighted = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )

        viewModel.onAction(SearchAction.UpdateQuery("숙소"))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()
        viewModel.onAction(SearchAction.UpdateQuery(""))
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
        assertEquals("", viewModel.uiState.value.submittedQuery)
        assertEquals(SearchContentPhase.Idle, viewModel.uiState.value.phase)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertEquals(0L, viewModel.uiState.value.resultCount)
    }

    @Test
    fun `reset clears search bar and results while keeping recent searches`() = runTest {
        recentSearchesFlow.value = listOf("최근검색")
        advanceUntilIdle()
        coEvery {
            searchRepository.search(any(), any(), any(), any(), any())
        } returns Result.success(
            SearchPage(
                count = 1L,
                hasNext = false,
                items = listOf(
                    SearchResult(
                        captureId = 7L,
                        typeCode = ScreenshotContentType.PLACE,
                        thumbnailUrl = null,
                        titleHighlighted = "숙소",
                        summaryHighlighted = "요약",
                        ocrExcerptHighlighted = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )

        viewModel.onAction(SearchAction.UpdateQuery("숙소"))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()
        viewModel.onAction(SearchAction.Reset)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
        assertEquals("", viewModel.uiState.value.submittedQuery)
        assertEquals(SearchContentPhase.Idle, viewModel.uiState.value.phase)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertEquals(0L, viewModel.uiState.value.resultCount)
        assertEquals(listOf("숙소", "최근검색"), viewModel.uiState.value.recentSearches)
    }

    @Test
    fun `toggle favorite updates local state and calls mutation`() = runTest {
        coEvery {
            searchRepository.search(any(), any(), any(), any(), any())
        } returns Result.success(
            SearchPage(
                count = 1L,
                hasNext = false,
                items = listOf(
                    SearchResult(
                        captureId = 7L,
                        typeCode = ScreenshotContentType.PLACE,
                        thumbnailUrl = null,
                        titleHighlighted = "제목",
                        summaryHighlighted = "요약",
                        ocrExcerptHighlighted = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        } returns Result.success(Unit)

        viewModel.onAction(SearchAction.UpdateQuery("제목"))
        viewModel.onAction(SearchAction.SubmitSearch)
        advanceUntilIdle()

        viewModel.onAction(SearchAction.ToggleFavorite(7L))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.results.single().isFavorite)
        coVerify(exactly = 1) {
            captureMutationRepository.updateFavorite(captureId = 7L, isFavorite = true)
        }
    }

    @Test
    fun `select recent search submits immediately`() = runTest {
        coEvery {
            searchRepository.search(any(), any(), any(), any(), any())
        } returns Result.success(
            SearchPage(count = 0L, hasNext = false, items = emptyList()),
        )

        viewModel.onAction(SearchAction.SelectRecentSearch("파스타"))
        advanceUntilIdle()

        assertEquals("파스타", viewModel.uiState.value.query)
        assertEquals(SearchContentPhase.Empty, viewModel.uiState.value.phase)
        coVerify(exactly = 1) {
            searchRepository.search(
                query = "파스타",
                scope = SearchScope.ALL,
                typeCode = null,
                page = 0,
                size = 20,
            )
        }
        assertNull(viewModel.uiState.value.results.firstOrNull())
    }

    @Test
    fun `remove recent search deletes matching term`() = runTest {
        recentSearchesFlow.value = listOf("파스타", "숙소")

        viewModel.onAction(SearchAction.RemoveRecentSearch("파스타"))
        advanceUntilIdle()

        assertEquals(listOf("숙소"), viewModel.uiState.value.recentSearches)
        coVerify(exactly = 1) { recentSearchStore.remove("파스타") }
    }

    @Test
    fun `clear all recent searches empties list`() = runTest {
        recentSearchesFlow.value = listOf("파스타", "숙소")

        viewModel.onAction(SearchAction.ClearAllRecentSearches)
        advanceUntilIdle()

        assertEquals(emptyList<String>(), viewModel.uiState.value.recentSearches)
        coVerify(exactly = 1) { recentSearchStore.clearAll() }
    }
}
