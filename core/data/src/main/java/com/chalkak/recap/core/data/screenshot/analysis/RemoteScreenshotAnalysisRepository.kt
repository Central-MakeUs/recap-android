package com.chalkak.recap.core.data.screenshot.analysis

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.chalkak.recap.core.data.capture.CaptureRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.capture.OrganizeStatusDetail
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

class RemoteOrganizeFailedException(
    val status: OrganizeStatus,
    message: String,
) : Exception(message)

@Singleton
class RemoteScreenshotAnalysisRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val captureRepository: CaptureRepository,
    private val changeNotifier: RemoteCaptureChangeNotifier,
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
            throw RemoteApiException(
                code = "UPLOAD_URL_MISMATCH",
                message = "Expected $total upload URLs but received ${uploadUrls.uploads.size}",
            )
        }

        // image PUT 업로드
        val imageKeys = ArrayList<String>(total)
        inputs.forEachIndexed { index, input ->
            val upload = uploadUrls.uploads[index]
            val bytes = readImageBytes(input.uri)
            val contentType = resolveContentType(input.uri)
            captureRepository.uploadImage(
                uploadUrl = upload.uploadUrl,
                bytes = bytes,
                contentType = contentType,
            ).getOrThrow()
            imageKeys += upload.imageKey
            onProgress(index + 1, total)
        }

        // 분석 시작
        val batch = captureRepository.organize(imageKeys).getOrThrow()
        // 1초 단위 status 폴링
        val finalStatus = pollUntilTerminal(
            batchId = batch.batchId,
            fallbackTotal = batch.totalCount.coerceAtLeast(total),
            onProgress = onProgress,
        )

        // 결과 처리
        when (finalStatus.status) {
            OrganizeStatus.COMPLETED,
            OrganizeStatus.PARTIAL_FAILED,
            -> {
                captureRepository.ackOrganizeResult(finalStatus.batchId)
                    .onFailure { error ->
                        Timber.w(error, "Failed to ack organize result batchId=%s", finalStatus.batchId)
                    }
                changeNotifier.notifyCaptureChanged()
                return ScreenshotOrganizeOutcome.RemoteCompleted(
                    successCount = finalStatus.successCount,
                    failCount = finalStatus.failCount,
                    status = finalStatus.status,
                )
            }

            OrganizeStatus.FAILED,
            OrganizeStatus.CANCELLED,
            -> {
                captureRepository.ackOrganizeResult(finalStatus.batchId)
                    .onFailure { error ->
                        Timber.w(error, "Failed to ack organize result batchId=%s", finalStatus.batchId)
                    }
                throw RemoteOrganizeFailedException(
                    status = finalStatus.status,
                    message = "Remote organize finished with status=${finalStatus.status}",
                )
            }

            OrganizeStatus.PROCESSING -> {
                throw RemoteOrganizeFailedException(
                    status = OrganizeStatus.PROCESSING,
                    message = "Remote organize polling ended while still processing",
                )
            }
        }
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

    private fun readImageBytes(uriString: String): ByteArray {
        val uri = parseUri(uriString)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            } ?: throw RemoteApiException(
                code = "IMAGE_READ_FAILED",
                message = "Unable to open image uri=$uriString",
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: RemoteApiException) {
            throw error
        } catch (error: Exception) {
            throw RemoteApiException(
                code = "IMAGE_READ_FAILED",
                message = error.message ?: "Unable to read image uri=$uriString",
            )
        }
    }

    private fun resolveContentType(uriString: String): String {
        val uri = parseUri(uriString)
        return context.contentResolver.getType(uri)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_CONTENT_TYPE
    }

    private fun parseUri(uriString: String): Uri {
        if (uriString.isBlank()) {
            throw RemoteApiException(
                code = "INVALID_IMAGE_URI",
                message = "Image uri is blank",
            )
        }
        return uriString.toUri()
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1_000L
        const val DEFAULT_CONTENT_TYPE = "image/jpeg"
    }
}
