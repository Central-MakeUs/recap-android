package com.chalkak.recap.app.share

internal object ShareImageFormatFilter {
    private val acceptedMimeTypes = setOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/heic",
        "image/heif",
    )

    private val acceptedExtensions = setOf(
        "png",
        "jpg",
        "jpeg",
        "heic",
        "heif",
    )

    fun isAccepted(mimeType: String?, displayName: String?): Boolean {
        val normalizedMime = mimeType?.trim()?.lowercase()
        if (!normalizedMime.isNullOrEmpty() && normalizedMime in acceptedMimeTypes) {
            return true
        }
        if (!normalizedMime.isNullOrEmpty() && normalizedMime != GENERIC_IMAGE_MIME_TYPE) {
            return false
        }
        val extension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.trim()
            ?.lowercase()
            .orEmpty()
        return extension in acceptedExtensions
    }

    private const val GENERIC_IMAGE_MIME_TYPE = "image/*"
}
