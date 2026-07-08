package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes
import com.chalkak.recap.core.model.screenshot.ScreenshotKeyField

data class ScreenshotCardImageRefs(
    val sourceImageUri: String? = null,
    val storedImagePath: String? = null,
    val thumbnailPath: String? = null,
)

data class StoredScreenshotCard(
    val analysisResult: ScreenshotAnalysisResult,
    val imageRefs: ScreenshotCardImageRefs,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

data class ScreenshotCardSaveEntry(
    val analysisResult: ScreenshotAnalysisResult,
    val imageRefs: ScreenshotCardImageRefs = ScreenshotCardImageRefs(),
)

fun ScreenshotAnalysisResult.toCardEntity(
    imageRefs: ScreenshotCardImageRefs,
    createdAtMillis: Long,
    updatedAtMillis: Long,
): ScreenshotCardEntity {
    return ScreenshotCardEntity(
        imageId = imageId,
        sourceImageUri = imageRefs.sourceImageUri,
        storedImagePath = imageRefs.storedImagePath,
        thumbnailPath = imageRefs.thumbnailPath,
        title = title,
        summary = summary,
        primaryContentType = contentTypes.primaryContentType.name,
        confidence = confidence.name,
        isFavorite = isFavorite,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

fun ScreenshotAnalysisResult.toKeyFieldEntities(): List<ScreenshotKeyFieldEntity> {
    return keyFields.map { field ->
        ScreenshotKeyFieldEntity(
            imageId = imageId,
            label = field.label,
            value = field.value,
            displayPriority = field.displayPriority,
            isSensitive = field.isSensitive,
        )
    }
}

fun ScreenshotCardWithKeyFields.toStoredScreenshotCard(): StoredScreenshotCard {
    return StoredScreenshotCard(
        analysisResult = card.toAnalysisResult(keyFields),
        imageRefs = ScreenshotCardImageRefs(
            sourceImageUri = card.sourceImageUri,
            storedImagePath = card.storedImagePath,
            thumbnailPath = card.thumbnailPath,
        ),
        createdAtMillis = card.createdAtMillis,
        updatedAtMillis = card.updatedAtMillis,
    )
}

private fun ScreenshotCardEntity.toAnalysisResult(
    keyFieldEntities: List<ScreenshotKeyFieldEntity>,
): ScreenshotAnalysisResult {
    return ScreenshotAnalysisResult(
        imageId = imageId,
        title = title,
        summary = summary,
        contentTypes = ScreenshotContentTypes(
            primaryContentType = ScreenshotContentType.valueOf(primaryContentType),
        ),
        keyFields = keyFieldEntities
            .sortedBy { it.displayPriority }
            .map { field ->
                ScreenshotKeyField(
                    label = field.label,
                    value = field.value,
                    displayPriority = field.displayPriority,
                    isSensitive = field.isSensitive,
                )
            },
        confidence = ScreenshotAnalysisConfidence.valueOf(confidence),
        isFavorite = isFavorite,
    )
}
