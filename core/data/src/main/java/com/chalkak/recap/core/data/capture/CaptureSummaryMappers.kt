package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.capture.CaptureSummary
import java.time.Instant

internal fun StoredScreenshotCard.toCaptureSummary(): CaptureSummary {
    val result = analysisResult
    return CaptureSummary(
        captureId = result.captureId,
        title = result.title,
        summary = result.summary,
        typeCode = result.typeCode,
        thumbnailUrl = toThumbnailSource(),
        isFavorite = result.isFavorite,
        organizedAt = result.organizedAt.toString(),
    )
}

internal fun StoredScreenshotCard.toThumbnailSource(): String? {
    return imageRefs.thumbnailPath?.takeIf { it.isNotBlank() }
        ?: imageRefs.storedImagePath?.takeIf { it.isNotBlank() }
        ?: imageRefs.sourceImageUri?.takeIf { it.isNotBlank() }
}

internal fun CaptureSummary.organizedAtEpochMillis(): Long {
    return runCatching { Instant.parse(organizedAt).toEpochMilli() }.getOrDefault(0L)
}

internal fun List<CaptureSummary>.matchesSearch(query: String): List<CaptureSummary> {
    val normalized = query.trim()
    if (normalized.isEmpty()) {
        return this
    }
    return filter { summary ->
        summary.title.contains(normalized, ignoreCase = true) ||
            summary.summary.contains(normalized, ignoreCase = true)
    }
}

internal fun List<CaptureSummary>.sortedByOrganizedAt(sort: com.chalkak.recap.core.model.storage.CaptureSort): List<CaptureSummary> {
    return when (sort) {
        com.chalkak.recap.core.model.storage.CaptureSort.Latest ->
            sortedByDescending { it.organizedAtEpochMillis() }
        com.chalkak.recap.core.model.storage.CaptureSort.Oldest ->
            sortedBy { it.organizedAtEpochMillis() }
    }
}
