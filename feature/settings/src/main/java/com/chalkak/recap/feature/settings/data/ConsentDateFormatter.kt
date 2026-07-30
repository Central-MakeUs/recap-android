package com.chalkak.recap.feature.settings.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ConsentDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN)

fun formatConsentDateFromIso(
    consentedAt: String?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String =
    consentedAt
        ?.let {
            runCatching {
                Instant.parse(it).atZone(zoneId).toLocalDate().format(ConsentDateFormatter)
            }.getOrNull()
        }
        .orEmpty()
