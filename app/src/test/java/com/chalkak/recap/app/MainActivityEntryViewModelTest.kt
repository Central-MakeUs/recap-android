package com.chalkak.recap.app

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.chalkak.recap.app.share.OnboardingSampleShareIntentContract
import com.chalkak.recap.app.share.OnboardingSampleShareSuccessStore
import com.chalkak.recap.app.share.SharedAnalysisRequest
import com.chalkak.recap.app.share.SharedAnalysisRequestStore
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MainActivityEntryViewModelTest {
    private val store = SharedAnalysisRequestStore()

    @Test
    fun `same request id is consumed only once`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        val request = sampleRequest(requestId = "req-1")
        val prepared = samplePrepared(request.images)
        store.register(request.requestId, prepared)

        val first = viewModel.consumeSharedAnalysisRequest(request)
        val second = viewModel.consumeSharedAnalysisRequest(request)

        assertEquals(prepared, first)
        assertNull(second)
        assertEquals(1, viewModel.pendingHomeNavigationRequestId.value)
    }

    @Test
    fun `different request ids each create a home navigation request`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        val first = sampleRequest(requestId = "req-1")
        val second = sampleRequest(requestId = "req-2")
        store.register(first.requestId, samplePrepared(first.images))
        store.register(second.requestId, samplePrepared(second.images))

        viewModel.consumeSharedAnalysisRequest(first)
        viewModel.consumeSharedAnalysisRequest(second)

        assertEquals(2, viewModel.pendingHomeNavigationRequestId.value)
    }

    @Test
    fun `unregistered request id is rejected`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        val forged = sampleRequest(requestId = "forged-req")

        val consumed = viewModel.consumeSharedAnalysisRequest(forged)

        assertNull(consumed)
        assertNull(viewModel.pendingHomeNavigationRequestId.value)
    }

    @Test
    fun `external forged extras without store registration cannot start analysis`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        val forged = SharedAnalysisRequest(
            requestId = "external-forged",
            images = listOf(
                LocalImage(
                    uri = "content://evil/image.jpg",
                    displayName = "evil.jpg",
                    dateAddedMillis = 99L,
                ),
            ),
        )

        assertNull(viewModel.consumeSharedAnalysisRequest(forged))
        assertNull(store.peek(forged.requestId))
    }

    @Test
    fun `completed home navigation is not restored as pending`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = MainActivityEntryViewModel(savedStateHandle, store)
        viewModel.requestNavigateToHome()
        val requestId = viewModel.pendingHomeNavigationRequestId.value!!

        viewModel.completeHomeNavigation(requestId)
        val recreatedViewModel = MainActivityEntryViewModel(savedStateHandle, store)

        assertNull(recreatedViewModel.pendingHomeNavigationRequestId.value)
    }

    @Test
    fun `new home navigation after completion uses a higher request id`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        viewModel.requestNavigateToHome()
        val firstRequestId = viewModel.pendingHomeNavigationRequestId.value!!
        viewModel.completeHomeNavigation(firstRequestId)

        viewModel.requestNavigateToHome()

        assertEquals(firstRequestId + 1, viewModel.pendingHomeNavigationRequestId.value)
    }

    @Test
    fun `stale completion does not clear a newer home navigation request`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        viewModel.requestNavigateToHome()
        val firstRequestId = viewModel.pendingHomeNavigationRequestId.value!!
        viewModel.requestNavigateToHome()
        val secondRequestId = viewModel.pendingHomeNavigationRequestId.value!!

        viewModel.completeHomeNavigation(firstRequestId)

        assertEquals(secondRequestId, viewModel.pendingHomeNavigationRequestId.value)
    }

    @Test
    fun `onboarding sample share success event is consumed only once`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        val eventId = OnboardingSampleShareSuccessStore.issueEventId()
        val intent = mockk<Intent> {
            every { action } returns OnboardingSampleShareIntentContract.ACTION
            every {
                getStringExtra(OnboardingSampleShareIntentContract.EXTRA_EVENT_ID)
            } returns eventId
        }

        assertTrue(viewModel.consumeOnboardingSampleShareSuccess(intent))
        assertEquals(1, viewModel.pendingOnboardingSampleShareAdvanceRequestId.value)
        assertFalse(viewModel.consumeOnboardingSampleShareSuccess(intent))
        assertEquals(1, viewModel.pendingOnboardingSampleShareAdvanceRequestId.value)
    }

    @Test
    fun `unregistered onboarding sample share success event is rejected`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        val intent = mockk<Intent> {
            every { action } returns OnboardingSampleShareIntentContract.ACTION
            every {
                getStringExtra(OnboardingSampleShareIntentContract.EXTRA_EVENT_ID)
            } returns "forged-event"
        }

        assertFalse(viewModel.consumeOnboardingSampleShareSuccess(intent))
        assertNull(viewModel.pendingOnboardingSampleShareAdvanceRequestId.value)
    }

    @Test
    fun `completed onboarding sample share advance is not restored as pending`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = MainActivityEntryViewModel(savedStateHandle, store)
        viewModel.requestOnboardingSampleShareAdvance()
        val requestId = viewModel.pendingOnboardingSampleShareAdvanceRequestId.value!!

        viewModel.completeOnboardingSampleShareAdvance(requestId)
        val recreatedViewModel = MainActivityEntryViewModel(savedStateHandle, store)

        assertNull(recreatedViewModel.pendingOnboardingSampleShareAdvanceRequestId.value)
    }

    @Test
    fun `new onboarding sample share advance after completion uses a higher request id`() {
        val viewModel = MainActivityEntryViewModel(SavedStateHandle(), store)
        viewModel.requestOnboardingSampleShareAdvance()
        val firstRequestId = viewModel.pendingOnboardingSampleShareAdvanceRequestId.value!!
        viewModel.completeOnboardingSampleShareAdvance(firstRequestId)

        viewModel.requestOnboardingSampleShareAdvance()

        assertEquals(
            firstRequestId + 1,
            viewModel.pendingOnboardingSampleShareAdvanceRequestId.value,
        )
    }

    private fun sampleRequest(requestId: String): SharedAnalysisRequest {
        return SharedAnalysisRequest(
            requestId = requestId,
            images = listOf(
                LocalImage(
                    uri = "content://share/image.jpg",
                    displayName = "image.jpg",
                    dateAddedMillis = 1L,
                ),
            ),
        )
    }

    private fun samplePrepared(images: List<LocalImage>): List<ScreenshotUploadCandidate> {
        return images.map { image ->
            ScreenshotUploadCandidate(
                localImage = image,
                preparedScreenshot = PreparedScreenshot(
                    localImage = image,
                    jpegBytes = byteArrayOf(1, 2, 3),
                ),
                completedPreparationAttempts = 1,
            )
        }
    }
}
