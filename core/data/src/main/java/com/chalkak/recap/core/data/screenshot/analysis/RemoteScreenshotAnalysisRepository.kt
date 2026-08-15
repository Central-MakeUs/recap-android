package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.data.capture.CaptureRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.screenshot.image.ScreenshotUploadPreparer
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.capture.OrganizeStatusDetail
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.ObservabilityKeys
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

class RemoteOrganizeFailedException(
    val status: OrganizeStatus,
    message: String,
) : Exception(message)

@Singleton
class RemoteScreenshotAnalysisRepository @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val changeNotifier: RemoteCaptureChangeNotifier,
    private val screenshotUploadPreparer: ScreenshotUploadPreparer,
    private val crashReporter: CrashReporter,
) : ScreenshotAnalysisRepository {
    override suspend fun analyze(input: ScreenshotAnalysisInput): ScreenshotAnalysisResult {
        throw UnsupportedOperationException("Remote analyze requires organize()")
    }

    override suspend fun analyze(
        inputs: List<ScreenshotAnalysisInput>,
    ): List<ScreenshotAnalysisResult> {
        throw UnsupportedOperationException("Remote analyze requires organize()")
    }

    override suspend fun organize(
        inputs: List<ScreenshotAnalysisInput>,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): ScreenshotOrganizeOutcome {
        val total = inputs.size
        if (total == 0) {
            onProgress(0, 0)
            return ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 0,
                failCount = 0,
                status = OrganizeStatus.COMPLETED,
            )
        }

        // presigned URL 발급
        val uploadUrls = captureRepository.issueUploadUrls(count = total).getOrThrow()
        if (uploadUrls.uploads.size != total) {
            val mismatch = RemoteApiException(
                code = "UPLOAD_URL_MISMATCH",
                message = "Expected $total upload URLs but received ${uploadUrls.uploads.size}",
            )
            crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "presign")
            crashReporter.recordException(mismatch)
            throw mismatch
        }

        crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "upload")

        // Confirmation에서 준비된 항목을 먼저 업로드한다.
        val uploadedImageKeys = ArrayList<Pair<Int, String>>(total)
        var preparationFailCount = 0
        inputs.withIndex()
            .filter { (_, input) -> input.jpegBytes?.isNotEmpty() == true }
            .forEach { (index, input) ->
                val upload = uploadUrls.uploads[index]
                val bytes = requirePreparedJpegBytes(input)
                captureRepository.uploadImage(
                    uploadUrl = upload.uploadUrl,
                    bytes = bytes,
                    contentType = PreparedScreenshot.MIME_TYPE_JPEG,
                ).getOrThrow()
                uploadedImageKeys += index to upload.imageKey
            }

        // 아직 준비되지 않은 항목은 선택 순서대로 압축하고 성공 즉시 업로드한다.
        inputs.withIndex()
            .filter { (_, input) -> input.jpegBytes?.isNotEmpty() != true }
            .forEach { (index, input) ->
                val prepared = prepareWithRetry(input)
                if (prepared == null) {
                    preparationFailCount += 1
                    return@forEach
                }
                val upload = uploadUrls.uploads[index]
                captureRepository.uploadImage(
                    uploadUrl = upload.uploadUrl,
                    bytes = prepared.jpegBytes,
                    contentType = PreparedScreenshot.MIME_TYPE_JPEG,
                ).getOrThrow()
                uploadedImageKeys += index to upload.imageKey
            }

        val imageKeys = uploadedImageKeys
            .sortedBy { (index, _) -> index }
            .map { (_, imageKey) -> imageKey }
        if (imageKeys.isEmpty()) {
            return ScreenshotOrganizeOutcome.RemoteCompleted(
                successCount = 0,
                failCount = preparationFailCount,
                status = OrganizeStatus.FAILED,
            )
        }

        // 분석 시작
        crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "organize")
        val batch = captureRepository.organize(imageKeys).getOrThrow()
        // 1초 단위 status 폴링
        crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "poll")
        val finalStatus = pollUntilTerminal(
            batchId = batch.batchId,
            fallbackTotal = batch.totalCount.coerceAtLeast(imageKeys.size),
            onProgress = onProgress,
        )

        // 결과 처리
        when (finalStatus.status) {
            OrganizeStatus.COMPLETED,
            OrganizeStatus.PARTIAL_FAILED,
            -> {
                crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "ack")
                captureRepository.ackOrganizeResult(finalStatus.batchId)
                    .onFailure { error ->
                        Timber.w(error, "Failed to ack organize result batchId=%s", finalStatus.batchId)
                    }
                changeNotifier.notifyCaptureChanged()
                return ScreenshotOrganizeOutcome.RemoteCompleted(
                    successCount = finalStatus.successCount,
                    failCount = finalStatus.failCount + preparationFailCount,
                    status = if (
                        preparationFailCount > 0 &&
                        finalStatus.status == OrganizeStatus.COMPLETED
                    ) {
                        OrganizeStatus.PARTIAL_FAILED
                    } else {
                        finalStatus.status
                    },
                )
            }

            OrganizeStatus.FAILED,
            OrganizeStatus.CANCELLED,
            -> {
                crashReporter.setCustomKey(ObservabilityKeys.ORGANIZE_PHASE, "ack")
                captureRepository.ackOrganizeResult(finalStatus.batchId)
                    .onFailure { error ->
                        Timber.w(error, "Failed to ack organize result batchId=%s", finalStatus.batchId)
                    }
                val failed = RemoteOrganizeFailedException(
                    status = finalStatus.status,
                    message = "Remote organize finished with status=${finalStatus.status}",
                )
                if (finalStatus.status == OrganizeStatus.FAILED) {
                    crashReporter.recordException(failed)
                }
                throw failed
            }

            OrganizeStatus.PROCESSING -> {
                throw RemoteOrganizeFailedException(
                    status = OrganizeStatus.PROCESSING,
                    message = "Remote organize polling ended while still processing",
                )
            }
        }
    }

    private fun requirePreparedJpegBytes(input: ScreenshotAnalysisInput): ByteArray {
        val bytes = input.jpegBytes
        if (bytes == null || bytes.isEmpty()) {
            throw RemoteApiException(
                code = "PREPARED_IMAGE_MISSING",
                message = "Prepared JPEG bytes missing for uri=${input.uri}",
            )
        }
        return bytes
    }

    private suspend fun prepareWithRetry(
        input: ScreenshotAnalysisInput,
    ): PreparedScreenshot? {
        val image = input.localImage ?: throw RemoteApiException(
            code = "PREPARED_IMAGE_MISSING",
            message = "Prepared JPEG bytes and local image missing for uri=${input.uri}",
        )
        val remainingAttempts = (
            ScreenshotUploadCandidate.MAX_PREPARATION_ATTEMPTS -
                input.completedPreparationAttempts
            ).coerceAtLeast(0)
        repeat(remainingAttempts) {
            try {
                return screenshotUploadPreparer.prepare(image)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // Retry only image preparation. Upload failures still fail the whole run.
            }
        }
        return null
    }

    private suspend fun pollUntilTerminal(
        batchId: Long,
        fallbackTotal: Int,
        onProgress: (completed: Int, total: Int) -> Unit,
    ): OrganizeStatusDetail {
        while (true) {
            val status = captureRepository.getOrganizeStatus(batchId).getOrThrow()
            val total = status.totalCount.takeIf { it > 0 } ?: fallbackTotal
            val completed = (status.successCount + status.failCount).coerceIn(0, total)
            onProgress(completed, total)
            if (status.status != OrganizeStatus.PROCESSING) {
                return status
            }
            delay(POLL_INTERVAL_MILLIS.milliseconds)
        }
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1_000L
    }
}
