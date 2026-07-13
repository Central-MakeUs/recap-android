package com.chalkak.recap.feature.screenshot

import com.chalkak.recap.core.design.component.card.formatOrganizedAbsoluteDate
import java.time.ZoneId
import timber.log.Timber

fun formatOrganizedDate(
    createdAtMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
    nowMillis: Long = System.currentTimeMillis(),
): String {
    return formatOrganizedAbsoluteDate(
        organizedAtMillis = createdAtMillis,
        nowMillis = nowMillis,
        zoneId = zoneId,
    )
}

enum class ScreenshotImageResolvePriority {
    Preview,
    Fullscreen,
}

fun resolveScreenshotImageModel(
    storedImagePath: String?,
    sourceImageUri: String?,
    thumbnailPath: String?,
    priority: ScreenshotImageResolvePriority,
): Any? {
    val candidates = when (priority) {
        ScreenshotImageResolvePriority.Preview -> listOf(
            thumbnailPath,
            storedImagePath,
            sourceImageUri,
        )

        ScreenshotImageResolvePriority.Fullscreen -> listOf(
            storedImagePath,
            sourceImageUri,
            thumbnailPath,
        )
    }
    val selected = candidates.firstOrNull { !it.isNullOrBlank() }
    val normalizedThumbnail = thumbnailPath?.takeIf { it.isNotBlank() }
    when {
        selected == null -> {
            Timber.d("Screenshot image resolve priority=%s selected=none", priority)
        }

        normalizedThumbnail != null && selected == normalizedThumbnail -> {
            Timber.d(
                "Screenshot image resolve priority=%s using=thumbnail path=%s",
                priority,
                selected,
            )
        }

        else -> {
            Timber.d(
                "Screenshot image resolve priority=%s using=fallback model=%s thumbnail=%s",
                priority,
                selected,
                normalizedThumbnail,
            )
        }
    }
    return selected
}
