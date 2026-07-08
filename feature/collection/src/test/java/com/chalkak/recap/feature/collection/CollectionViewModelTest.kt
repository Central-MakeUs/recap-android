package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
import com.chalkak.recap.core.model.screenshot.ScreenshotKeyField
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
class CollectionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ScreenshotCardRepository>()
    private val cardsFlow = MutableSharedFlow<List<StoredScreenshotCard>>(replay = 1)
    private lateinit var viewModel: CollectionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observeStoredCards() } returns cardsFlow
        viewModel = CollectionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty repository emits empty overview state`() = runTest(testDispatcher) {
        cardsFlow.emit(emptyList())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasStoredScreenshots)
        assertNull(state.overview.favoriteSummary)
        assertTrue(state.overview.typeSummaries.isEmpty())
    }

    @Test
    fun `stored cards without favorites show empty favorite summary`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "shopping-1",
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    createdAtMillis = 200L,
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasStoredScreenshots)
        assertNull(state.overview.favoriteSummary)
        assertEquals(1, state.overview.typeSummaries.single().count)
    }

    @Test
    fun `favorite cards populate favorite summary count`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "favorite-1",
                    title = "즐겨찾기 1",
                    isFavorite = true,
                    createdAtMillis = 300L,
                ),
                storedCard(
                    imageId = "favorite-2",
                    title = "즐겨찾기 2",
                    isFavorite = true,
                    createdAtMillis = 200L,
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.overview.favoriteSummary?.count)
    }

    @Test
    fun `type summaries group cards and build example text`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "shopping-1",
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    createdAtMillis = 300L,
                ),
                storedCard(
                    imageId = "shopping-2",
                    title = "노트북 가격 비교",
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    createdAtMillis = 200L,
                ),
                storedCard(
                    imageId = "shopping-3",
                    title = "여름 원피스 주문내역",
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    createdAtMillis = 100L,
                ),
            ),
        )
        advanceUntilIdle()

        val summary = viewModel.uiState.value.overview.typeSummaries.single()
        assertEquals(3, summary.count)
        assertEquals(
            listOf("택배 반품 절차", "노트북 가격 비교"),
            summary.exampleTitles,
        )
        assertEquals(1, summary.additionalExampleCount)
    }

    @Test
    fun `open favorite detail filters favorite cards latest first`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "favorite-old",
                    title = "Old favorite",
                    isFavorite = true,
                    createdAtMillis = 100L,
                ),
                storedCard(
                    imageId = "favorite-new",
                    title = "New favorite",
                    isFavorite = true,
                    createdAtMillis = 300L,
                ),
                storedCard(
                    imageId = "regular",
                    title = "Regular",
                    createdAtMillis = 200L,
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.OpenFavoriteDetail)
        advanceUntilIdle()

        val detail = viewModel.uiState.value.detail
        assertEquals(2, detail?.count)
        assertEquals("favorite-new", detail?.cards?.first()?.imageId)
    }

    @Test
    fun `toggle favorite delegates to repository`() = runTest(testDispatcher) {
        coEvery { repository.updateFavorite(any(), any()) } returns Unit
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "card-1",
                    title = "Card",
                    isFavorite = false,
                    createdAtMillis = 100L,
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.ToggleFavorite("card-1"))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.updateFavorite(imageId = "card-1", isFavorite = true)
        }
    }

    private fun storedCard(
        imageId: String,
        title: String,
        summary: String = "summary",
        contentType: ScreenshotContentType = ScreenshotContentType.OTHER,
        isFavorite: Boolean = false,
        createdAtMillis: Long,
    ): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                imageId = imageId,
                title = title,
                summary = summary,
                contentTypes = ScreenshotContentTypes(primaryContentType = contentType),
                keyFields = listOf(
                    ScreenshotKeyField(
                        label = "label",
                        value = "value",
                        displayPriority = 1,
                        isSensitive = false,
                    ),
                ),
                confidence = ScreenshotAnalysisConfidence.HIGH,
                isFavorite = isFavorite,
            ),
            imageRefs = ScreenshotCardImageRefs(sourceImageUri = "content://$imageId"),
            createdAtMillis = createdAtMillis,
            updatedAtMillis = createdAtMillis,
        )
    }
}
