package com.chalkak.recap.feature.settings.account

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val JoinedDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.M.d", Locale.KOREAN)

fun formatJoinedDate(
    connectedAt: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = connectedAt.atZone(zoneId).toLocalDate().format(JoinedDateFormatter)

fun formatJoinedDateFromIso(
    createdAt: String,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String? =
    runCatching {
        formatJoinedDate(Instant.parse(createdAt), zoneId)
    }.getOrNull()

fun parseLoginPlatform(platform: String): LoginPlatform =
    when (platform.lowercase(Locale.ROOT)) {
        "apple" -> LoginPlatform.Apple
        else -> LoginPlatform.Kakao
    }
