package com.chalkak.recap.app.share

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.core.model.observability.PerformanceTrace
import com.chalkak.recap.core.model.observability.PerformanceTracer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareIntakeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val contentResolver = mockk<ContentResolver>()
    private val context = mockk<Context> {
        every { contentResolver } returns this@ShareIntakeViewModelTest.contentResolver
        every { packageName } returns "com.chalkak.recap"
    }
    private val sessionTokenStore = mockk<SessionTokenStore>()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val sharedAnalysisRequestStore = SharedAnalysisRequestStore()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { sessionTokenStore.getRefreshToken() } returns "refresh-token"
        coEvery { userPreferencesRepository.setOnboardingCompleted(any()) } returns Unit
        every { userPreferencesRepository.onboardingCompleted } returns flowOf(true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `same restored intent is not parsed again after completion`() = runTest(testDispatcher) {
        val intent = mockk<Intent>()
        var parseCount = 0
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = {
                parseCount += 1
                sampleParseResult()
            },
        )

        viewModel.submitShareIntent(intent)
        assertEquals(0, parseCount)
        advanceUntilIdle()
        val firstSessionId = viewModel.pendingShareIntake.value?.sessionId
        viewModel.completePendingShareIntake(firstSessionId!!)
        viewModel.submitShareIntent(intent)
        advanceUntilIdle()

        assertEquals(1, parseCount)
        assertNull(viewModel.pendingShareIntake.value)
    }

    @Test
    fun `new intent delivery with same content creates a new session`() = runTest(testDispatcher) {
        val intent = mockk<Intent>()
        var parseCount = 0
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = {
                parseCount += 1
                sampleParseResult()
            },
        )

        viewModel.submitShareIntent(intent)
        advanceUntilIdle()
        val firstSessionId = viewModel.pendingShareIntake.value?.sessionId
        viewModel.completePendingShareIntake(firstSessionId!!)
        viewModel.submitShareIntent(intent, forceNewSession = true)
        advanceUntilIdle()

        assertEquals(2, parseCount)
        assertNotEquals(firstSessionId, viewModel.pendingShareIntake.value?.sessionId)
    }

    @Test
    fun `completion for stale session does not clear newer share`() = runTest(testDispatcher) {
        val intent = mockk<Intent>()
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
        )

        viewModel.submitShareIntent(intent)
        advanceUntilIdle()
        val current = viewModel.pendingShareIntake.value
        viewModel.completePendingShareIntake("stale-session")

        assertEquals(current, viewModel.pendingShareIntake.value)
    }

    @Test
    fun `completion stops the active share trace as cancelled`() = runTest(testDispatcher) {
        val performanceTracer = RecordingPerformanceTracer()
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
            performanceTracer = performanceTracer,
        )

        viewModel.submitShareIntent(mockk())
        advanceUntilIdle()
        val sessionId = viewModel.pendingShareIntake.value?.sessionId
        val trace = performanceTracer.traces.single()
        assertFalse(trace.isStopped)

        viewModel.completePendingShareIntake(sessionId!!)

        assertTrue(trace.isStopped)
        assertEquals("discard", trace.attributes["gate"])
        assertEquals("cancel", trace.attributes["outcome"])
    }

    @Test
    fun `logged out share entry emits login required without confirmation`() = runTest(testDispatcher) {
        coEvery { sessionTokenStore.getRefreshToken() } returns null
        val intent = mockk<Intent>()
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
        )

        viewModel.events.test {
            viewModel.submitShareIntent(intent)
            advanceUntilIdle()

            assertEquals(ShareIntakeEvent.LoginRequired, awaitItem())
            assertNull(viewModel.pendingShareIntake.value)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login required share entry keeps onboarding completed flag`() = runTest(testDispatcher) {
        coEvery { sessionTokenStore.getRefreshToken() } returns null
        val intent = mockk<Intent>()
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
        )

        viewModel.submitShareIntent(intent)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            userPreferencesRepository.setOnboardingCompleted(any())
        }
    }

    @Test
    fun `incomplete onboarding share entry emits onboarding required without confirmation`() =
        runTest(testDispatcher) {
            every { userPreferencesRepository.onboardingCompleted } returns flowOf(false)
            val intent = mockk<Intent>()
            val viewModel = createViewModel(
                fingerprint = "share-fingerprint",
                parseResult = ::sampleParseResult,
            )

            viewModel.events.test {
                viewModel.submitShareIntent(intent)
                advanceUntilIdle()

                assertEquals(ShareIntakeEvent.OnboardingRequired, awaitItem())
                assertNull(viewModel.pendingShareIntake.value)
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `logged in start emits launch event once`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
        )
        val images = sampleParseResult().accepted
        val prepared = samplePrepared(images)

        viewModel.events.test {
            viewModel.requestStartOrganize(prepared)
            viewModel.requestStartOrganize(prepared)
            advanceUntilIdle()

            val event = awaitItem() as ShareIntakeEvent.LaunchMainAnalysis
            assertEquals(images, event.images)
            assertTrue(event.requestId.isNotBlank())
            assertNotNull(sharedAnalysisRequestStore.peek(event.requestId))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logged out completion emits login required without launch`() = runTest(testDispatcher) {
        val intent = mockk<Intent>()
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
        )

        viewModel.submitShareIntent(intent)
        advanceUntilIdle()
        coEvery { sessionTokenStore.getRefreshToken() } returns null

        viewModel.events.test {
            viewModel.requestStartOrganize(samplePrepared(sampleParseResult().accepted))
            advanceUntilIdle()

            assertEquals(ShareIntakeEvent.LoginRequired, awaitItem())
            assertNull(viewModel.pendingShareIntake.value)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `incomplete onboarding completion emits onboarding required without launch`() =
        runTest(testDispatcher) {
            val intent = mockk<Intent>()
            val viewModel = createViewModel(
                fingerprint = "share-fingerprint",
                parseResult = ::sampleParseResult,
            )

            viewModel.submitShareIntent(intent)
            advanceUntilIdle()
            every { userPreferencesRepository.onboardingCompleted } returns flowOf(false)

            viewModel.events.test {
                viewModel.requestStartOrganize(samplePrepared(sampleParseResult().accepted))
                advanceUntilIdle()

                assertEquals(ShareIntakeEvent.OnboardingRequired, awaitItem())
                assertNull(viewModel.pendingShareIntake.value)
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `launch event remains available after collector reconnect`() = runTest(testDispatcher) {
        val viewModel = createViewModel(
            fingerprint = "share-fingerprint",
            parseResult = ::sampleParseResult,
        )
        val images = sampleParseResult().accepted

        viewModel.requestStartOrganize(samplePrepared(images))
        advanceUntilIdle()

        viewModel.events.test {
            val event = awaitItem() as ShareIntakeEvent.LaunchMainAnalysis
            assertEquals(images, event.images)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onboarding sample share emits return event without confirmation`() = runTest(testDispatcher) {
        coEvery { sessionTokenStore.getRefreshToken() } returns null
        every { userPreferencesRepository.onboardingCompleted } returns flowOf(false)
        val intent = mockk<Intent>()
        val viewModel = createViewModel(
            fingerprint = "onboarding-sample-fingerprint",
            parseResult = ::onboardingSampleParseResult,
        )

        viewModel.events.test {
            viewModel.submitShareIntent(intent)
            advanceUntilIdle()

            assertEquals(ShareIntakeEvent.ReturnAfterOnboardingSampleShare, awaitItem())
            assertNull(viewModel.pendingShareIntake.value)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        fingerprint: String,
        parseResult: () -> ShareImageParseResult,
        performanceTracer: PerformanceTracer = PerformanceTracer.NoOp,
    ): ShareIntakeViewModel {
        return ShareIntakeViewModel(
            savedStateHandle = SavedStateHandle(),
            sessionTokenStore = sessionTokenStore,
            userPreferencesRepository = userPreferencesRepository,
            sharedAnalysisRequestStore = sharedAnalysisRequestStore,
            crashReporter = com.chalkak.recap.core.model.observability.CrashReporter.NoOp,
            performanceTracer = performanceTracer,
            context = context,
        ).apply {
            ioDispatcher = testDispatcher
            copyIntent = { intent -> intent }
            fingerprintIntent = { fingerprint }
            parseIntent = { parseResult() }
        }
    }

    private fun sampleParseResult(): ShareImageParseResult {
        return ShareImageParseResult(
            accepted = listOf(
                LocalImage(
                    uri = "content://share/image.jpg",
                    displayName = "image.jpg",
                    dateAddedMillis = 1L,
                ),
            ),
            rejectedCount = 0,
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

    private fun onboardingSampleParseResult(): ShareImageParseResult {
        return ShareImageParseResult(
            accepted = listOf(
                LocalImage(
                    uri = "content://com.chalkak.recap.fileprovider/onboarding_share/onboarding_add_to_favorite_share.png",
                    displayName = "onboarding_add_to_favorite_share.png",
                    dateAddedMillis = 1L,
                ),
            ),
            rejectedCount = 0,
        )
    }

    private class RecordingPerformanceTracer : PerformanceTracer {
        val traces = mutableListOf<RecordingPerformanceTrace>()

        override fun startTrace(name: String): PerformanceTrace =
            RecordingPerformanceTrace().also(traces::add)
    }

    private class RecordingPerformanceTrace : PerformanceTrace {
        val attributes = mutableMapOf<String, String>()
        var isStopped = false

        override fun putAttribute(key: String, value: String) {
            attributes[key] = value
        }

        override fun stop() {
            isStopped = true
        }
    }
}
