package com.chalkak.recap.feature.screenshot

import app.cash.turbine.test
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenshotViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ScreenshotCardRepository>()
    private val imageStorage = mockk<ScreenshotImageStorage>()
    private val cardFlow = MutableSharedFlow<StoredScreenshotCard?>(replay = 1)
    private lateinit var viewModel: ScreenshotViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.observeCard(any()) } returns cardFlow
        every { imageStorage.deleteStoredImages(any()) } just Runs
        viewModel = ScreenshotViewModel(repository, imageStorage).apply {
            ioDispatcher = testDispatcher
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `bind loads content successfully`() = runTest(testDispatcher) {
        val card = storedCard(captureId = 1L, title = "제주 숙소")
        viewModel.bind(1L)
        cardFlow.emit(card)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("제주 숙소", state.card.analysisResult.title)
        assertEquals("제주 숙소", state.editDraft.title)
    }

    @Test
    fun `bind emits not found when card is missing`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(null)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ScreenshotUiState.NotFound)
    }

    @Test
    fun `bind emits load error when observe fails`() = runTest(testDispatcher) {
        every { repository.observeCard(99L) } returns kotlinx.coroutines.flow.flow {
            throw IllegalStateException("db down")
        }

        viewModel.bind(99L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ScreenshotUiState.LoadError)
    }

    @Test
    fun `discard during save cancels update and does not emit success`() = runTest(testDispatcher) {
        val allowSaveToFinish = CompletableDeferred<Unit>()
        coEvery {
            repository.updateCardContent(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
                updatedAtMillis = any(),
            )
        } coAnswers {
            allowSaveToFinish.await()
            true
        }

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("저장 중 취소"))
        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.SaveEdit)
            testScheduler.runCurrent()

            viewModel.onAction(ScreenshotAction.DiscardEditDraft)
            testScheduler.runCurrent()

            expectNoEvents()
            allowSaveToFinish.complete(Unit)
            testScheduler.runCurrent()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("원본", state.editDraft.title)
        assertFalse(state.isSaving)
    }

    @Test
    fun `toggle favorite updates repository`() = runTest(testDispatcher) {
        coEvery { repository.updateFavorite(1L, true) } just Runs
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, isFavorite = false))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.ToggleFavorite)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.ShowFavoriteToast(isFavorite = true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.updateFavorite(1L, true) }
    }

    @Test
    fun `toggle favorite off sends removed toast event`() = runTest(testDispatcher) {
        coEvery { repository.updateFavorite(1L, false) } just Runs
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, isFavorite = true))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.ToggleFavorite)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.ShowFavoriteToast(isFavorite = false), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.updateFavorite(1L, false) }
    }

    @Test
    fun `prepare edit draft resets draft from card`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본 제목", summary = "원본 요약"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("임시 제목"))
        viewModel.onAction(ScreenshotAction.PrepareEditDraft)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("원본 제목", state.editDraft.title)
        assertEquals("원본 요약", state.editDraft.summary)
        assertFalse(state.titleError)
    }

    @Test
    fun `discard edit draft restores card values`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("변경"))
        viewModel.onAction(ScreenshotAction.DiscardEditDraft)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("원본", state.editDraft.title)
    }

    @Test
    fun `show discard confirm dialog when edit draft has unsaved changes`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("변경"))
        viewModel.onAction(ScreenshotAction.ShowDiscardEditConfirmDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertTrue(state.showDiscardEditConfirmDialog)
        assertTrue(state.hasUnsavedEditChanges())
    }

    @Test
    fun `show discard confirm dialog is ignored when edit draft is unchanged`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.ShowDiscardEditConfirmDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.showDiscardEditConfirmDialog)
        assertFalse(state.hasUnsavedEditChanges())
    }

    @Test
    fun `dismiss discard confirm dialog hides dialog and keeps draft`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("변경"))
        viewModel.onAction(ScreenshotAction.ShowDiscardEditConfirmDialog)
        viewModel.onAction(ScreenshotAction.DismissDiscardEditConfirmDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.showDiscardEditConfirmDialog)
        assertEquals("변경", state.editDraft.title)
    }

    @Test
    fun `discard edit draft hides discard confirm dialog`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("변경"))
        viewModel.onAction(ScreenshotAction.ShowDiscardEditConfirmDialog)
        viewModel.onAction(ScreenshotAction.DiscardEditDraft)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.showDiscardEditConfirmDialog)
        assertEquals("원본", state.editDraft.title)
    }

    @Test
    fun `save edit sends repository values and save succeeded event`() = runTest(testDispatcher) {
        coEvery {
            repository.updateCardContent(
                captureId = 1L,
                title = "새 제목",
                summary = "새 요약",
                body = "새 본문",
                typeCode = ScreenshotContentType.SCHEDULE,
                updatedAtMillis = any(),
            )
        } returns true

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.PrepareEditDraft)
        viewModel.onAction(ScreenshotAction.UpdateEditTitle("  새 제목  "))
        viewModel.onAction(ScreenshotAction.UpdateEditSummary("  새 요약  "))
        viewModel.onAction(ScreenshotAction.UpdateEditBody("  새 본문  "))
        viewModel.onAction(
            ScreenshotAction.UpdateEditContentType(ScreenshotContentType.SCHEDULE),
        )

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.SaveEdit)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.SaveSucceeded, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            repository.updateCardContent(
                captureId = 1L,
                title = "새 제목",
                summary = "새 요약",
                body = "새 본문",
                typeCode = ScreenshotContentType.SCHEDULE,
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `save edit keeps draft when repository fails`() = runTest(testDispatcher) {
        coEvery {
            repository.updateCardContent(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
                updatedAtMillis = any(),
            )
        } throws IllegalStateException("save failed")

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("실패할 제목"))
        viewModel.onAction(ScreenshotAction.SaveEdit)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("실패할 제목", state.editDraft.title)
        assertFalse(state.isSaving)
        assertEquals(R.string.screenshot_edit_save_error, state.actionErrorMessageResId)
    }

    @Test
    fun `update edit title strips newlines`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("a\nb"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("ab", state.editDraft.title)
    }

    @Test
    fun `update edit summary converts newlines to spaces`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditSummary("a\nb"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("a b", state.editDraft.summary)
    }

    @Test
    fun `save edit is ignored when draft is unchanged`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.PrepareEditDraft)
        viewModel.onAction(ScreenshotAction.SaveEdit)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            repository.updateCardContent(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `delete request shows confirm dialog without deleting`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.ShowDeleteConfirmDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertTrue(state.showDeleteConfirmDialog)
        coVerify(exactly = 0) { repository.deleteCard(any()) }
    }

    @Test
    fun `dismiss delete confirm dialog hides dialog`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.ShowDeleteConfirmDialog)
        viewModel.onAction(ScreenshotAction.DismissDeleteConfirmDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.showDeleteConfirmDialog)
    }

    @Test
    fun `delete success cleans images and emits event`() = runTest(testDispatcher) {
        coEvery { repository.deleteCard(1L) } just Runs

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.ShowDeleteConfirmDialog)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.DeleteScreenshot)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.DeleteSucceeded, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.deleteCard(1L) }
        coVerify(exactly = 1) { imageStorage.deleteStoredImages(setOf(1L)) }
    }

    @Test
    fun `delete failure keeps detail and shows retryable error`() = runTest(testDispatcher) {
        coEvery { repository.deleteCard(1L) } throws IllegalStateException("delete failed")

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.DeleteScreenshot)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.isDeleting)
        assertEquals(R.string.screenshot_detail_delete_error, state.actionErrorMessageResId)
        coVerify(exactly = 0) { imageStorage.deleteStoredImages(any()) }
    }

    private fun storedCard(
        captureId: Long,
        title: String = "title-$captureId",
        summary: String = "summary-$captureId",
        body: String = "body-$captureId",
        isFavorite: Boolean = false,
        contentType: ScreenshotContentType = ScreenshotContentType.SHOPPING,
    ): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                captureId = captureId,
                typeCode = contentType,
                title = title,
                summary = summary,
                body = body,
                originalImageUrl = "mock://captures/$captureId",
                isFavorite = isFavorite,
                organizedAt = Instant.ofEpochMilli(1000L),
            ),
            imageRefs = ScreenshotCardImageRefs(
                storedImagePath = "/images/$captureId",
            ),
            updatedAtMillis = 2_000L,
        )
    }
}
