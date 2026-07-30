package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.data.capture.CaptureRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.screenshot.image.ScreenshotUploadPreparer
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.LocalImage
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
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteScreenshotAnalysisRepositoryTest {
    private val captureRepository = mockk<CaptureRepository>()
    private val changeNotifier = mockk<RemoteCaptureChangeNotifier>()
    private val screenshotUploadPreparer = mockk<ScreenshotUploadPreparer>()
    private lateinit var repository: RemoteScreenshotAnalysisRepository

    @BeforeEach
    fun setUp() {
        every { changeNotifier.notifyCaptureChanged() } just Runs
        repository = RemoteScreenshotAnalysisRepository(
            captureRepository = captureRepository,
            changeNotifier = changeNotifier,
            screenshotUploadPreparer = screenshotUploadPreparer,
        )
    }

    @Test
    fun `analyze throws UnsupportedOperationException`() = runTest {
        assertThrows<UnsupportedOperationException> {
            repository.analyze(
                ScreenshotAnalysisInput(
                    fileName = "a.png",
                    uri = "content://1",
                    jpegBytes = byteArrayOf(1),
                ),
            )
        }
    }

    @Test
    fun `analyze list throws UnsupportedOperationException`() = runTest {
        assertThrows<UnsupportedOperationException> {
            repository.analyze(
                listOf(
                    ScreenshotAnalysisInput(
                        fileName = "a.png",
                        uri = "content://1",
                        jpegBytes = byteArrayOf(1),
                    ),
                ),
            )
        }
    }

    @Test
    fun `organize uploads prepared jpeg bytes with image jpeg content type`() = runTest {
        val jpegBytes = byteArrayOf(1, 2, 3)
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(uploads = listOf(UploadItem(imageKey = "key-1", uploadUrl = "https://up/1"))),
        )
        coEvery {
            captureRepository.uploadImage(
                "https://up/1",
                jpegBytes,
                PreparedScreenshot.MIME_TYPE_JPEG,
            )
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
                    ScreenshotAnalysisInput(
                        fileName = "a.png",
                        uri = "content://1",
                        jpegBytes = jpegBytes,
                        contentType = PreparedScreenshot.MIME_TYPE_JPEG,
                    ),
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
            captureRepository.uploadImage(
                "https://up/1",
                jpegBytes,
                PreparedScreenshot.MIME_TYPE_JPEG,
            )
            captureRepository.organize(listOf("key-1"))
            captureRepository.getOrganizeStatus(9L)
            captureRepository.getOrganizeStatus(9L)
            captureRepository.ackOrganizeResult(9L)
        }
        verify(exactly = 1) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `organize fails when prepared jpeg bytes are missing`() = runTest {
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(uploads = listOf(UploadItem(imageKey = "key-1", uploadUrl = "https://up/1"))),
        )

        val error = assertThrows<RemoteApiException> {
            repository.organize(
                inputs = listOf(
                    ScreenshotAnalysisInput(
                        fileName = "a.png",
                        uri = "content://1",
                        jpegBytes = null,
                    ),
                ),
            )
        }

        assertEquals("PREPARED_IMAGE_MISSING", error.code)
        coVerify(exactly = 0) { captureRepository.uploadImage(any(), any(), any()) }
    }

    @Test
    fun `organize fails when prepared jpeg bytes are empty`() = runTest {
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(uploads = listOf(UploadItem(imageKey = "key-1", uploadUrl = "https://up/1"))),
        )

        val error = assertThrows<RemoteApiException> {
            repository.organize(
                inputs = listOf(
                    ScreenshotAnalysisInput(
                        fileName = "a.png",
                        uri = "content://1",
                        jpegBytes = byteArrayOf(),
                    ),
                ),
            )
        }

        assertEquals("PREPARED_IMAGE_MISSING", error.code)
        coVerify(exactly = 0) { captureRepository.uploadImage(any(), any(), any()) }
    }

    @Test
    fun `organize throws when status is failed after ack`() = runTest {
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
                    ScreenshotAnalysisInput(
                        fileName = "a.png",
                        uri = "content://1",
                        jpegBytes = byteArrayOf(9),
                    ),
                ),
            )
        }

        assertEquals(OrganizeStatus.FAILED, error.status)
        coVerify(exactly = 1) { captureRepository.ackOrganizeResult(3L) }
        verify(exactly = 0) { changeNotifier.notifyCaptureChanged() }
    }

    @Test
    fun `organize uploads preprepared input before pending input and keeps image key order`() =
        runTest {
            val pendingImage = LocalImage("content://pending", "pending.png", 1L)
            val pendingBytes = byteArrayOf(7, 7)
            coEvery { captureRepository.issueUploadUrls(2) } returns Result.success(
                UploadUrls(
                    uploads = listOf(
                        UploadItem("key-pending", "https://up/pending"),
                        UploadItem("key-ready", "https://up/ready"),
                    ),
                ),
            )
            coEvery { screenshotUploadPreparer.prepare(pendingImage) } returns
                PreparedScreenshot(pendingImage, pendingBytes)
            coEvery { captureRepository.uploadImage(any(), any(), any()) } returns
                Result.success(Unit)
            coEvery {
                captureRepository.organize(listOf("key-pending", "key-ready"))
            } returns Result.success(
                OrganizeBatch(11L, 2, OrganizeStatus.PROCESSING),
            )
            coEvery { captureRepository.getOrganizeStatus(11L) } returns Result.success(
                OrganizeStatusDetail(
                    batchId = 11L,
                    status = OrganizeStatus.COMPLETED,
                    totalCount = 2,
                    successCount = 2,
                    failCount = 0,
                ),
            )
            coEvery { captureRepository.ackOrganizeResult(11L) } returns Result.success(Unit)

            repository.organize(
                listOf(
                    ScreenshotAnalysisInput(
                        fileName = pendingImage.displayName,
                        uri = pendingImage.uri,
                        localImage = pendingImage,
                    ),
                    ScreenshotAnalysisInput(
                        fileName = "ready.png",
                        uri = "content://ready",
                        jpegBytes = byteArrayOf(8, 8),
                    ),
                ),
            )

            coVerifyOrder {
                captureRepository.uploadImage(
                    "https://up/ready",
                    byteArrayOf(8, 8),
                    PreparedScreenshot.MIME_TYPE_JPEG,
                )
                screenshotUploadPreparer.prepare(pendingImage)
                captureRepository.uploadImage(
                    "https://up/pending",
                    pendingBytes,
                    PreparedScreenshot.MIME_TYPE_JPEG,
                )
                captureRepository.organize(listOf("key-pending", "key-ready"))
            }
        }

    @Test
    fun `organize retries preparation only once after confirmation failure`() = runTest {
        val image = LocalImage("content://failed", "failed.png", 1L)
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(listOf(UploadItem("unused", "https://up/unused"))),
        )
        coEvery { screenshotUploadPreparer.prepare(image) } throws
            IllegalStateException("decode failed")

        val outcome = repository.organize(
            listOf(
                ScreenshotAnalysisInput(
                    fileName = image.displayName,
                    uri = image.uri,
                    localImage = image,
                    completedPreparationAttempts = 1,
                ),
            ),
        )

        assertEquals(
            ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 0,
                failCount = 1,
                status = OrganizeStatus.FAILED,
            ),
            outcome,
        )
        coVerify(exactly = 1) { screenshotUploadPreparer.prepare(image) }
        coVerify(exactly = 0) { captureRepository.organize(any()) }
    }

    @Test
    fun `upload does not emit analysis progress`() = runTest {
        val statusGate = CompletableDeferred<Unit>()
        coEvery { captureRepository.issueUploadUrls(1) } returns Result.success(
            UploadUrls(listOf(UploadItem("key-1", "https://up/1"))),
        )
        coEvery { captureRepository.uploadImage(any(), any(), any()) } returns Result.success(Unit)
        coEvery { captureRepository.organize(listOf("key-1")) } returns Result.success(
            OrganizeBatch(12L, 1, OrganizeStatus.PROCESSING),
        )
        coEvery { captureRepository.getOrganizeStatus(12L) } coAnswers {
            statusGate.await()
            Result.success(
                OrganizeStatusDetail(
                    batchId = 12L,
                    status = OrganizeStatus.COMPLETED,
                    totalCount = 1,
                    successCount = 1,
                    failCount = 0,
                ),
            )
        }
        coEvery { captureRepository.ackOrganizeResult(12L) } returns Result.success(Unit)
        val progress = mutableListOf<Pair<Int, Int>>()

        val result = async {
            repository.organize(
                listOf(
                    ScreenshotAnalysisInput(
                        fileName = "ready.png",
                        jpegBytes = byteArrayOf(1),
                    ),
                ),
            ) { completed, total -> progress += completed to total }
        }
        runCurrent()

        assertTrue(progress.isEmpty())
        statusGate.complete(Unit)
        runCurrent()
        result.await()
        assertEquals(listOf(1 to 1), progress)
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
