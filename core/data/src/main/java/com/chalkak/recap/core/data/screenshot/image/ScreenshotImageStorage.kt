package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotImageStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun resolveImagesDirectory(): File {
        return File(context.filesDir, IMAGES_DIRECTORY).also { directory ->
            directory.mkdirs()
        }
    }

    fun resolveThumbnailsDirectory(): File {
        return File(context.filesDir, THUMBNAILS_DIRECTORY).also { directory ->
            directory.mkdirs()
        }
    }

    fun buildImagePath(imageId: String): File {
        return File(resolveImagesDirectory(), imageId)
    }

    fun buildThumbnailPath(imageId: String): File {
        return File(resolveThumbnailsDirectory(), imageId)
    }

    fun copyImageFromUri(imageId: String, sourceUri: Uri): String? {
        return runCatching {
            val targetFile = buildImagePath(imageId)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            targetFile.absolutePath
        }.getOrNull()
    }

    fun clearStoredImages() {
        clearDirectory(resolveImagesDirectory())
        clearDirectory(resolveThumbnailsDirectory())
    }

    private fun clearDirectory(directory: File) {
        runCatching {
            directory.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        }
    }

    private companion object {
        const val IMAGES_DIRECTORY = "recap/images"
        const val THUMBNAILS_DIRECTORY = "recap/thumbnails"
    }
}
