package com.chalkak.recap.core.data.capture.remote

import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.capture.OrganizeStatus
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadUrlsRequestDto(
    val count: Int,
)

@Serializable
data class UploadItemDto(
    val imageKey: String,
    val uploadUrl: String,
)

@Serializable
data class UploadUrlsResponseDto(
    val uploads: List<UploadItemDto> = emptyList(),
)

@Serializable
data class OrganizeRequestDto(
    val imageKeys: List<String>,
)

@Serializable
enum class OrganizeStatusDto {
    @SerialName("PROCESSING")
    PROCESSING,

    @SerialName("COMPLETED")
    COMPLETED,

    @SerialName("PARTIAL_FAILED")
    PARTIAL_FAILED,

    @SerialName("FAILED")
    FAILED,

    @SerialName("CANCELLED")
    CANCELLED,
}

@Serializable
data class OrganizeResponseDto(
    val batchId: Long,
    val totalCount: Int,
    val status: OrganizeStatusDto,
)

@Serializable
data class OrganizeStatusResponseDto(
    val batchId: Long,
    val status: OrganizeStatusDto,
    val totalCount: Int,
    val successCount: Int,
    val failCount: Int,
)

@Serializable
data class PendingResultResponseDto(
    val batchId: Long? = null,
    val status: OrganizeStatusDto? = null,
    val successCount: Int? = null,
    val failCount: Int? = null,
)

@Serializable
data class FavoriteRequestDto(
    val isFavorite: Boolean,
)

@Serializable
enum class CardTypeDto {
    @SerialName("JOB")
    JOB,

    @SerialName("SHOPPING")
    SHOPPING,

    @SerialName("PLACE")
    PLACE,

    @SerialName("SCHEDULE")
    SCHEDULE,

    @SerialName("KNOWLEDGE")
    KNOWLEDGE,

    @SerialName("CONTENT")
    CONTENT,

    @SerialName("BENEFIT")
    BENEFIT,

    @SerialName("RECORD")
    RECORD,

    @SerialName("ETC")
    ETC,
}

@Serializable
data class CaptureSummaryResponseDto(
    val captureId: Long,
    val title: String,
    val summary: String,
    val typeCode: CardTypeDto,
    val thumbnailUrl: String? = null,
    val isFavorite: Boolean,
    val organizedAt: String,
)

@Serializable
data class CaptureListResponseDto(
    val count: Int,
    val items: List<CaptureSummaryResponseDto> = emptyList(),
)

@Serializable
data class CaptureDetailResponseDto(
    val captureId: Long,
    val typeCode: CardTypeDto,
    val title: String,
    val summary: String,
    val body: String,
    val originalImageUrl: String? = null,
    val isFavorite: Boolean,
    val organizedAt: String,
)

typealias UploadUrlsApiResponse = ApiResponseDto<UploadUrlsResponseDto>
typealias OrganizeApiResponse = ApiResponseDto<OrganizeResponseDto>
typealias OrganizeStatusApiResponse = ApiResponseDto<OrganizeStatusResponseDto>
typealias PendingResultApiResponse = ApiResponseDto<PendingResultResponseDto>
typealias CaptureDetailApiResponse = ApiResponseDto<CaptureDetailResponseDto>
typealias CaptureListApiResponse = ApiResponseDto<CaptureListResponseDto>

fun CardTypeDto.toDomain(): ScreenshotContentType =
    when (this) {
        CardTypeDto.JOB -> ScreenshotContentType.JOB
        CardTypeDto.SHOPPING -> ScreenshotContentType.SHOPPING
        CardTypeDto.PLACE -> ScreenshotContentType.PLACE
        CardTypeDto.SCHEDULE -> ScreenshotContentType.SCHEDULE
        CardTypeDto.KNOWLEDGE -> ScreenshotContentType.KNOWLEDGE
        CardTypeDto.CONTENT -> ScreenshotContentType.CONTENT
        CardTypeDto.BENEFIT -> ScreenshotContentType.BENEFIT
        CardTypeDto.RECORD -> ScreenshotContentType.RECORD
        CardTypeDto.ETC -> ScreenshotContentType.ETC
    }

fun ScreenshotContentType.toCardTypeDto(): CardTypeDto =
    when (this) {
        ScreenshotContentType.JOB -> CardTypeDto.JOB
        ScreenshotContentType.SHOPPING -> CardTypeDto.SHOPPING
        ScreenshotContentType.PLACE -> CardTypeDto.PLACE
        ScreenshotContentType.SCHEDULE -> CardTypeDto.SCHEDULE
        ScreenshotContentType.KNOWLEDGE -> CardTypeDto.KNOWLEDGE
        ScreenshotContentType.CONTENT -> CardTypeDto.CONTENT
        ScreenshotContentType.BENEFIT -> CardTypeDto.BENEFIT
        ScreenshotContentType.RECORD -> CardTypeDto.RECORD
        ScreenshotContentType.ETC -> CardTypeDto.ETC
    }

fun OrganizeStatusDto.toDomain(): OrganizeStatus =
    when (this) {
        OrganizeStatusDto.PROCESSING -> OrganizeStatus.PROCESSING
        OrganizeStatusDto.COMPLETED -> OrganizeStatus.COMPLETED
        OrganizeStatusDto.PARTIAL_FAILED -> OrganizeStatus.PARTIAL_FAILED
        OrganizeStatusDto.FAILED -> OrganizeStatus.FAILED
        OrganizeStatusDto.CANCELLED -> OrganizeStatus.CANCELLED
    }

fun CaptureSummaryResponseDto.toDomain() =
    com.chalkak.recap.core.model.capture.CaptureSummary(
        captureId = captureId,
        title = title,
        summary = summary,
        typeCode = typeCode.toDomain(),
        thumbnailUrl = thumbnailUrl,
        isFavorite = isFavorite,
        organizedAt = organizedAt,
    )

fun CaptureListResponseDto.toDomain() =
    com.chalkak.recap.core.model.capture.CaptureList(
        count = count,
        items = items.map { it.toDomain() },
    )

fun CaptureDetailResponseDto.toDomain() =
    com.chalkak.recap.core.model.capture.CaptureDetail(
        captureId = captureId,
        typeCode = typeCode.toDomain(),
        title = title,
        summary = summary,
        body = body,
        originalImageUrl = originalImageUrl,
        isFavorite = isFavorite,
        organizedAt = organizedAt,
    )
