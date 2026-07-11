package com.chalkak.recap.feature.screenshot

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val OrganizedDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy. MM. dd")

fun formatOrganizedDate(createdAtMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
    return Instant.ofEpochMilli(createdAtMillis)
        .atZone(zoneId)
        .toLocalDate()
        .format(OrganizedDateFormatter)
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
