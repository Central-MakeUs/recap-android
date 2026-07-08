package com.chalkak.recap.core.data.screenshot.image

import android.content.Context
import org.junit.Assert.assertEquals
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
    fun buildThumbnailPath_returnsStablePathForImageId() {
        val thumbnailPath = storage.buildThumbnailPath("image-123")

        assertEquals(
            File(storage.resolveThumbnailsDirectory(), "image-123"),
            thumbnailPath,
        )
    }
}
