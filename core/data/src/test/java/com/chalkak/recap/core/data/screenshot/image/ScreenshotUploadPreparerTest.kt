package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ScreenshotUploadPreparerTest {
    private lateinit var context: Context
    private lateinit var preparer: ScreenshotUploadPreparer

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        preparer = ScreenshotUploadPreparer(context).apply {
            ioDispatcher = Dispatchers.Unconfined
        }
    }

    @Test
    fun `prepare converts png to jpeg bytes`() = runTest {
        val source = writeSolidPng(fileName = "sample.png", width = 40, height = 30)

        val prepared = preparer.prepare(localImage(source))

        assertEquals(PreparedScreenshot.MIME_TYPE_JPEG, prepared.mimeType)
        assertJpegDecodable(prepared.jpegBytes, expectedWidth = 40, expectedHeight = 30)
    }

    @Test
    fun `prepare converts jpeg to jpeg bytes preserving size`() = runTest {
        val source = writeSolidJpeg(fileName = "sample.jpg", width = 50, height = 20)

        val prepared = preparer.prepare(localImage(source))

        assertJpegDecodable(prepared.jpegBytes, expectedWidth = 50, expectedHeight = 20)
    }

    @Test
    fun `prepare applies exif orientation to output dimensions`() = runTest {
        val source = writeSolidJpeg(fileName = "oriented.jpg", width = 60, height = 20)
        ExifInterface(source.absolutePath).apply {
            setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90.toString(),
            )
            saveAttributes()
        }

        val prepared = preparer.prepare(localImage(source))

        assertJpegDecodable(prepared.jpegBytes, expectedWidth = 20, expectedHeight = 60)
    }

    @Test
    fun `exif transforms preserve pixel positions for rotate and flip orientations`() {
        val source = bitmapOf(
            width = 2,
            colors = intArrayOf(
                Color.RED, Color.GREEN,
                Color.BLUE, Color.YELLOW,
                Color.CYAN, Color.MAGENTA,
            ),
        )

        val rotated90 = preparer.applyExifOrientation(
            source,
            ExifInterface.ORIENTATION_ROTATE_90,
        )
        assertPixels(
            rotated90,
            width = 3,
            colors = intArrayOf(
                Color.CYAN, Color.BLUE, Color.RED,
                Color.MAGENTA, Color.YELLOW, Color.GREEN,
            ),
        )

        val rotated180 = preparer.applyExifOrientation(
            source,
            ExifInterface.ORIENTATION_ROTATE_180,
        )
        assertPixels(
            rotated180,
            width = 2,
            colors = intArrayOf(
                Color.MAGENTA, Color.CYAN,
                Color.YELLOW, Color.BLUE,
                Color.GREEN, Color.RED,
            ),
        )

        val flipped = preparer.applyExifOrientation(
            source,
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
        )
        assertPixels(
            flipped,
            width = 2,
            colors = intArrayOf(
                Color.GREEN, Color.RED,
                Color.YELLOW, Color.BLUE,
                Color.MAGENTA, Color.CYAN,
            ),
        )

        val square = bitmapOf(
            width = 2,
            colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW),
        )
        val squareRotated90 = preparer.applyExifOrientation(
            square,
            ExifInterface.ORIENTATION_ROTATE_90,
        )
        assertPixels(
            squareRotated90,
            width = 2,
            colors = intArrayOf(Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN),
        )

        rotated90.recycle()
        rotated180.recycle()
        flipped.recycle()
        squareRotated90.recycle()
        square.recycle()
        source.recycle()
    }

    @Test
    fun `prepare flattens alpha onto white background`() = runTest {
        // Robolectric PNG round-trip may drop alpha; exercise flatten with an in-memory ARGB bitmap.
        val source = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        for (y in 0 until 8) {
            for (x in 0 until 8) {
                source.setPixel(
                    x,
                    y,
                    if (x < 4) Color.TRANSPARENT else Color.RED,
                )
            }
        }

        val flattened = preparer.flattenOntoWhite(source)
        assertEquals(Color.WHITE, flattened.getPixel(0, 0))
        assertEquals(Color.RED, flattened.getPixel(7, 0))
        flattened.recycle()
        source.recycle()
    }

    @Test
    fun `prepare uses jpeg quality 75`() = runTest {
        val source = writeSolidJpeg(fileName = "quality.jpg", width = 16, height = 16)
        val seenQuality = AtomicInteger(-1)
        preparer.jpegCompressor = JpegBitmapCompressor { bitmap, quality ->
            seenQuality.set(quality)
            DefaultJpegBitmapCompressor.compress(bitmap, quality)
        }

        preparer.prepare(localImage(source))

        assertEquals(ScreenshotUploadPreparer.JPEG_QUALITY, seenQuality.get())
        assertEquals(75, ScreenshotUploadPreparer.JPEG_QUALITY)
    }

    @Test
    fun `prepare throws for missing uri content`() = runTest {
        try {
            preparer.prepare(
                LocalImage(
                    uri = "file:///missing/does-not-exist.png",
                    displayName = "missing.png",
                    dateAddedMillis = 0L,
                ),
            )
            fail("Expected ScreenshotUploadPrepareException")
        } catch (_: ScreenshotUploadPrepareException) {
            // expected
        }
    }

    @Test
    fun `prepare propagates cancellation`() = runTest {
        val source = writeSolidJpeg(fileName = "cancel.jpg", width = 8, height = 8)
        preparer.jpegCompressor = JpegBitmapCompressor { _, _ ->
            throw CancellationException("cancelled during compress")
        }
        val job = async {
            preparer.prepare(localImage(source))
        }
        try {
            job.await()
            fail("Expected CancellationException")
        } catch (_: CancellationException) {
            // expected
        } finally {
            job.cancelAndJoin()
        }
    }

    @Test
    fun `prepare serializes concurrent full resolution work`() = runTest {
        val firstSource = writeSolidJpeg(fileName = "serial-first.jpg", width = 8, height = 8)
        val secondSource = writeSolidJpeg(fileName = "serial-second.jpg", width = 8, height = 8)
        val firstCompressionStarted = CountDownLatch(1)
        val releaseFirstCompression = CountDownLatch(1)
        val secondCompressionStarted = CountDownLatch(1)
        val compressionCalls = AtomicInteger(0)
        preparer.ioDispatcher = Dispatchers.Default
        preparer.jpegCompressor = JpegBitmapCompressor { bitmap, quality ->
            when (compressionCalls.incrementAndGet()) {
                1 -> {
                    firstCompressionStarted.countDown()
                    assertTrue(releaseFirstCompression.await(5, TimeUnit.SECONDS))
                }

                2 -> secondCompressionStarted.countDown()
            }
            DefaultJpegBitmapCompressor.compress(bitmap, quality)
        }

        val first = async(Dispatchers.Default) {
            preparer.prepare(localImage(firstSource))
        }
        assertTrue(firstCompressionStarted.await(5, TimeUnit.SECONDS))
        val second = async(Dispatchers.Default) {
            preparer.prepare(localImage(secondSource))
        }

        assertFalse(secondCompressionStarted.await(200, TimeUnit.MILLISECONDS))
        releaseFirstCompression.countDown()
        first.await()
        second.await()

        assertTrue(secondCompressionStarted.await(5, TimeUnit.SECONDS))
        assertEquals(2, compressionCalls.get())
    }

    @Test
    fun `prepare heic path produces jpeg`() = runTest {
        val fixture = copyFixture("sample.heic")
        val prepared = preparer.prepare(localImage(fixture, displayName = fixture.name))
        assertJpegDecodable(prepared.jpegBytes)
    }

    @Test
    fun `prepare heif path produces jpeg`() = runTest {
        val fixture = copyFixture("sample.heif")
        val prepared = preparer.prepare(localImage(fixture, displayName = fixture.name))
        assertJpegDecodable(prepared.jpegBytes)
    }

    private fun copyFixture(fileName: String): File {
        val resourceStream = requireNotNull(
            javaClass.classLoader?.getResourceAsStream("image_fixtures/$fileName"),
        ) {
            "Missing required image fixture: $fileName"
        }
        val target = File(context.cacheDir, fileName)
        resourceStream.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }

    private fun localImage(file: File, displayName: String = file.name): LocalImage {
        return LocalImage(
            uri = file.toURI().toString(),
            displayName = displayName,
            dateAddedMillis = 1L,
        )
    }

    private fun writeSolidJpeg(fileName: String, width: Int, height: Int): File {
        val file = File(context.cacheDir, fileName)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLUE)
        file.outputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output))
        }
        bitmap.recycle()
        return file
    }

    private fun writeSolidPng(fileName: String, width: Int, height: Int): File {
        val file = File(context.cacheDir, fileName)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)
        file.outputStream().use { output ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
        bitmap.recycle()
        return file
    }

    private fun assertJpegDecodable(
        bytes: ByteArray,
        expectedWidth: Int? = null,
        expectedHeight: Int? = null,
    ) {
        assertTrue(bytes.isNotEmpty())
        assertTrue(bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte())
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertTrue(decoded != null)
        if (expectedWidth != null) {
            assertEquals(expectedWidth, decoded!!.width)
        }
        if (expectedHeight != null) {
            assertEquals(expectedHeight, decoded!!.height)
        }
        decoded?.recycle()
    }

    private fun bitmapOf(width: Int, colors: IntArray): Bitmap {
        val height = colors.size / width
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(colors, 0, width, 0, 0, width, height)
        }
    }

    private fun assertPixels(bitmap: Bitmap, width: Int, colors: IntArray) {
        assertEquals(width, bitmap.width)
        assertEquals(colors.size / width, bitmap.height)
        val actual = IntArray(colors.size)
        bitmap.getPixels(actual, 0, width, 0, 0, width, bitmap.height)
        assertTrue(
            "Expected=${colors.contentToString()} actual=${actual.contentToString()}",
            colors.contentEquals(actual),
        )
    }
}
