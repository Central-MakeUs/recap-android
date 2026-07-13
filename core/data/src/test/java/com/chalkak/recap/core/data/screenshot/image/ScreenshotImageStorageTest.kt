package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class ScreenshotImageStorageTest {
    private lateinit var context: Context
    private lateinit var storage: ScreenshotImageStorage

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        storage = ScreenshotImageStorage(context)
        storage.clearStoredImages()
    }

    @Test
    fun resolveImagesDirectory_createsRecapImagesDirectoryUnderFiles() {
        val imagesDirectory = storage.resolveImagesDirectory()

        assertTrue(imagesDirectory.isDirectory)
        assertEquals(
            File(context.filesDir, "recap/images"),
            imagesDirectory,
        )
    }

    @Test
    fun resolveThumbnailsDirectory_createsRecapThumbnailsDirectoryUnderFiles() {
        val thumbnailsDirectory = storage.resolveThumbnailsDirectory()

        assertTrue(thumbnailsDirectory.isDirectory)
        assertEquals(
            File(context.filesDir, "recap/thumbnails"),
            thumbnailsDirectory,
        )
    }

    @Test
    fun buildImagePath_returnsStablePathForImageId() {
        val imagePath = storage.buildImagePath("image-123")

        assertEquals(
            File(storage.resolveImagesDirectory(), "image-123"),
            imagePath,
        )
    }

    @Test
    fun buildThumbnailPath_returnsJpgPathForImageId() {
        val thumbnailPath = storage.buildThumbnailPath("image-123")

        assertEquals(
            File(storage.resolveThumbnailsDirectory(), "image-123.jpg"),
            thumbnailPath,
        )
    }

    @Test
    fun createThumbnailFromUri_writesHalfSizeJpegPreservingAspectRatio() {
        val sourceFile = writeSolidJpeg(
            fileName = "source-1080x2400.jpg",
            width = 1080,
            height = 2400,
        )

        val thumbnailPath = storage.createThumbnailFromUri(
            imageId = "image-half",
            sourceUri = Uri.fromFile(sourceFile),
        )

        assertNotNull(thumbnailPath)
        val thumbnailFile = File(requireNotNull(thumbnailPath))
        assertTrue(thumbnailFile.exists())
        assertEquals(storage.buildThumbnailPath("image-half").absolutePath, thumbnailPath)
        assertTrue(isJpegFile(thumbnailFile))

        val bounds = decodeBounds(thumbnailFile)
        assertEquals(540, bounds.width)
        assertEquals(1200, bounds.height)
    }

    @Test
    fun createThumbnailFromUri_appliesExifOrientationBeforeHalving() {
        val sourceFile = writeSolidJpeg(
            fileName = "source-oriented-200x100.jpg",
            width = 200,
            height = 100,
        )
        ExifInterface(sourceFile.absolutePath).apply {
            setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90.toString(),
            )
            saveAttributes()
        }

        val thumbnailPath = storage.createThumbnailFromUri(
            imageId = "image-oriented",
            sourceUri = Uri.fromFile(sourceFile),
        )

        assertNotNull(thumbnailPath)
        val bounds = decodeBounds(File(requireNotNull(thumbnailPath)))
        assertEquals(50, bounds.width)
        assertEquals(100, bounds.height)
    }

    @Test
    fun createThumbnailFromUri_roundsOddPixelsAndGuaranteesMinimumOnePx() {
        val oddSource = writeSolidJpeg(
            fileName = "source-odd.jpg",
            width = 1081,
            height = 2401,
        )
        val oddThumbnailPath = storage.createThumbnailFromUri(
            imageId = "image-odd",
            sourceUri = Uri.fromFile(oddSource),
        )
        val oddBounds = decodeBounds(File(requireNotNull(oddThumbnailPath)))
        assertEquals(541, oddBounds.width)
        assertEquals(1201, oddBounds.height)

        val tinySource = writeSolidJpeg(
            fileName = "source-1x1.jpg",
            width = 1,
            height = 1,
        )
        val tinyThumbnailPath = storage.createThumbnailFromUri(
            imageId = "image-tiny",
            sourceUri = Uri.fromFile(tinySource),
        )
        val tinyBounds = decodeBounds(File(requireNotNull(tinyThumbnailPath)))
        assertEquals(1, tinyBounds.width)
        assertEquals(1, tinyBounds.height)
    }

    @Test
    fun createThumbnailFromUri_downsamplesLargeSourceToHalfSize() {
        val sourceFile = writeSolidJpeg(
            fileName = "source-1440x3200.jpg",
            width = 1440,
            height = 3200,
        )

        val thumbnailPath = storage.createThumbnailFromUri(
            imageId = "image-large",
            sourceUri = Uri.fromFile(sourceFile),
        )

        assertNotNull(thumbnailPath)
        val bounds = decodeBounds(File(requireNotNull(thumbnailPath)))
        assertEquals(720, bounds.width)
        assertEquals(1600, bounds.height)
    }

    @Test
    fun createThumbnailFromStoredImage_doesNotReopenOriginalUri() {
        val imageId = "image-stored"
        val sourceFile = writeSolidJpeg(
            fileName = "source-stored-1080x2400.jpg",
            width = 1080,
            height = 2400,
        )
        sourceFile.copyTo(storage.buildImagePath(imageId), overwrite = true)

        val thumbnailPath = storage.createThumbnailFromStoredImage(imageId)

        assertNotNull(thumbnailPath)
        val bounds = decodeBounds(File(requireNotNull(thumbnailPath)))
        assertEquals(540, bounds.width)
        assertEquals(1200, bounds.height)
    }

    @Test
    fun createThumbnailFromUri_publishFailurePreservesExistingTarget() {
        val imageId = "image-preserve"
        val sourceFile = writeSolidJpeg(
            fileName = "source-preserve.jpg",
            width = 200,
            height = 400,
        )
        val createdPath = storage.createThumbnailFromUri(
            imageId = imageId,
            sourceUri = Uri.fromFile(sourceFile),
        )
        assertNotNull(createdPath)
        val targetFile = storage.buildThumbnailPath(imageId)
        val originalBytes = targetFile.readBytes()

        val backupBlocker = File(targetFile.parentFile, "${targetFile.name}.bak")
        assertTrue(backupBlocker.mkdir())
        File(backupBlocker, "blocker").writeText("block")

        val newerSource = writeSolidJpeg(
            fileName = "source-preserve-new.jpg",
            width = 300,
            height = 600,
        )
        val result = storage.createThumbnailFromUri(
            imageId = imageId,
            sourceUri = Uri.fromFile(newerSource),
        )

        assertNull(result)
        assertTrue(targetFile.exists())
        assertTrue(originalBytes.contentEquals(targetFile.readBytes()))
        assertFalse(
            storage.resolveThumbnailsDirectory()
                .listFiles()
                ?.any { it.isFile && it.name.endsWith(".tmp") }
                ?: false,
        )

        File(backupBlocker, "blocker").delete()
        backupBlocker.delete()
    }

    @Test
    fun createThumbnailFromUri_failureLeavesNoTempOrResultFiles() {
        val missingUri = Uri.fromFile(File(context.cacheDir, "missing-source.jpg"))

        val thumbnailPath = storage.createThumbnailFromUri(
            imageId = "image-missing",
            sourceUri = missingUri,
        )

        assertNull(thumbnailPath)
        assertFalse(storage.buildThumbnailPath("image-missing").exists())
        val leftoverTemps = storage.resolveThumbnailsDirectory()
            .listFiles()
            ?.filter { it.name.endsWith(".tmp") }
            .orEmpty()
        assertTrue(leftoverTemps.isEmpty())
    }

    @Test
    fun clearStoredImages_deletesFilesUnderRecapDirectories() {
        val imageFile = storage.buildImagePath("image-1").apply {
            parentFile?.mkdirs()
            writeText("image")
        }
        val thumbnailFile = storage.buildThumbnailPath("image-1").apply {
            parentFile?.mkdirs()
            writeText("thumbnail")
        }

        storage.clearStoredImages()

        assertFalse(imageFile.exists())
        assertFalse(thumbnailFile.exists())
    }

    @Test
    fun deleteStoredImages_deletesOnlySelectedImageAndJpgThumbnailFiles() {
        val selectedImage = storage.buildImagePath("selected").apply { writeText("image") }
        val selectedThumbnail = storage.buildThumbnailPath("selected").apply {
            writeText("thumbnail")
        }
        val keptImage = storage.buildImagePath("kept").apply { writeText("image") }
        val keptThumbnail = storage.buildThumbnailPath("kept").apply { writeText("thumbnail") }

        storage.deleteStoredImages(setOf("selected"))

        assertFalse(selectedImage.exists())
        assertFalse(selectedThumbnail.exists())
        assertTrue(keptImage.exists())
        assertTrue(keptThumbnail.exists())
        assertEquals("selected.jpg", selectedThumbnail.name)
    }

    @Test
    fun deleteStoredImages_rejectsTraversalAndContinuesDeletingValidFiles() {
        val outsideFile = File(storage.resolveImagesDirectory().parentFile, "outside").apply {
            writeText("outside")
        }
        val selectedImage = storage.buildImagePath("selected").apply { writeText("image") }
        val selectedThumbnail = storage.buildThumbnailPath("selected").apply {
            writeText("thumbnail")
        }

        storage.deleteStoredImages(linkedSetOf("../outside", "selected"))

        assertTrue(outsideFile.exists())
        assertFalse(selectedImage.exists())
        assertFalse(selectedThumbnail.exists())
        outsideFile.delete()
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

    private fun decodeBounds(file: File): BitmapFactory.Options {
        return BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, this)
        }
    }

    private fun isJpegFile(file: File): Boolean {
        val header = file.inputStream().use { input ->
            ByteArray(3).also { bytes ->
                assertEquals(3, input.read(bytes))
            }
        }
        return header[0] == 0xFF.toByte() &&
            header[1] == 0xD8.toByte() &&
            header[2] == 0xFF.toByte()
    }

    private val BitmapFactory.Options.width: Int
        get() = outWidth

    private val BitmapFactory.Options.height: Int
        get() = outHeight
}
