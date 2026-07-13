package com.chalkak.recap.feature.screenshot

import com.chalkak.recap.core.design.component.card.formatOrganizedAbsoluteDate
import java.time.ZoneId

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

fun resolveScreenshotImageModel(
    storedImagePath: String?,
    sourceImageUri: String?,
    thumbnailPath: String?,
): Any? {
    return storedImagePath?.takeIf { it.isNotBlank() }
        ?: sourceImageUri?.takeIf { it.isNotBlank() }
        ?: thumbnailPath?.takeIf { it.isNotBlank() }
}
