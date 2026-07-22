package com.chalkak.recap.feature.collection

import com.chalkak.recap.core.data.capture.MockCaptureMutationRepository
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.data.storage.MockStorageRepository
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val cardRepository = mockk<ScreenshotCardRepository>()
    private val imageStorage = mockk<ScreenshotImageStorage>()
    private val cardsFlow = MutableSharedFlow<List<StoredScreenshotCard>>(replay = 1)
    private lateinit var captureMutations: MockCaptureMutationRepository
    private lateinit var viewModel: CollectionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { cardRepository.observeStoredCards() } returns cardsFlow
        every { imageStorage.deleteStoredImages(any()) } just Runs
        captureMutations = MockCaptureMutationRepository(
            screenshotCardRepository = cardRepository,
            screenshotImageStorage = imageStorage,
        ).apply {
            ioDispatcher = testDispatcher
        }
        viewModel = CollectionViewModel(
            storageRepository = MockStorageRepository(cardRepository),
            captureMutationRepository = captureMutations,
        )
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
        assertEquals(0, state.overview.favoriteSummary.count)
        assertTrue(state.overview.typeSummaries.isEmpty())
    }

    @Test
    fun `stored cards without favorites still expose zero favorite summary`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasStoredScreenshots)
        assertEquals(0, state.overview.favoriteSummary.count)
        assertEquals(1, state.overview.typeSummaries.single().count)
    }

    @Test
    fun `favorite cards populate favorite summary count`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "즐겨찾기 1",
                    isFavorite = true,
                    organizedAt = Instant.ofEpochMilli(300L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "즐겨찾기 2",
                    isFavorite = true,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.overview.favoriteSummary.count)
    }

    @Test
    fun `type summaries group cards and build example text`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(300L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "노트북 가격 비교",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
                storedCard(
                    captureId = 3L,
                    title = "여름 원피스 주문내역",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
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
    fun `other content type appears in overview type summaries`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "연말정산 서류 목록",
                    contentType = ScreenshotContentType.ETC,
                    organizedAt = Instant.ofEpochMilli(300L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
                storedCard(
                    captureId = 3L,
                    title = "미분류 메모",
                    contentType = ScreenshotContentType.ETC,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()

        val overview = viewModel.uiState.value.overview
        assertEquals(
            listOf(
                ScreenshotContentType.SHOPPING,
                ScreenshotContentType.ETC,
            ),
            overview.typeSummaries.map { it.contentType },
        )
        assertEquals(2, overview.typeSummaries.last().count)
    }

    @Test
    fun `zero count categories are excluded from overview`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "택배 반품 절차",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(
            listOf(ScreenshotContentType.SHOPPING),
            viewModel.uiState.value.overview.typeSummaries.map { it.contentType },
        )
    }

    @Test
    fun `search filters favorite and category counts together`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "노트북 할인",
                    contentType = ScreenshotContentType.SHOPPING,
                    isFavorite = true,
                    organizedAt = Instant.ofEpochMilli(300L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "카페 예약",
                    contentType = ScreenshotContentType.PLACE,
                    isFavorite = true,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
                storedCard(
                    captureId = 3L,
                    title = "노트북 메모",
                    contentType = ScreenshotContentType.ETC,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.UpdateSearchQuery("노트북"))
        advanceUntilIdle()

        val overview = viewModel.uiState.value.overview
        assertEquals(1, overview.favoriteSummary.count)
        assertEquals(
            listOf(
                ScreenshotContentType.SHOPPING,
                ScreenshotContentType.ETC,
            ),
            overview.typeSummaries.map { it.contentType },
        )
    }

    @Test
    fun `open favorite detail filters favorite cards latest first`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "Old favorite",
                    isFavorite = true,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "New favorite",
                    isFavorite = true,
                    organizedAt = Instant.ofEpochMilli(300L),
                ),
                storedCard(
                    captureId = 3L,
                    title = "Regular",
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.OpenFavoriteDetail)
        advanceUntilIdle()

        val detail = viewModel.uiState.value.detail
        assertEquals(2, detail?.count)
        assertEquals(2L, detail?.cards?.first()?.captureId)
        assertEquals(
            com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode.CategoryChip,
            detail?.cardMetadataMode,
        )
    }

    @Test
    fun `open type detail uses organized date metadata mode`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "기타",
                    contentType = ScreenshotContentType.ETC,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.ETC))
        advanceUntilIdle()

        val detail = viewModel.uiState.value.detail
        assertEquals(1, detail?.count)
        assertEquals(
            com.chalkak.recap.core.design.component.card.ScreenshotCardMetadataMode.OrganizedDate,
            detail?.cardMetadataMode,
        )
        assertEquals(
            com.chalkak.recap.core.design.category.RecapCategoryType.Other,
            detail?.categoryType,
        )
    }

    @Test
    fun `toggle favorite delegates to repository`() = runTest(testDispatcher) {
        coEvery { cardRepository.updateFavorite(any(), any()) } returns Unit
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "Card",
                    contentType = ScreenshotContentType.SHOPPING,
                    isFavorite = false,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.ToggleFavorite(1L))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            cardRepository.updateFavorite(captureId = 1L, isFavorite = true)
        }
    }

    @Test
    fun `selection mode toggles items and cancel clears selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "Card",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()

        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))

        assertTrue(viewModel.uiState.value.selection.isActive)
        assertEquals(setOf(1L), viewModel.uiState.value.selection.selectedCaptureIds)

        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))
        assertTrue(viewModel.uiState.value.selection.selectedCaptureIds.isEmpty())

        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))
        viewModel.onAction(CollectionAction.CancelSelection)

        assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
    }

    @Test
    fun `delete selected shows confirm dialog without deleting`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))

        viewModel.onAction(CollectionAction.DeleteSelected)

        assertTrue(viewModel.uiState.value.selection.showDeleteConfirmDialog)
        coVerify(exactly = 0) { cardRepository.deleteCards(any()) }
    }

    @Test
    fun `dismiss delete confirm dialog keeps selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))
        viewModel.onAction(CollectionAction.DeleteSelected)

        viewModel.onAction(CollectionAction.DismissDeleteConfirmDialog)

        assertFalse(viewModel.uiState.value.selection.showDeleteConfirmDialog)
        assertTrue(viewModel.uiState.value.selection.isActive)
        assertEquals(setOf(1L), viewModel.uiState.value.selection.selectedCaptureIds)
    }

    @Test
    fun `delete selected removes Room cards then stored files and exits selection`() =
        runTest(testDispatcher) {
            coEvery { cardRepository.deleteCards(any()) } returns Unit
            cardsFlow.emit(
                listOf(
                    storedCard(
                        captureId = 1L,
                        title = "First",
                        contentType = ScreenshotContentType.SHOPPING,
                        organizedAt = Instant.ofEpochMilli(200L),
                    ),
                    storedCard(
                        captureId = 2L,
                        title = "Second",
                        contentType = ScreenshotContentType.SHOPPING,
                        organizedAt = Instant.ofEpochMilli(100L),
                    ),
                ),
            )
            advanceUntilIdle()
            viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
            advanceUntilIdle()
            viewModel.onAction(CollectionAction.StartSelection)
            viewModel.onAction(CollectionAction.ToggleItemSelection(1L))
            viewModel.onAction(CollectionAction.ToggleItemSelection(2L))

            viewModel.onAction(CollectionAction.DeleteSelected)
            val eventDeferred = async { viewModel.events.first() }
            viewModel.onAction(CollectionAction.ConfirmDeleteSelected)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                cardRepository.deleteCards(setOf(1L, 2L))
            }
            verify(exactly = 1) {
                imageStorage.deleteStoredImages(setOf(1L, 2L))
            }
            coVerifyOrder {
                cardRepository.deleteCards(setOf(1L, 2L))
                imageStorage.deleteStoredImages(setOf(1L, 2L))
            }
            assertEquals(
                CollectionEvent.ShowDeleteSuccessToast(deletedCount = 2),
                eventDeferred.await(),
            )
            assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
        }

    @Test
    fun `delete selected failure keeps selection available for retry`() = runTest(testDispatcher) {
        coEvery { cardRepository.deleteCards(any()) } throws IllegalStateException("database failure")
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))

        viewModel.onAction(CollectionAction.DeleteSelected)
        val eventDeferred = async { viewModel.events.first() }
        viewModel.onAction(CollectionAction.ConfirmDeleteSelected)
        advanceUntilIdle()

        verify(exactly = 0) { imageStorage.deleteStoredImages(any()) }
        assertEquals(CollectionEvent.ShowDeleteFailureToast, eventDeferred.await())
        assertTrue(viewModel.uiState.value.selection.isActive)
        assertFalse(viewModel.uiState.value.selection.isDeleting)
        assertFalse(viewModel.uiState.value.selection.showDeleteConfirmDialog)
        assertEquals(setOf(1L), viewModel.uiState.value.selection.selectedCaptureIds)
    }

    @Test
    fun `partial delete success keeps failed ids selected`() = runTest(testDispatcher) {
        val captureMutationRepository = mockk<com.chalkak.recap.core.data.capture.CaptureMutationRepository>()
        coEvery { captureMutationRepository.deleteCaptures(setOf(1L, 2L)) } returns Result.success(
            CaptureDeleteResult(
                deletedIds = setOf(1L),
                failedIds = setOf(2L),
            ),
        )
        viewModel = CollectionViewModel(
            storageRepository = MockStorageRepository(cardRepository),
            captureMutationRepository = captureMutationRepository,
        )
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "Second",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))
        viewModel.onAction(CollectionAction.ToggleItemSelection(2L))

        viewModel.onAction(CollectionAction.DeleteSelected)
        val eventDeferred = async { viewModel.events.first() }
        viewModel.onAction(CollectionAction.ConfirmDeleteSelected)
        advanceUntilIdle()

        assertEquals(
            CollectionEvent.ShowDeletePartialFailureToast(deletedCount = 1, failedCount = 1),
            eventDeferred.await(),
        )
        assertTrue(viewModel.uiState.value.selection.isActive)
        assertFalse(viewModel.uiState.value.selection.isDeleting)
        assertEquals(setOf(2L), viewModel.uiState.value.selection.selectedCaptureIds)
    }

    @Test
    fun `completed delete does not clear a newer selection generation`() = runTest(testDispatcher) {
        val deleteGate = CompletableDeferred<Unit>()
        coEvery { cardRepository.deleteCards(any()) } coAnswers { deleteGate.await() }
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(200L),
                ),
                storedCard(
                    captureId = 2L,
                    title = "Second",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))
        viewModel.onAction(CollectionAction.DeleteSelected)
        viewModel.onAction(CollectionAction.ConfirmDeleteSelected)
        runCurrent()

        viewModel.onAction(CollectionAction.CancelSelection)
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(2L))
        deleteGate.complete(Unit)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.selection.isActive)
        assertEquals(setOf(2L), viewModel.uiState.value.selection.selectedCaptureIds)
    }

    @Test
    fun `delete selected with no items does not call repository`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)

        viewModel.onAction(CollectionAction.DeleteSelected)
        viewModel.onAction(CollectionAction.ConfirmDeleteSelected)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.selection.showDeleteConfirmDialog)
        coVerify(exactly = 0) { cardRepository.deleteCards(any()) }
    }

    @Test
    fun `updating search query clears selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))

        viewModel.onAction(CollectionAction.UpdateSearchQuery("First"))

        assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
    }

    @Test
    fun `closing detail clears selection`() = runTest(testDispatcher) {
        cardsFlow.emit(
            listOf(
                storedCard(
                    captureId = 1L,
                    title = "First",
                    contentType = ScreenshotContentType.SHOPPING,
                    organizedAt = Instant.ofEpochMilli(100L),
                ),
            ),
        )
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.OpenTypeDetail(ScreenshotContentType.SHOPPING))
        advanceUntilIdle()
        viewModel.onAction(CollectionAction.StartSelection)
        viewModel.onAction(CollectionAction.ToggleItemSelection(1L))

        viewModel.onAction(CollectionAction.CloseDetail)

        assertNull(viewModel.uiState.value.detail)
        assertEquals(CollectionSelectionUiState(), viewModel.uiState.value.selection)
    }

    private fun storedCard(
        captureId: Long,
        title: String = "title-$captureId",
        summary: String = "summary",
        contentType: ScreenshotContentType = ScreenshotContentType.ETC,
        isFavorite: Boolean = false,
        organizedAt: Instant,
    ): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                captureId = captureId,
                typeCode = contentType,
                title = title,
                summary = summary,
                body = "body-$captureId",
                originalImageUrl = "mock://captures/$captureId",
                isFavorite = isFavorite,
                organizedAt = organizedAt,
            ),
            imageRefs = ScreenshotCardImageRefs(sourceImageUri = "content://$captureId"),
            updatedAtMillis = organizedAt.toEpochMilli(),
        )
    }
}
