package com.chalkak.recap.feature.screenshot

import app.cash.turbine.test
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
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
        val card = storedCard(imageId = "card-1", title = "제주 숙소")
        viewModel.bind("card-1")
        cardFlow.emit(card)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("제주 숙소", state.card.analysisResult.title)
        assertEquals("제주 숙소", state.editDraft.title)
    }

    @Test
    fun `bind emits not found when card is missing`() = runTest(testDispatcher) {
        viewModel.bind("missing")
        cardFlow.emit(null)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ScreenshotUiState.NotFound)
    }

    @Test
    fun `bind emits load error when observe fails`() = runTest(testDispatcher) {
        every { repository.observeCard("broken") } returns kotlinx.coroutines.flow.flow {
            throw IllegalStateException("db down")
        }

        viewModel.bind("broken")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ScreenshotUiState.LoadError)
    }

    @Test
    fun `discard during save cancels update and does not emit success`() = runTest(testDispatcher) {
        val allowSaveToFinish = CompletableDeferred<Unit>()
        coEvery {
            repository.updateCardContent(
                imageId = any(),
                title = any(),
                summary = any(),
                body = any(),
                primaryContentType = any(),
                updatedAtMillis = any(),
            )
        } coAnswers {
            allowSaveToFinish.await()
            true
        }

        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1", title = "원본"))
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
        coEvery { repository.updateFavorite("card-1", true) } just Runs
        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1", isFavorite = false))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.ToggleFavorite)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.updateFavorite("card-1", true) }
    }

    @Test
    fun `prepare edit draft resets draft from card`() = runTest(testDispatcher) {
        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1", title = "원본 제목", summary = "원본 요약"))
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
        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1", title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("변경"))
        viewModel.onAction(ScreenshotAction.DiscardEditDraft)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("원본", state.editDraft.title)
    }

    @Test
    fun `save edit sends repository values and save succeeded event`() = runTest(testDispatcher) {
        coEvery {
            repository.updateCardContent(
                imageId = "card-1",
                title = "새 제목",
                summary = "새 요약",
                body = "새 본문",
                primaryContentType = ScreenshotContentType.SCHEDULE_RESERVATION,
                updatedAtMillis = any(),
            )
        } returns true

        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.PrepareEditDraft)
        viewModel.onAction(ScreenshotAction.UpdateEditTitle("  새 제목  "))
        viewModel.onAction(ScreenshotAction.UpdateEditSummary("  새 요약  "))
        viewModel.onAction(ScreenshotAction.UpdateEditBody("  새 본문  "))
        viewModel.onAction(
            ScreenshotAction.UpdateEditContentType(ScreenshotContentType.SCHEDULE_RESERVATION),
        )

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.SaveEdit)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.SaveSucceeded, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) {
            repository.updateCardContent(
                imageId = "card-1",
                title = "새 제목",
                summary = "새 요약",
                body = "새 본문",
                primaryContentType = ScreenshotContentType.SCHEDULE_RESERVATION,
                updatedAtMillis = any(),
            )
        }
    }

    @Test
    fun `save edit keeps draft when repository fails`() = runTest(testDispatcher) {
        coEvery {
            repository.updateCardContent(
                imageId = any(),
                title = any(),
                summary = any(),
                body = any(),
                primaryContentType = any(),
                updatedAtMillis = any(),
            )
        } throws IllegalStateException("save failed")

        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1", title = "원본"))
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
    fun `delete success cleans images and emits event`() = runTest(testDispatcher) {
        coEvery { repository.deleteCard("card-1") } just Runs

        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1"))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.DeleteScreenshot)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.DeleteSucceeded, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { repository.deleteCard("card-1") }
        coVerify(exactly = 1) { imageStorage.deleteStoredImages(setOf("card-1")) }
    }

    @Test
    fun `delete failure keeps detail and shows retryable error`() = runTest(testDispatcher) {
        coEvery { repository.deleteCard("card-1") } throws IllegalStateException("delete failed")

        viewModel.bind("card-1")
        cardFlow.emit(storedCard(imageId = "card-1"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.DeleteScreenshot)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.isDeleting)
        assertEquals(R.string.screenshot_detail_delete_error, state.actionErrorMessageResId)
        coVerify(exactly = 0) { imageStorage.deleteStoredImages(any()) }
    }

    private fun storedCard(
        imageId: String,
        title: String = "title-$imageId",
        summary: String = "summary-$imageId",
        body: String = "body-$imageId",
        isFavorite: Boolean = false,
        contentType: ScreenshotContentType = ScreenshotContentType.SHOPPING_PRODUCT,
    ): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                imageId = imageId,
                title = title,
                summary = summary,
                contentTypes = ScreenshotContentTypes(primaryContentType = contentType),
                keyFields = emptyList(),
                confidence = ScreenshotAnalysisConfidence.HIGH,
                isFavorite = isFavorite,
                body = body,
            ),
            imageRefs = ScreenshotCardImageRefs(
                storedImagePath = "/images/$imageId",
            ),
            createdAtMillis = 1_000L,
            updatedAtMillis = 2_000L,
        )
    }
}
