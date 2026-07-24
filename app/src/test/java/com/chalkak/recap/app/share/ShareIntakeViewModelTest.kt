package com.chalkak.recap.app.share

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.chalkak.recap.core.model.LocalImage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareIntakeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val contentResolver = mockk<ContentResolver>()
    private val context = mockk<Context> {
        every { contentResolver } returns this@ShareIntakeViewModelTest.contentResolver
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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

    private fun createViewModel(
        fingerprint: String,
        parseResult: () -> ShareImageParseResult,
    ): ShareIntakeViewModel {
        return ShareIntakeViewModel(
            savedStateHandle = SavedStateHandle(),
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
}
