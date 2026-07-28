package com.chalkak.recap.core.data.capture.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CaptureApi {
    @POST("api/v1/captures/upload-urls")
    suspend fun issueUploadUrls(
        @Body body: UploadUrlsRequestDto,
    ): UploadUrlsApiResponse

    @POST("api/v1/captures/organize")
    suspend fun organize(
        @Body body: OrganizeRequestDto,
    ): OrganizeApiResponse

    @GET("api/v1/captures/organize/{batchId}/status")
    suspend fun getOrganizeStatus(
        @Path("batchId") batchId: Long,
    ): OrganizeStatusApiResponse

    @POST("api/v1/captures/organize/{batchId}/cancel")
    suspend fun cancelOrganize(
        @Path("batchId") batchId: Long,
    )

    @POST("api/v1/captures/organize/{batchId}/ack")
    suspend fun ackOrganizeResult(
        @Path("batchId") batchId: Long,
    )

    @GET("api/v1/captures/organize/pending-result")
    suspend fun getPendingResult(): PendingResultApiResponse

    @GET("api/v1/captures/{captureId}")
    suspend fun getDetail(
        @Path("captureId") captureId: Long,
    ): CaptureDetailApiResponse

    @DELETE("api/v1/captures/{captureId}")
    suspend fun delete(
        @Path("captureId") captureId: Long,
    )

    @PATCH("api/v1/captures/{captureId}/favorite")
    suspend fun updateFavorite(
        @Path("captureId") captureId: Long,
        @Body body: FavoriteRequestDto,
    )

    @POST("api/v1/captures/bulk-delete")
    suspend fun bulkDelete(
        @Body body: BulkDeleteRequestDto,
    )

    @PATCH("api/v1/captures/{captureId}/body")
    suspend fun updateBody(
        @Path("captureId") captureId: Long,
        @Body body: BodyUpdateRequestDto,
    )

    @POST("api/v1/captures/{captureId}/report")
    suspend fun report(
        @Path("captureId") captureId: Long,
        @Body body: ReportRequestDto,
    )
}
