package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap
import kotlin.math.sqrt

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

    fun buildImagePath(captureId: Long): File {
        return File(resolveImagesDirectory(), captureId.toString())
    }

    fun buildThumbnailPath(captureId: Long): File {
        return File(resolveThumbnailsDirectory(), "${captureId}$THUMBNAIL_EXTENSION")
    }

    fun copyImageFromUri(captureId: Long, sourceUri: Uri): String? {
        return try {
            val targetFile = buildImagePath(captureId)
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

    fun createThumbnailFromUri(captureId: Long, sourceUri: Uri): String? {
        return createThumbnail(captureId) {
            context.contentResolver.openInputStream(sourceUri)
        }
    }

    fun createThumbnailFromStoredImage(captureId: Long): String? {
        val storedImage = buildImagePath(captureId)
        return createThumbnail(captureId) {
            storedImage.inputStream()
        }
    }

    private fun createThumbnail(
        captureId: Long,
        openInputStream: () -> InputStream?,
    ): String? {
        var decodedBitmap: Bitmap? = null
        var jpegBitmap: Bitmap? = null
        var tempFile: File? = null
        val targetFile = buildThumbnailPath(captureId)
        return try {
            resolveThumbnailsDirectory()
            decodedBitmap = decodeHalfSizeBitmap(openInputStream)
            jpegBitmap = flattenAlphaOnWhite(decodedBitmap)
            if (jpegBitmap !== decodedBitmap) {
                decodedBitmap.recycle()
                decodedBitmap = null
            }

            tempFile = File(
                requireNotNull(targetFile.parentFile),
                "${targetFile.name}$TEMP_SUFFIX",
            )
            if (tempFile.exists() && !tempFile.delete()) {
                Timber.w("Failed to clear thumbnail temp file before write")
                return null
            }

            tempFile.outputStream().use { output ->
                val compressed = jpegBitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    JPEG_QUALITY,
                    output,
                )
                if (!compressed) {
                    Timber.w("Failed to compress screenshot thumbnail")
                    return null
                }
            }

            if (!publishThumbnail(tempFile = tempFile, targetFile = targetFile)) {
                Timber.w("Failed to publish screenshot thumbnail")
                return null
            }
            tempFile = null
            Timber.d(
                "Created screenshot thumbnail captureId=%s path=%s size=%dx%d",
                captureId,
                targetFile.absolutePath,
                jpegBitmap.width,
                jpegBitmap.height,
            )
            targetFile.absolutePath
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Exception) {
            Timber.w(throwable, "Failed to create screenshot thumbnail")
            null
        } finally {
            if (jpegBitmap != null && jpegBitmap !== decodedBitmap) {
                jpegBitmap.recycle()
            }
            decodedBitmap?.recycle()
            tempFile?.let { leftover ->
                if (leftover.exists() && !leftover.delete()) {
                    Timber.w("Failed to delete leftover thumbnail temp file")
                }
            }
        }
    }

    /**
     * 저장된 원본/썸네일을 모두 지운다.
     * 계정 전환 wipe가 남은 파일을 새 소유자에게 넘기지 않도록, 삭제 실패를 삼키지 않고 알린다.
     *
     * @return 두 디렉터리를 완전히 비웠으면 true
     */
    fun clearStoredImages(): Boolean {
        val imagesCleared = clearDirectory(resolveImagesDirectory())
        val thumbnailsCleared = clearDirectory(resolveThumbnailsDirectory())
        return imagesCleared && thumbnailsCleared
    }

    fun deleteStoredImages(captureIds: Set<Long>) {
        deleteFiles(resolveImagesDirectory(), captureIds) { captureId -> captureId.toString() }
        deleteFiles(resolveThumbnailsDirectory(), captureIds) { captureId ->
            "${captureId}$THUMBNAIL_EXTENSION"
        }
    }

    fun cacheThumbnailBytes(captureId: Long, bytes: ByteArray): String? {
        if (bytes.isEmpty()) {
            return null
        }
        var tempFile: File? = null
        val targetFile = buildThumbnailPath(captureId)
        return try {
            resolveThumbnailsDirectory()
            tempFile = File(
                requireNotNull(targetFile.parentFile),
                "${targetFile.name}$TEMP_SUFFIX",
            )
            if (tempFile.exists() && !tempFile.delete()) {
                Timber.w("Failed to clear thumbnail temp file before remote cache write")
                return null
            }
            tempFile.outputStream().use { output ->
                output.write(bytes)
            }
            if (!publishThumbnail(tempFile = tempFile, targetFile = targetFile)) {
                Timber.w("Failed to publish remote thumbnail cache")
                return null
            }
            tempFile = null
            targetFile.absolutePath
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Exception) {
            Timber.w(throwable, "Failed to save remote thumbnail cache")
            null
        } finally {
            tempFile?.let { leftover ->
                if (leftover.exists() && !leftover.delete()) {
                    Timber.w("Failed to delete leftover remote thumbnail temp file")
                }
            }
        }
    }
    private fun decodeHalfSizeBitmap(openInputStream: () -> InputStream?): Bitmap {
        val orientation = readExifOrientation(openInputStream)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsInput = openInputStream() ?: error("Unable to open screenshot image")
        boundsInput.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            error("Invalid screenshot image bounds")
        }

        val (orientedWidth, orientedHeight) = orientedSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            orientation = orientation,
        )
        val (targetWidth, targetHeight) = screenshotThumbnailTargetSize(
            width = orientedWidth,
            height = orientedHeight,
        )
        val (rawTargetWidth, rawTargetHeight) = orientedSize(
            width = targetWidth,
            height = targetHeight,
            orientation = orientation,
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = thumbnailInSampleSize(
                rawWidth = bounds.outWidth,
                rawHeight = bounds.outHeight,
                targetWidth = rawTargetWidth,
                targetHeight = rawTargetHeight,
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = openInputStream()?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: error("Unable to decode screenshot image")

        val oriented = applyExifOrientation(decoded, orientation)
        if (oriented.width == targetWidth && oriented.height == targetHeight) {
            return oriented
        }
        val scaled = oriented.scale(targetWidth, targetHeight)
        if (scaled !== oriented) {
            oriented.recycle()
        }
        return scaled
    }

    /**
     * temp → target 교체. 기존 target은 backup으로 옮긴 뒤 성공 시에만 제거한다.
     * publish 실패 시 backup을 target으로 복구해 이전 정상 파일을 보존한다.
     */
    private fun publishThumbnail(tempFile: File, targetFile: File): Boolean {
        val parent = targetFile.parentFile ?: return false
        val backupFile = File(parent, "${targetFile.name}$BACKUP_SUFFIX")
        if (backupFile.exists() && !backupFile.delete()) {
            return false
        }

        val hadExisting = targetFile.exists()
        if (hadExisting && !targetFile.renameTo(backupFile)) {
            return false
        }

        if (!tempFile.renameTo(targetFile)) {
            if (hadExisting && backupFile.exists()) {
                if (!backupFile.renameTo(targetFile)) {
                    Timber.w("Failed to restore previous screenshot thumbnail after publish failure")
                }
            }
            return false
        }

        if (backupFile.exists() && !backupFile.delete()) {
            Timber.w("Failed to delete screenshot thumbnail backup after publish")
        }
        return true
    }

    private fun readExifOrientation(openInputStream: () -> InputStream?): Int {
        return runCatching {
            openInputStream()?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL
    }

    private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return source
        }
        val transformed = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true,
        )
        if (transformed !== source) {
            source.recycle()
        }
        return transformed
    }

    private fun flattenAlphaOnWhite(source: Bitmap): Bitmap {
        if (!source.hasAlpha()) {
            return source
        }
        val flattened = createBitmap(source.width, source.height)
        val canvas = Canvas(flattened)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(source, 0f, 0f, null)
        return flattened
    }

    private fun deleteFiles(
        directory: File,
        captureIds: Set<Long>,
        fileNameFor: (Long) -> String,
    ) {
        val canonicalDirectory = runCatching { directory.canonicalFile }.getOrNull() ?: return
        captureIds.forEach { captureId ->
            runCatching {
                val candidate = File(canonicalDirectory, fileNameFor(captureId)).canonicalFile
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

    private fun clearDirectory(directory: File): Boolean =
        runCatching {
            val files = directory.listFiles()
            if (files == null) {
                Timber.w("Unable to list a managed screenshot directory for clearing")
                return@runCatching false
            }
            files.fold(true) { cleared, file ->
                if (file.isFile && !file.delete()) {
                    Timber.w("Failed to delete a stored screenshot file while clearing")
                    false
                } else {
                    cleared
                }
            }
        }.getOrElse { throwable ->
            Timber.w(throwable, "Failed to clear a managed screenshot directory")
            false
        }

    private companion object {
        const val IMAGES_DIRECTORY = "recap/images"
        const val THUMBNAILS_DIRECTORY = "recap/thumbnails"
        const val THUMBNAIL_EXTENSION = ".jpg"
        const val TEMP_SUFFIX = ".tmp"
        const val BACKUP_SUFFIX = ".bak"
        const val JPEG_QUALITY = 80

        fun orientedSize(width: Int, height: Int, orientation: Int): Pair<Int, Int> {
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90,
                ExifInterface.ORIENTATION_ROTATE_270,
                ExifInterface.ORIENTATION_TRANSPOSE,
                ExifInterface.ORIENTATION_TRANSVERSE,
                -> height to width

                else -> width to height
            }
        }

        fun thumbnailInSampleSize(
            rawWidth: Int,
            rawHeight: Int,
            targetWidth: Int,
            targetHeight: Int,
        ): Int {
            var sampleSize = 1
            while (
                rawWidth / (sampleSize * 2) >= targetWidth &&
                rawHeight / (sampleSize * 2) >= targetHeight
            ) {
                sampleSize *= 2
            }
            return sampleSize
        }
    }
}

private const val ScreenshotThumbnailPixelBudget = 2_000_000L

internal fun screenshotThumbnailTargetSize(
    width: Int,
    height: Int,
    pixelBudget: Long = ScreenshotThumbnailPixelBudget,
): Pair<Int, Int> {
    val halfWidth = ((width + 1) / 2).coerceAtLeast(1)
    val halfHeight = ((height + 1) / 2).coerceAtLeast(1)
    val halfPixels = halfWidth.toLong() * halfHeight
    if (halfPixels <= pixelBudget) return halfWidth to halfHeight

    val scale = sqrt(pixelBudget.toDouble() / halfPixels.toDouble())
    return (halfWidth * scale).toInt().coerceAtLeast(1) to
        (halfHeight * scale).toInt().coerceAtLeast(1)
}
