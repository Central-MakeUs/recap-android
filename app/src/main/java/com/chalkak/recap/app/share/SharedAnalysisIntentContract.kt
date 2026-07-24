package com.chalkak.recap.app.share

import android.content.Context
import android.content.Intent
import android.content.ClipData
import androidx.core.net.toUri
import com.chalkak.recap.MainActivity
import com.chalkak.recap.core.model.LocalImage

data class SharedAnalysisRequest(
    val requestId: String,
    val images: List<LocalImage>,
)

internal data class SharedAnalysisPayload(
    val requestId: String,
    val uris: List<String>,
    val displayNames: List<String>,
    val dateAddedMillis: LongArray,
)

object SharedAnalysisIntentContract {
    const val ACTION = "com.chalkak.recap.action.START_SHARED_ANALYSIS"

    internal const val EXTRA_REQUEST_ID = "shared_analysis_request_id"
    internal const val EXTRA_URIS = "shared_analysis_uris"
    internal const val EXTRA_DISPLAY_NAMES = "shared_analysis_display_names"
    internal const val EXTRA_DATE_ADDED_MILLIS = "shared_analysis_date_added_millis"

    fun createIntent(
        context: Context,
        requestId: String,
        images: List<LocalImage>,
    ): Intent {
        val payload = encodeSharedAnalysisPayload(requestId = requestId, images = images)
        val intent = Intent(ACTION).apply {
            setClass(context, MainActivity::class.java)
            putExtra(EXTRA_REQUEST_ID, payload.requestId)
            putStringArrayListExtra(EXTRA_URIS, ArrayList(payload.uris))
            putStringArrayListExtra(EXTRA_DISPLAY_NAMES, ArrayList(payload.displayNames))
            putExtra(EXTRA_DATE_ADDED_MILLIS, payload.dateAddedMillis)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (images.isNotEmpty()) {
            val firstUri = images.first().uri.toUri()
            val clipData = ClipData.newUri(context.contentResolver, "shared_images", firstUri)
            for (index in 1 until images.size) {
                clipData.addItem(ClipData.Item(images[index].uri.toUri()))
            }
            intent.clipData = clipData
        }
        return intent
    }

    fun decode(intent: Intent): SharedAnalysisRequest? {
        if (intent.action != ACTION) {
            return null
        }
        return decodeSharedAnalysisPayload(
            requestId = intent.getStringExtra(EXTRA_REQUEST_ID),
            uris = intent.getStringArrayListExtra(EXTRA_URIS),
            displayNames = intent.getStringArrayListExtra(EXTRA_DISPLAY_NAMES),
            dateAddedMillis = intent.getLongArrayExtra(EXTRA_DATE_ADDED_MILLIS),
        )
    }
}

internal fun encodeSharedAnalysisPayload(
    requestId: String,
    images: List<LocalImage>,
): SharedAnalysisPayload {
    return SharedAnalysisPayload(
        requestId = requestId,
        uris = images.map { image -> image.uri },
        displayNames = images.map { image -> image.displayName },
        dateAddedMillis = images.map { image -> image.dateAddedMillis }.toLongArray(),
    )
}

internal fun decodeSharedAnalysisPayload(
    requestId: String?,
    uris: List<String>?,
    displayNames: List<String>?,
    dateAddedMillis: LongArray?,
): SharedAnalysisRequest? {
    val resolvedRequestId = requestId?.takeIf { id -> id.isNotBlank() } ?: return null
    if (uris == null || displayNames == null || dateAddedMillis == null) {
        return null
    }
    if (uris.isEmpty() ||
        uris.size != displayNames.size ||
        uris.size != dateAddedMillis.size
    ) {
        return null
    }
    val images = ArrayList<LocalImage>(uris.size)
    for (index in uris.indices) {
        val uriString = uris[index]
        if (!hasUsableUriString(uriString)) {
            return null
        }
        images += LocalImage(
            uri = uriString,
            displayName = displayNames[index],
            dateAddedMillis = dateAddedMillis[index],
        )
    }
    return SharedAnalysisRequest(
        requestId = resolvedRequestId,
        images = images,
    )
}

internal fun hasUsableUriString(uriString: String): Boolean {
    if (uriString.isBlank()) {
        return false
    }
    val schemeSeparator = uriString.indexOf("://")
    if (schemeSeparator <= 0) {
        return false
    }
    val scheme = uriString.substring(0, schemeSeparator)
    return scheme.any { character -> character.isLetter() }
}
