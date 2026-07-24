package com.chalkak.recap.app.share

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.OpenableColumns
import com.chalkak.recap.core.model.LocalImage
import java.security.MessageDigest

data class ShareImageParseResult(
    val accepted: List<LocalImage>,
    val rejectedCount: Int,
)

class ShareImageIntentParser(
    private val contentResolver: ContentResolver,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
) {
    fun parse(intent: Intent?): ShareImageParseResult? {
        if (intent == null) return null
        val action = intent.action ?: return null
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) {
            return null
        }

        val uris = extractUris(intent)
        if (uris.isEmpty()) {
            return ShareImageParseResult(accepted = emptyList(), rejectedCount = 0)
        }

        val accepted = ArrayList<LocalImage>(uris.size)
        var rejectedCount = 0
        val now = currentTimeMillis()
        for (uri in uris) {
            val image = parseUri(uri = uri, dateAddedMillis = now)
            if (image == null) {
                rejectedCount += 1
            } else {
                accepted += image
            }
        }
        return ShareImageParseResult(
            accepted = accepted,
            rejectedCount = rejectedCount,
        )
    }

    fun fingerprint(intent: Intent?): String? {
        if (intent == null) return null
        val action = intent.action ?: return null
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) {
            return null
        }
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(action.encodeToByteArray())
        digest.update(0.toByte())
        digest.update(intent.type.orEmpty().encodeToByteArray())
        extractUris(intent).forEach { uri ->
            digest.update(0.toByte())
            digest.update(uri.toString().encodeToByteArray())
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    internal fun parseUri(
        uri: Uri,
        dateAddedMillis: Long,
    ): LocalImage? {
        val displayName = queryDisplayName(uri) ?: uri.lastPathSegment.orEmpty()
        val mimeTypeResult = runCatching { contentResolver.getType(uri) }
        if (mimeTypeResult.isFailure) return null
        if (!ShareImageFormatFilter.isAccepted(mimeTypeResult.getOrNull(), displayName)) {
            return null
        }
        return LocalImage(
            uri = uri.toString(),
            displayName = displayName.ifBlank { "image" },
            dateAddedMillis = dateAddedMillis,
        )
    }

    private fun extractUris(intent: Intent): List<Uri> {
        val fromExtra: List<Uri> = when (intent.action) {
            Intent.ACTION_SEND -> listOfNotNull(intent.parcelableExtraCompat<Uri>(Intent.EXTRA_STREAM))
            Intent.ACTION_SEND_MULTIPLE -> intent.uriListExtra(Intent.EXTRA_STREAM)
            else -> emptyList()
        }
        val fromClip = extractClipDataUris(intent)
        // 일부 공유 앱은 EXTRA_STREAM typed getter가 비고 ClipData만 채우거나, 둘 다 채운다.
        return (fromExtra + fromClip).distinct()
    }

    private fun extractClipDataUris(intent: Intent): List<Uri> {
        val clipData = intent.clipData ?: return emptyList()
        return buildList(clipData.itemCount) {
            for (index in 0 until clipData.itemCount) {
                clipData.getItemAt(index)?.uri?.let(::add)
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index < 0) return@use null
                cursor.getString(index)
            }
        }.getOrNull()
    }
}

@Suppress("DEPRECATION")
private inline fun <reified T : Parcelable> Intent.parcelableExtraCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
            ?: (getParcelableExtra(key) as? T)
    } else {
        getParcelableExtra(key) as? T
    }
}

@Suppress("DEPRECATION")
private fun Intent.uriListExtra(key: String): List<Uri> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val typed = getParcelableArrayListExtra(key, Uri::class.java)
            ?.mapNotNull { uri -> uri }
            .orEmpty()
        if (typed.isNotEmpty()) return typed
    }
    // Share intent의 ArrayList<Uri>는 API 33+ typed getter가 null/empty를 주는 경우가 있다.
    val raw = getParcelableArrayListExtra<Parcelable>(key).orEmpty()
    return raw.mapNotNull { item -> item as? Uri }
}
