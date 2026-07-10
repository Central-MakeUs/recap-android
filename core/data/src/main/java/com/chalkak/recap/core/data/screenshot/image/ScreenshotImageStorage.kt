package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import timber.log.Timber

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
        return try {
            val targetFile = buildImagePath(imageId)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            targetFile.absolutePath
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
    }

    fun clearStoredImages() {
        clearDirectory(resolveImagesDirectory())
        clearDirectory(resolveThumbnailsDirectory())
    }

    fun deleteStoredImages(imageIds: Set<String>) {
        deleteFiles(resolveImagesDirectory(), imageIds)
        deleteFiles(resolveThumbnailsDirectory(), imageIds)
    }

    private fun deleteFiles(directory: File, imageIds: Set<String>) {
        val canonicalDirectory = runCatching { directory.canonicalFile }.getOrNull() ?: return
        imageIds.forEach { imageId ->
            runCatching {
                val candidate = File(canonicalDirectory, imageId).canonicalFile
                when {
                    candidate.parentFile != canonicalDirectory -> {
                        Timber.w("Skipped screenshot file deletion outside managed directory")
                    }

                    candidate.isFile && !candidate.delete() -> {
                        Timber.w("Failed to delete a stored screenshot file")
                    }
                }
            }.onFailure { throwable ->
                Timber.w(throwable, "Failed to resolve a stored screenshot file for deletion")
            }
        }
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
