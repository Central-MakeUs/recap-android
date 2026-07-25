package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.capture.CaptureDetail
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import java.time.Instant
import java.time.OffsetDateTime

fun CaptureDetail.toStoredScreenshotCard(
    thumbnailPath: String? = null,
    updatedAtMillis: Long = System.currentTimeMillis(),
): StoredScreenshotCard {
    return StoredScreenshotCard(
        analysisResult = ScreenshotAnalysisResult(
            captureId = captureId,
            typeCode = typeCode,
            title = title,
            summary = summary,
            body = body,
            originalImageUrl = originalImageUrl.orEmpty(),
            isFavorite = isFavorite,
            organizedAt = parseOrganizedAt(organizedAt),
        ),
        imageRefs = ScreenshotCardImageRefs(
            sourceImageUri = originalImageUrl?.takeIf { it.isNotBlank() },
            storedImagePath = null,
            thumbnailPath = thumbnailPath?.takeIf { it.isNotBlank() },
        ),
        updatedAtMillis = updatedAtMillis,
    )
}

internal fun parseOrganizedAt(value: String): Instant {
    return runCatching { Instant.parse(value) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
        ?: Instant.EPOCH
}
