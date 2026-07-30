package com.chalkak.recap.core.data.screenshot.analysis

import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot

data class ScreenshotAnalysisInput(
    val fileName: String,
    val uri: String = "",
    val jpegBytes: ByteArray? = null,
    val contentType: String = PreparedScreenshot.MIME_TYPE_JPEG,
    val localImage: LocalImage? = null,
    val completedPreparationAttempts: Int = if (jpegBytes == null) 0 else 1,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScreenshotAnalysisInput) return false
        if (fileName != other.fileName) return false
        if (uri != other.uri) return false
        if (contentType != other.contentType) return false
        if (localImage != other.localImage) return false
        if (completedPreparationAttempts != other.completedPreparationAttempts) return false
        if (jpegBytes === other.jpegBytes) return true
        if (jpegBytes == null || other.jpegBytes == null) return false
        return jpegBytes.contentEquals(other.jpegBytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + (jpegBytes?.contentHashCode() ?: 0)
        result = 31 * result + contentType.hashCode()
        result = 31 * result + (localImage?.hashCode() ?: 0)
        result = 31 * result + completedPreparationAttempts
        return result
    }
}
