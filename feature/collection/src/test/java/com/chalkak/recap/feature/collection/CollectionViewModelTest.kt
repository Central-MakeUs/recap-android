package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
import com.chalkak.recap.core.model.screenshot.ScreenshotKeyField
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
    private val imageStorage = mockk<ScreenshotImageStorage>()
    private val cardsFlow = MutableSharedFlow<List<StoredScreenshotCard>>(replay = 1)
    private lateinit var viewModel: CollectionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observeStoredCards() } returns cardsFlow
        every { imageStorage.deleteStoredImages(any()) } just Runs
        viewModel = CollectionViewModel(repository, imageStorage).apply {
            ioDispatcher = testDispatcher
        }
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
    fun `other content type cards appear only in others tab`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "other-1",
                    title = "연말정산 서류 목록",
                    contentType = ScreenshotContentType.OTHER,
                    createdAtMillis = 300L,
                ),
                storedCard(
                    imageId = "shopping-1",
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    createdAtMillis = 200L,
                ),
                storedCard(
                    imageId = "other-2",
                    title = "미분류 메모",
                    contentType = ScreenshotContentType.OTHER,
                    createdAtMillis = 100L,
                ),
            ),
        )
        advanceUntilIdle()

        val overview = viewModel.uiState.value.overview
        assertEquals(1, overview.typeSummaries.size)
        assertEquals(ScreenshotContentType.SHOPPING_PRODUCT, overview.typeSummaries.single().contentType)
        assertEquals(
            listOf("other-1", "other-2"),
            overview.otherItems.map { it.imageId },
        )
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

    @Test
    fun `selection mode toggles items and cancel clears selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "card-1",
                    title = "Card",
                    createdAtMillis = 100L,
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))

        assertTrue(viewModel.uiState.value.selection.isActive)
        assertEquals(setOf("card-1"), viewModel.uiState.value.selection.selectedImageIds)

        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))
        assertTrue(viewModel.uiState.value.selection.selectedImageIds.isEmpty())

        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))
        viewModel.onAction(CollectionAction.CancelSelection)

        assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
    }

    @Test
    fun `toggle all selection affects only visible stored ids`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(imageId = "card-1", title = "First", createdAtMillis = 200L),
                storedCard(imageId = "card-2", title = "Second", createdAtMillis = 100L),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(
            CollectionAction.ToggleAllSelection(setOf("card-1", "card-2", "missing")),
        )

        assertEquals(
            setOf("card-1", "card-2"),
            viewModel.uiState.value.selection.selectedImageIds,
        )

        viewModel.onAction(
            CollectionAction.ToggleAllSelection(setOf("card-1", "card-2", "missing")),
        )

        assertTrue(viewModel.uiState.value.selection.selectedImageIds.isEmpty())
    }

    @Test
    fun `delete selected removes Room cards then stored files and exits selection`() =
        runTest(testDispatcher) {
            coEvery { repository.deleteCards(any()) } returns Unit
            cardsFlow.emit(
                listOf(
                    storedCard(imageId = "card-1", title = "First", createdAtMillis = 200L),
                    storedCard(imageId = "card-2", title = "Second", createdAtMillis = 100L),
                ),
            )
            advanceUntilIdle()
            viewModel.onAction(CollectionAction.StartSelection)
            viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))
            viewModel.onAction(CollectionAction.ToggleItemSelection("card-2"))

            viewModel.onAction(CollectionAction.DeleteSelected)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.deleteCards(setOf("card-1", "card-2"))
            }
            verify(exactly = 1) {
                imageStorage.deleteStoredImages(setOf("card-1", "card-2"))
            }
            coVerifyOrder {
                repository.deleteCards(setOf("card-1", "card-2"))
                imageStorage.deleteStoredImages(setOf("card-1", "card-2"))
            }
            assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
        }

    @Test
    fun `delete selected failure keeps selection available for retry`() = runTest(testDispatcher) {
        coEvery { repository.deleteCards(any()) } throws IllegalStateException("database failure")
        cardsFlow.emit(
            listOf(
                storedCard(imageId = "card-1", title = "First", createdAtMillis = 100L),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))

        viewModel.onAction(CollectionAction.DeleteSelected)
        advanceUntilIdle()

        verify(exactly = 0) { imageStorage.deleteStoredImages(any()) }
        assertTrue(viewModel.uiState.value.selection.isActive)
        assertFalse(viewModel.uiState.value.selection.isDeleting)
        assertEquals(setOf("card-1"), viewModel.uiState.value.selection.selectedImageIds)
    }

    @Test
    fun `completed delete does not clear a newer selection generation`() = runTest(testDispatcher) {
        val deleteGate = CompletableDeferred<Unit>()
        coEvery { repository.deleteCards(any()) } coAnswers { deleteGate.await() }
        cardsFlow.emit(
            listOf(
                storedCard(imageId = "card-1", title = "First", createdAtMillis = 200L),
                storedCard(imageId = "card-2", title = "Second", createdAtMillis = 100L),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))
        viewModel.onAction(CollectionAction.DeleteSelected)
        runCurrent()

        viewModel.onAction(CollectionAction.CancelSelection)
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection("card-2"))
        deleteGate.complete(Unit)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selection.isActive)
        assertEquals(setOf("card-2"), viewModel.uiState.value.selection.selectedImageIds)
    }

    @Test
    fun `delete selected with no items does not call repository`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(imageId = "card-1", title = "First", createdAtMillis = 100L),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)

        viewModel.onAction(CollectionAction.DeleteSelected)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.deleteCards(any()) }
    }

    @Test
    fun `changing tabs clears selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(imageId = "card-1", title = "First", createdAtMillis = 100L),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))

        viewModel.onAction(CollectionAction.SelectTab(CollectionTab.Types))

        assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
    }

    @Test
    fun `closing detail clears selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    imageId = "card-1",
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING_PRODUCT,
                    createdAtMillis = 100L,
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING_PRODUCT))
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection("card-1"))

        viewModel.onAction(CollectionAction.CloseDetail)

        assertNull(viewModel.uiState.value.detail)
        assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
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
