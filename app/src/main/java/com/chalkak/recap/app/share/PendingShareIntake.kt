package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.feature.organize.MAX_SELECTION_COUNT

sealed interface PendingShareIntake {
    val sessionId: String

    data class Confirmation(
        override val sessionId: String,
        val images: List<LocalImage>,
        val rejectedCount: Int,
        val trimmedByMax: Boolean,
    ) : PendingShareIntake

    data class Unsupported(
        override val sessionId: String,
    ) : PendingShareIntake
}

internal const val SHARE_INTAKE_SAVED_STATE_KEY = "pending_share_intake"

private const val SHARE_STATE_FIELD_SEPARATOR = "\u001E"

internal fun PendingShareIntake.encodeForSavedState(): String = when (this) {
    is PendingShareIntake.Unsupported -> buildString {
        append("unsupported")
        append(SHARE_STATE_FIELD_SEPARATOR)
        append(sessionId)
    }
    is PendingShareIntake.Confirmation -> buildString {
        append("confirmation")
        append(SHARE_STATE_FIELD_SEPARATOR)
        append(sessionId)
        append(SHARE_STATE_FIELD_SEPARATOR)
        append(rejectedCount)
        append(SHARE_STATE_FIELD_SEPARATOR)
        append(trimmedByMax)
        append(SHARE_STATE_FIELD_SEPARATOR)
        append(images.size)
        images.forEach { image ->
            append(SHARE_STATE_FIELD_SEPARATOR)
            append(image.uri)
            append(SHARE_STATE_FIELD_SEPARATOR)
            append(image.displayName)
            append(SHARE_STATE_FIELD_SEPARATOR)
            append(image.dateAddedMillis)
        }
    }
}

internal fun decodePendingShareIntakeFromSavedState(encoded: String): PendingShareIntake? {
    val parts = encoded.split(SHARE_STATE_FIELD_SEPARATOR)
    if (parts.size == 2 && parts[0] == "unsupported") {
        return PendingShareIntake.Unsupported(sessionId = parts[1])
    }
    if (parts.size < 5 || parts[0] != "confirmation") {
        return null
    }
    val sessionId = parts[1]
    val rejectedCount = parts[2].toIntOrNull() ?: return null
    val trimmedByMax = parts[3].toBooleanStrictOrNull() ?: return null
    val imageCount = parts[4].toIntOrNull() ?: return null
    val expectedSize = 5 + imageCount * 3
    if (parts.size != expectedSize) {
        return null
    }
    val images = buildList(imageCount) {
        var index = 5
        repeat(imageCount) {
            val dateAddedMillis = parts[index + 2].toLongOrNull() ?: return null
            add(
                LocalImage(
                    uri = parts[index],
                    displayName = parts[index + 1],
                    dateAddedMillis = dateAddedMillis,
                ),
            )
            index += 3
        }
    }
    return PendingShareIntake.Confirmation(
        sessionId = sessionId,
        images = images,
        rejectedCount = rejectedCount,
        trimmedByMax = trimmedByMax,
    )
}

internal fun ShareImageParseResult.toPendingShareIntake(
    sessionId: String,
    maxCount: Int = MAX_SELECTION_COUNT,
): PendingShareIntake {
    if (accepted.isEmpty()) {
        return PendingShareIntake.Unsupported(sessionId = sessionId)
    }
    val trimmedByMax = accepted.size > maxCount
    return PendingShareIntake.Confirmation(
        sessionId = sessionId,
        images = accepted.take(maxCount),
        rejectedCount = rejectedCount,
        trimmedByMax = trimmedByMax,
    )
}
