package com.chalkak.recap.feature.screenshot

import com.chalkak.recap.core.design.component.card.formatOrganizedAbsoluteDate
import java.time.ZoneId
import timber.log.Timber

fun formatOrganizedDate(
    organizedAtMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
    nowMillis: Long = System.currentTimeMillis(),
): String {
    return formatOrganizedAbsoluteDate(
        organizedAtMillis = organizedAtMillis,
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
    val ordered = when (priority) {
        ScreenshotImageResolvePriority.Preview -> listOf(
            thumbnailPath,
            storedImagePath,
            sourceImageUri,
        )

        // 짧은 수명의 원격 URL(예: S3 presigned)보다 로컬/비-http 모델을 우선한다.
        // Mock content:// 소스는 둘 다 비-http일 때 thumbnail보다 앞선다.
        ScreenshotImageResolvePriority.Fullscreen -> listOf(
            storedImagePath,
            sourceImageUri,
            thumbnailPath,
        )
    }.mapNotNull { value -> value?.takeIf { it.isNotBlank() } }

    val selected = when (priority) {
        ScreenshotImageResolvePriority.Preview -> ordered.firstOrNull()
        ScreenshotImageResolvePriority.Fullscreen ->
            ordered.firstOrNull { !it.isRemoteHttpUrl() } ?: ordered.firstOrNull()
    }
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

private fun String.isRemoteHttpUrl(): Boolean {
    return startsWith("http://", ignoreCase = true) ||
        startsWith("https://", ignoreCase = true)
}
