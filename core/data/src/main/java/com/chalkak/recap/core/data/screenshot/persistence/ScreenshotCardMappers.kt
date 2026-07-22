package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.time.Instant

data class ScreenshotCardImageRefs(
    val sourceImageUri: String? = null,
    val storedImagePath: String? = null,
    val thumbnailPath: String? = null,
)

data class StoredScreenshotCard(
    val analysisResult: ScreenshotAnalysisResult,
    val imageRefs: ScreenshotCardImageRefs,
    val updatedAtMillis: Long,
)

data class ScreenshotCardSaveEntry(
    val analysisResult: ScreenshotAnalysisResult,
    val imageRefs: ScreenshotCardImageRefs = ScreenshotCardImageRefs(),
)

internal fun mergeImageRefs(
    incoming: ScreenshotCardImageRefs,
    existing: ScreenshotCardEntity?,
): ScreenshotCardImageRefs {
    if (existing == null) {
        return incoming
    }
    return ScreenshotCardImageRefs(
        sourceImageUri = incoming.sourceImageUri ?: existing.sourceImageUri,
        storedImagePath = incoming.storedImagePath ?: existing.storedImagePath,
        thumbnailPath = incoming.thumbnailPath ?: existing.thumbnailPath,
    )
}

fun ScreenshotAnalysisResult.toCardEntity(
    imageRefs: ScreenshotCardImageRefs,
    updatedAtMillis: Long,
): ScreenshotCardEntity {
    return ScreenshotCardEntity(
        captureId = captureId,
        sourceImageUri = imageRefs.sourceImageUri,
        storedImagePath = imageRefs.storedImagePath,
        thumbnailPath = imageRefs.thumbnailPath,
        title = title,
        summary = summary,
        body = body,
        typeCode = typeCode.name,
        originalImageUrl = originalImageUrl,
        isFavorite = isFavorite,
        organizedAtMillis = organizedAt.toEpochMilli(),
        updatedAtMillis = updatedAtMillis,
    )
}

fun ScreenshotCardEntity.toStoredScreenshotCard(): StoredScreenshotCard {
    return StoredScreenshotCard(
        analysisResult = toAnalysisResult(),
        imageRefs = ScreenshotCardImageRefs(
            sourceImageUri = sourceImageUri,
            storedImagePath = storedImagePath,
            thumbnailPath = thumbnailPath,
        ),
        updatedAtMillis = updatedAtMillis,
    )
}

private fun ScreenshotCardEntity.toAnalysisResult(): ScreenshotAnalysisResult {
    return ScreenshotAnalysisResult(
        captureId = captureId,
        typeCode = ScreenshotContentType.valueOf(typeCode),
        title = title,
        summary = summary,
        body = body,
        originalImageUrl = originalImageUrl,
        isFavorite = isFavorite,
        organizedAt = Instant.ofEpochMilli(organizedAtMillis),
    )
}
