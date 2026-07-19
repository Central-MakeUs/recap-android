package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.capture.remote.CaptureApi
import com.chalkak.recap.core.data.capture.remote.FavoriteRequestDto
import com.chalkak.recap.core.data.capture.remote.OrganizeRequestDto
import com.chalkak.recap.core.data.capture.remote.UploadUrlsRequestDto
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CaptureDetail
import com.chalkak.recap.core.model.capture.OrganizeBatch
import com.chalkak.recap.core.model.capture.OrganizeStatusDetail
import com.chalkak.recap.core.model.capture.PendingOrganizeResult
import com.chalkak.recap.core.model.capture.UploadItem
import com.chalkak.recap.core.model.capture.UploadUrls
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class CaptureRepository @Inject constructor(
    private val captureApi: CaptureApi,
    private val okHttpClient: OkHttpClient,
) {
    suspend fun issueUploadUrls(count: Int): Result<UploadUrls> =
        runRemoteCatchingSuspend {
            mapApiResponse(captureApi.issueUploadUrls(UploadUrlsRequestDto(count = count))) { dto ->
                UploadUrls(
                    uploads = dto.uploads.map { UploadItem(imageKey = it.imageKey, uploadUrl = it.uploadUrl) },
                )
            }.getOrThrow()
        }

    suspend fun uploadImage(
        uploadUrl: String,
        bytes: ByteArray,
        contentType: String = "image/jpeg",
    ): Result<Unit> =
        runRemoteCatchingSuspend {
            withContext(Dispatchers.IO) {
                val requestBody = bytes.toRequestBody(contentType.toMediaType())
                val request =
                    Request.Builder()
                        .url(uploadUrl)
                        .put(requestBody)
                        .build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw RemoteApiException(
                            code = "UPLOAD_FAILED",
                            message = "Image upload failed with HTTP ${response.code}",
                        )
                    }
                }
            }
        }

    suspend fun organize(imageKeys: List<String>): Result<OrganizeBatch> =
        runRemoteCatchingSuspend {
            mapApiResponse(captureApi.organize(OrganizeRequestDto(imageKeys = imageKeys))) { dto ->
                OrganizeBatch(
                    batchId = dto.batchId,
                    totalCount = dto.totalCount,
                    status = dto.status.toDomain(),
                )
            }.getOrThrow()
        }

    suspend fun getOrganizeStatus(batchId: Long): Result<OrganizeStatusDetail> =
        runRemoteCatchingSuspend {
            mapApiResponse(captureApi.getOrganizeStatus(batchId)) { dto ->
                OrganizeStatusDetail(
                    batchId = dto.batchId,
                    status = dto.status.toDomain(),
                    totalCount = dto.totalCount,
                    successCount = dto.successCount,
                    failCount = dto.failCount,
                )
            }.getOrThrow()
        }

    suspend fun cancelOrganize(batchId: Long): Result<Unit> =
        runRemoteCatchingSuspend {
            captureApi.cancelOrganize(batchId)
        }

    suspend fun ackOrganizeResult(batchId: Long): Result<Unit> =
        runRemoteCatchingSuspend {
            captureApi.ackOrganizeResult(batchId)
        }

    suspend fun getPendingResult(): Result<PendingOrganizeResult> =
        runRemoteCatchingSuspend {
            mapApiResponse(captureApi.getPendingResult()) { dto ->
                PendingOrganizeResult(
                    batchId = dto.batchId,
                    status = dto.status?.toDomain(),
                    successCount = dto.successCount,
                    failCount = dto.failCount,
                )
            }.getOrThrow()
        }

    suspend fun getDetail(captureId: Long): Result<CaptureDetail> =
        runRemoteCatchingSuspend {
            mapApiResponse(captureApi.getDetail(captureId)) { it.toDomain() }.getOrThrow()
        }

    suspend fun delete(captureId: Long): Result<Unit> =
        runRemoteCatchingSuspend {
            captureApi.delete(captureId)
        }

    suspend fun updateFavorite(
        captureId: Long,
        isFavorite: Boolean,
    ): Result<Unit> =
        runRemoteCatchingSuspend {
            captureApi.updateFavorite(
                captureId = captureId,
                body = FavoriteRequestDto(isFavorite = isFavorite),
            )
        }
}
