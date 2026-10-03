package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.createBitmap
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

class ScreenshotUploadPrepareException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

fun interface JpegBitmapCompressor {
    fun compress(bitmap: Bitmap, quality: Int): ByteArray
}

object DefaultJpegBitmapCompressor : JpegBitmapCompressor {
    override fun compress(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        if (!compressed) {
            throw ScreenshotUploadPrepareException("JPEG compress returned false")
        }
        val bytes = stream.toByteArray()
        if (bytes.isEmpty()) {
            throw ScreenshotUploadPrepareException("JPEG compress produced empty bytes")
        }
        return bytes
    }
}

@Singleton
class ScreenshotUploadPreparer @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val preparationMutex = Mutex()

    @VisibleForTesting
    internal var ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @VisibleForTesting
    internal var jpegCompressor: JpegBitmapCompressor = DefaultJpegBitmapCompressor

    suspend fun prepare(image: LocalImage): PreparedScreenshot = preparationMutex.withLock {
        prepareOnIo(image)
    }

    private suspend fun prepareOnIo(
        image: LocalImage,
    ): PreparedScreenshot = withContext(ioDispatcher) {
        val uri = parseUri(image.uri)
        var decoded: Bitmap? = null
        var flattened: Bitmap? = null
        var oriented: Bitmap? = null
        Timber.d(
            "Preparing screenshot upload jpeg displayName=%s uri=%s quality=%d",
            image.displayName,
            image.uri,
            JPEG_QUALITY,
        )
        try {
            val bytes = readUriBytes(uri)
            if (bytes.isEmpty()) {
                throw ScreenshotUploadPrepareException("Screenshot bytes are empty uri=$uri")
            }
            val decodedResult = decodeBitmap(bytes, uri)
            decoded = decodedResult.bitmap
            if (decoded.width <= 0 || decoded.height <= 0) {
                throw ScreenshotUploadPrepareException("Decoded bitmap has invalid size uri=$uri")
            }
            coroutineContext.ensureActive()

            flattened = flattenOntoWhite(decoded)
            if (flattened !== decoded) {
                decoded.recycle()
                decoded = null
            }
            coroutineContext.ensureActive()

            oriented = ensureExifOrientation(
                source = flattened,
                bytes = bytes,
                decoderAppliedExifOrientation = decodedResult.appliedExifOrientation,
            )
            if (oriented !== flattened) {
                if (decoded === flattened) {
                    decoded = null
                }
                flattened.recycle()
                flattened = null
            }
            coroutineContext.ensureActive()

            val jpegBytes = jpegCompressor.compress(oriented, JPEG_QUALITY)
            Timber.d(
                "Prepared screenshot upload jpeg displayName=%s uri=%s " +
                        "inputBytes=%d outputBytes=%d size=%dx%d decoderExif=%s",
                image.displayName,
                image.uri,
                bytes.size,
                jpegBytes.size,
                oriented.width,
                oriented.height,
                decodedResult.appliedExifOrientation,
            )
            PreparedScreenshot(
                localImage = image,
                jpegBytes = jpegBytes,
                mimeType = PreparedScreenshot.MIME_TYPE_JPEG,
            )
        } catch (cancellation: CancellationException) {
            Timber.d(
                "Cancelled screenshot upload jpeg preparation displayName=%s uri=%s",
                image.displayName,
                image.uri,
            )
            throw cancellation
        } catch (error: ScreenshotUploadPrepareException) {
            Timber.w(
                error,
                "Failed screenshot upload jpeg preparation displayName=%s uri=%s",
                image.displayName,
                image.uri,
            )
            throw error
        } catch (error: Exception) {
            val wrapped = ScreenshotUploadPrepareException(
                message = "Failed to prepare screenshot uri=${image.uri}",
                cause = error,
            )
            Timber.w(
                wrapped,
                "Failed screenshot upload jpeg preparation displayName=%s uri=%s",
                image.displayName,
                image.uri,
            )
            throw wrapped
        } finally {
            if (oriented !== flattened && oriented !== decoded) {
                oriented?.recycle()
            }
            if (flattened !== decoded) {
                flattened?.recycle()
            }
            decoded?.recycle()
        }
    }

    private fun decodeBitmap(bytes: ByteArray, uri: Uri): DecodedBitmap {
        val bitmapFactoryOptions = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val fromFactory = BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            bitmapFactoryOptions,
        )
        if (fromFactory != null) {
            return DecodedBitmap(
                bitmap = fromFactory,
                appliedExifOrientation = false,
            )
        }

        val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
        return try {
            DecodedBitmap(
                bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                },
                appliedExifOrientation = true,
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            throw ScreenshotUploadPrepareException(
                message = "Failed to decode screenshot uri=$uri",
                cause = error,
            )
        }
    }

    private fun ensureExifOrientation(
        source: Bitmap,
        bytes: ByteArray,
        decoderAppliedExifOrientation: Boolean,
    ): Bitmap {
        if (decoderAppliedExifOrientation) {
            return source
        }
        val orientation = readExifOrientation(bytes)
        return applyExifOrientation(source, orientation)
    }

    private fun readUriBytes(uri: Uri): ByteArray {
        return try {
            if (uri.scheme.equals("file", ignoreCase = true)) {
                uri.toFile().readBytes()
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes()
                } ?: throw ScreenshotUploadPrepareException("Unable to open screenshot uri=$uri")
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: ScreenshotUploadPrepareException) {
            throw error
        } catch (error: Exception) {
            throw ScreenshotUploadPrepareException(
                message = "Failed to read screenshot uri=$uri",
                cause = error,
            )
        }
    }

    @VisibleForTesting
    internal fun flattenOntoWhite(source: Bitmap): Bitmap {
        if (!source.hasAlpha()) {
            return source
        }
        val flattened = createBitmap(source.width, source.height)
        try {
            val canvas = Canvas(flattened)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(source, 0f, 0f, null)
        } catch (error: Throwable) {
            flattened.recycle()
            throw error
        }
        return flattened
    }

    private fun readExifOrientation(bytes: ByteArray): Int {
        return runCatching {
            ExifInterface(ByteArrayInputStream(bytes)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    @VisibleForTesting
    internal fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.preScale(1f, -1f)
            }

            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(90f)
                matrix.preScale(-1f, 1f)
            }

            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return source
        }

        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            false,
        )
    }

    private fun parseUri(uriString: String): Uri {
        if (uriString.isBlank()) {
            throw ScreenshotUploadPrepareException("Image uri is blank")
        }
        return uriString.toUri()
    }

    companion object {
        const val JPEG_QUALITY = 75
    }

    private data class DecodedBitmap(
        val bitmap: Bitmap,
        val appliedExifOrientation: Boolean,
    )
}
