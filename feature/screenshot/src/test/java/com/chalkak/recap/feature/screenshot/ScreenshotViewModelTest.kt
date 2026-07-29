package com.chalkak.recap.feature.screenshot

import app.cash.turbine.test
import com.chalkak.recap.core.data.capture.CaptureMutationRepository
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotDetailRepository
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.model.capture.CaptureDeleteResult
import com.chalkak.recap.core.model.capture.ReportReason
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
    private val detailRepository = mockk<ScreenshotDetailRepository>()
    private val captureMutationRepository = mockk<CaptureMutationRepository>()
    private val cardFlow = MutableSharedFlow<StoredScreenshotCard?>(replay = 1)
    private lateinit var viewModel: ScreenshotViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { detailRepository.observeCard(any()) } returns cardFlow
        viewModel = ScreenshotViewModel(
            screenshotDetailRepository = detailRepository,
            captureMutationRepository = captureMutationRepository,
        ).apply {
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
        every { detailRepository.observeCard(99L) } returns kotlinx.coroutines.flow.flow {
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
            captureMutationRepository.updateCapture(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
            )
        } coAnswers {
            allowSaveToFinish.await()
            Result.success(Unit)
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
    fun `toggle favorite updates content and emits toast`() = runTest(testDispatcher) {
        val allowFavoriteToFinish = CompletableDeferred<Unit>()
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 1L, isFavorite = true)
        } coAnswers {
            allowFavoriteToFinish.await()
            Result.success(Unit)
        }

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, isFavorite = false))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ScreenshotAction.ToggleFavorite)

            val optimistic = viewModel.uiState.value as ScreenshotUiState.Content
            assertTrue(optimistic.card.analysisResult.isFavorite)
            assertTrue(optimistic.isFavoriteUpdating)

            allowFavoriteToFinish.complete(Unit)
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.ShowFavoriteToast(isFavorite = true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertTrue(state.card.analysisResult.isFavorite)
        assertFalse(state.isFavoriteUpdating)
    }

    @Test
    fun `toggle favorite failure keeps previous state and shows error`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.updateFavorite(captureId = 1L, isFavorite = false)
        } returns Result.failure(IllegalStateException("network"))

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, isFavorite = true))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.ToggleFavorite)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertTrue(state.card.analysisResult.isFavorite)
        assertFalse(state.isFavoriteUpdating)
        assertEquals(R.string.screenshot_detail_favorite_error, state.actionErrorMessageResId)
    }

    @Test
    fun `show discard edit confirm dialog when draft dirty`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본", summary = "원본 요약"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("변경"))
        viewModel.onAction(ScreenshotAction.ShowDiscardEditConfirmDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertTrue(state.showDiscardEditConfirmDialog)
    }

    @Test
    fun `dismiss discard edit confirm dialog hides dialog`() = runTest(testDispatcher) {
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
            captureMutationRepository.updateCapture(
                captureId = 1L,
                title = "새 제목",
                summary = "새 요약",
                body = "새 본문",
                typeCode = ScreenshotContentType.SCHEDULE,
            )
        } returns Result.success(Unit)

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
            captureMutationRepository.updateCapture(
                captureId = 1L,
                title = "새 제목",
                summary = "새 요약",
                body = "새 본문",
                typeCode = ScreenshotContentType.SCHEDULE,
            )
        }
    }

    @Test
    fun `save edit keeps draft when repository fails`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.updateCapture(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
            )
        } returns Result.failure(IllegalStateException("save failed"))

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
    fun `save edit shows save error when update returns failure`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.updateCapture(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
            )
        } returns Result.failure(IllegalStateException("not found"))

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("원격 미지원"))
        viewModel.onAction(ScreenshotAction.SaveEdit)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.isSaving)
        assertEquals(R.string.screenshot_edit_save_error, state.actionErrorMessageResId)
        assertTrue(viewModel.uiState.value is ScreenshotUiState.Content)
    }

    @Test
    fun `update edit title strips newlines`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("줄\n바꿈"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("줄바꿈", state.editDraft.title)
    }

    @Test
    fun `update edit summary strips newlines`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditSummary("요약\r\n줄"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertEquals("요약 줄", state.editDraft.summary)
    }

    @Test
    fun `empty title after trim marks title error and skips save`() = runTest(testDispatcher) {
        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L, title = "원본"))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.UpdateEditTitle("   "))
        viewModel.onAction(ScreenshotAction.SaveEdit)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertTrue(state.titleError)
        coVerify(exactly = 0) {
            captureMutationRepository.updateCapture(
                captureId = any(),
                title = any(),
                summary = any(),
                body = any(),
                typeCode = any(),
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
        coVerify(exactly = 0) { captureMutationRepository.deleteCaptures(any()) }
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
    fun `delete success emits event via capture mutation repository`() = runTest(testDispatcher) {
        coEvery { captureMutationRepository.deleteCaptures(setOf(1L)) } returns Result.success(
            CaptureDeleteResult(deletedIds = setOf(1L), failedIds = emptySet()),
        )

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

        coVerify(exactly = 1) { captureMutationRepository.deleteCaptures(setOf(1L)) }
    }

    @Test
    fun `delete failure keeps detail and shows retryable error`() = runTest(testDispatcher) {
        coEvery { captureMutationRepository.deleteCaptures(setOf(1L)) } returns Result.failure(
            IllegalStateException("delete failed"),
        )

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(ScreenshotAction.DeleteScreenshot)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.isDeleting)
        assertEquals(R.string.screenshot_detail_delete_error, state.actionErrorMessageResId)
    }

    @Test
    fun `submit report success emits ReportSucceeded`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.report(
                captureId = 1L,
                reason = ReportReason.SENSITIVE_INFO,
                detail = null,
            )
        } returns Result.success(Unit)

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(
                ScreenshotAction.SubmitReport(reason = ReportReason.SENSITIVE_INFO),
            )
            advanceUntilIdle()

            assertEquals(ScreenshotEvent.ReportSucceeded, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value as ScreenshotUiState.Content
        assertFalse(state.isReporting)
        coVerify(exactly = 1) {
            captureMutationRepository.report(
                captureId = 1L,
                reason = ReportReason.SENSITIVE_INFO,
                detail = null,
            )
        }
    }

    @Test
    fun `submit report other sends trimmed detail`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.report(
                captureId = 1L,
                reason = ReportReason.OTHER,
                detail = "상세",
            )
        } returns Result.success(Unit)

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.onAction(
            ScreenshotAction.SubmitReport(
                reason = ReportReason.OTHER,
                detail = "  상세  ",
            ),
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            captureMutationRepository.report(
                captureId = 1L,
                reason = ReportReason.OTHER,
                detail = "상세",
            )
        }
    }

    @Test
    fun `submit report already reported emits ReportFailed`() = runTest(testDispatcher) {
        coEvery {
            captureMutationRepository.report(any(), any(), any())
        } returns Result.failure(
            RemoteApiException(code = "ALREADY_REPORTED", message = "already"),
        )

        viewModel.bind(1L)
        cardFlow.emit(storedCard(captureId = 1L))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(
                ScreenshotAction.SubmitReport(reason = ReportReason.INACCURATE_CONTENT),
            )
            advanceUntilIdle()

            assertEquals(
                ScreenshotEvent.ReportFailed(
                    messageResId = R.string.screenshot_report_already_reported_toast,
                    dismissSheet = true,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
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
