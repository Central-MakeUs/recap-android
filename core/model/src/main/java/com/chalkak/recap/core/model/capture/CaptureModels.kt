package com.chalkak.recap.core.model.capture

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

data class CaptureSummary(
    val captureId: Long,
    val title: String,
    val summary: String,
    val typeCode: ScreenshotContentType,
    val thumbnailUrl: String?,
    val isFavorite: Boolean,
    val organizedAt: String,
)

data class CaptureDetail(
    val captureId: Long,
    val typeCode: ScreenshotContentType,
    val title: String,
    val summary: String,
    val body: String,
    val originalImageUrl: String?,
    val isFavorite: Boolean,
    val organizedAt: String,
)

data class CaptureList(
    val count: Int,
    val items: List<CaptureSummary>,
)

data class UploadItem(
    val imageKey: String,
    val uploadUrl: String,
)

data class UploadUrls(
    val uploads: List<UploadItem>,
)

enum class OrganizeStatus {
    PROCESSING,
    COMPLETED,
    PARTIAL_FAILED,
    FAILED,
    CANCELLED,
}

data class OrganizeBatch(
    val batchId: Long,
    val totalCount: Int,
    val status: OrganizeStatus,
)

data class OrganizeStatusDetail(
    val batchId: Long,
    val status: OrganizeStatus,
    val totalCount: Int,
    val successCount: Int,
    val failCount: Int,
)

data class PendingOrganizeResult(
    val batchId: Long?,
    val status: OrganizeStatus?,
    val successCount: Int?,
    val failCount: Int?,
)
