package com.chalkak.recap.core.data.screenshot

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.chalkak.recap.core.data.capture.CaptureRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.model.capture.OrganizeBatch
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.capture.OrganizeStatusDetail
import com.chalkak.recap.core.model.capture.UploadItem
import com.chalkak.recap.core.model.capture.UploadUrls
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.Runs
import io.mockk.unmockkStatic
import io.mockk.verify
import java.io.ByteArrayInputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteScreenshotAnalysisRepositoryTest {
    private val context = mockk<Context>()
    private val contentResolver = mockk<ContentResolver>()
    private val captureRepository = mockk<CaptureRepository>()
    private val changeNotifier = mockk<RemoteCaptureChangeNotifier>()
    private lateinit var repository: RemoteScreenshotAnalysisRepository

    @BeforeEach
    fun setUp() {
        mockkStatic(Uri::class)
        every { context.contentResolver } returns contentResolver
        every { changeNotifier.notifyCaptureChanged() } just Runs
        repository = RemoteScreenshotAnalysisRepository(
            context = context,
            captureRepository = captureRepository,
            changeNotifier = changeNotifier,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `analyze throws UnsupportedOperationException`() = runTest {
        assertThrows<UnsupportedOperationException> {
            repository.analyze(ScreenshotAnalysisInput(fileName = "a.png", uri = "content://1"))
        }
    }

    @Test
    fun `analyze list throws UnsupportedOperationException`() = runTest {
        assertThrows<UnsupportedOperationException> {
            repository.analyze(
                listOf(ScreenshotAnalysisInput(fileName = "a.png", uri = "content://1")),
            )
        }
    }

    @Test
    fun `organize uploads polls acks and notifies on completed`() = runTest {
        stubImage("content://1", byteArrayOf(1, 2, 3))
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(uploads = listOf(UploadItem(imageKey = "key-1", uploadUrl = "https://up/1"))),
        )
        coEvery {
            captureRepository.uploadImage("https://up/1", byteArrayOf(1, 2, 3), "image/jpeg")
        } returns Result.success(Unit)
        coEvery { captureRepository.organize(listOf("key-1")) } returns Result.success(
            OrganizeBatch(batchId = 9L, totalCount = 1, status = OrganizeStatus.PROCESSING),
        )
        coEvery { captureRepository.getOrganizeStatus(9L) } returnsMany listOf(
            Result.success(
                OrganizeStatusDetail(
                    batchId = 9L,
                    status = OrganizeStatus.PROCESSING,
                    totalCount = 1,
                    successCount = 0,
                    failCount = 0,
                ),
            ),
            Result.success(
                OrganizeStatusDetail(
                    batchId = 9L,
                    status = OrganizeStatus.COMPLETED,
                    totalCount = 1,
                    successCount = 1,
                    failCount = 0,
                ),
            ),
        )
        coEvery { captureRepository.ackOrganizeResult(9L) } returns Result.success(Unit)

        val progress = mutableListOf<Pair<Int, Int>>()
        val outcomeDeferred = async {
            repository.organize(
                inputs = listOf(
                    ScreenshotAnalysisInput(fileName = "a.png", uri = "content://1"),
                ),
                onProgress = { completed, total -> progress += completed to total },
            )
        }
        runCurrent()
        advanceTimeBy(1_000.milliseconds)
        runCurrent()
        val outcome = outcomeDeferred.await()

        assertEquals(
            ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 1,
                failCount = 0,
                status = OrganizeStatus.COMPLETED,
            ),
            outcome,
        )
        assertTrue(progress.contains(1 to 1))
        coVerifyOrder {
            captureRepository.issueUploadUrls(1)
            captureRepository.uploadImage("https://up/1", byteArrayOf(1, 2, 3), "image/jpeg")
            captureRepository.organize(listOf("key-1"))
            captureRepository.getOrganizeStatus(9L)
            captureRepository.getOrganizeStatus(9L)
            captureRepository.ackOrganizeResult(9L)
        }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `organize throws when status is failed after ack`() = runTest {
        stubImage("content://1", byteArrayOf(9))
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(uploads = listOf(UploadItem(imageKey = "key-1", uploadUrl = "https://up/1"))),
        )
        coEvery {
            captureRepository.uploadImage(any(), any(), any())
        } returns Result.success(Unit)
        coEvery { captureRepository.organize(any()) } returns Result.success(
            OrganizeBatch(batchId = 3L, totalCount = 1, status = OrganizeStatus.PROCESSING),
        )
        coEvery { captureRepository.getOrganizeStatus(3L) } returns Result.success(
            OrganizeStatusDetail(
                batchId = 3L,
                status = OrganizeStatus.FAILED,
                totalCount = 1,
                successCount = 0,
                failCount = 1,
            ),
        )
        coEvery { captureRepository.ackOrganizeResult(3L) } returns Result.success(Unit)

        val error = assertThrows<RemoteOrganizeFailedException> {
            repository.organize(
                inputs = listOf(
                    ScreenshotAnalysisInput(fileName = "a.png", uri = "content://1"),
                ),
            )
        }

        assertEquals(OrganizeStatus.FAILED, error.status)
        coVerify(exactly = 1) { captureRepository.ackOrganizeResult(3L) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `empty organize returns completed without network`() = runTest {
        val outcome = repository.organize(emptyList())

        assertEquals(
            ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 0,
                failCount = 0,
                status = OrganizeStatus.COMPLETED,
            ),
            outcome,
        )
        coVerify(exactly = 0) { captureRepository.issueUploadUrls(any()) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    private fun stubImage(uriString: String, bytes: ByteArray) {
        val uri = mockk<Uri>()
        every { Uri.parse(uriString) } returns uri
        every { contentResolver.openInputStream(uri) } answers {
            ByteArrayInputStream(bytes)
        }
        every { contentResolver.getType(uri) } returns "image/jpeg"
    }
}

class ScreenshotAnalysisRunStateTest {
    @Test
    fun `begin and end restore idle state`() {
        val runState = ScreenshotAnalysisRunState()

        assertFalse(runState.isRunning.value)
        runState.beginRun()
        assertTrue(runState.isRunning.value)
        runState.endRun()
        assertFalse(runState.isRunning.value)
    }

    @Test
    fun `overlapping runs stay running until last end`() {
        val runState = ScreenshotAnalysisRunState()

        runState.beginRun()
        runState.beginRun()
        runState.endRun()
        assertTrue(runState.isRunning.value)

        runState.endRun()
        assertFalse(runState.isRunning.value)
    }

    @Test
    fun `extra endRun keeps idle without going negative`() {
        val runState = ScreenshotAnalysisRunState()

        runState.endRun()
        assertFalse(runState.isRunning.value)

        runState.beginRun()
        runState.endRun()
        runState.endRun()
        assertFalse(runState.isRunning.value)
    }
}
