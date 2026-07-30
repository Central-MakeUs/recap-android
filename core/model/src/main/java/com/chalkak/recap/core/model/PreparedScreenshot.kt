package com.chalkak.recap.core.model

/**
 * In-memory upload-ready screenshot. JPEG bytes must stay in process memory only —
 * do not put this type in Parcelable, Serializable, SavedStateHandle, Intent, or disk.
 */
class PreparedScreenshot(
    val localImage: LocalImage,
    val jpegBytes: ByteArray,
    val mimeType: String = MIME_TYPE_JPEG,
) {
    init {
        require(jpegBytes.isNotEmpty()) { "jpegBytes must not be empty" }
        require(mimeType == MIME_TYPE_JPEG) { "mimeType must be $MIME_TYPE_JPEG" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PreparedScreenshot) return false
        return localImage.uri == other.localImage.uri
    }

    override fun hashCode(): Int = localImage.uri.hashCode()

    companion object {
        const val MIME_TYPE_JPEG = "image/jpeg"
    }
}

/**
 * In-memory handoff from confirmation to analysis.
 *
 * [preparedScreenshot] is present when background preparation finished before the user started.
 * Otherwise analysis resumes preparation from [completedPreparationAttempts].
 */
class ScreenshotUploadCandidate(
    val localImage: LocalImage,
    val preparedScreenshot: PreparedScreenshot? = null,
    val completedPreparationAttempts: Int = 0,
) {
    init {
        require(completedPreparationAttempts in 0..MAX_PREPARATION_ATTEMPTS)
        require(
            preparedScreenshot == null ||
                preparedScreenshot.localImage.uri == localImage.uri,
        ) {
            "Prepared screenshot must belong to the same local image"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScreenshotUploadCandidate) return false
        return localImage.uri == other.localImage.uri
    }

    override fun hashCode(): Int = localImage.uri.hashCode()

    companion object {
        const val MAX_PREPARATION_ATTEMPTS = 2
    }
}
