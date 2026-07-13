package com.chalkak.recap.feature.screenshot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenshotFormattersTest {
    @Test
    fun `preview priority prefers thumbnail over stored and source`() {
        val model = resolveScreenshotImageModel(
            storedImagePath = "/stored/image",
            sourceImageUri = "content://source",
            thumbnailPath = "/thumbs/image.jpg",
            priority = ScreenshotImageResolvePriority.Preview,
        )

        assertEquals("/thumbs/image.jpg", model)
    }

    @Test
    fun `preview priority falls back to stored then source`() {
        assertEquals(
            "/stored/image",
            resolveScreenshotImageModel(
                storedImagePath = "/stored/image",
                sourceImageUri = "content://source",
                thumbnailPath = null,
                priority = ScreenshotImageResolvePriority.Preview,
            ),
        )
        assertEquals(
            "content://source",
            resolveScreenshotImageModel(
                storedImagePath = null,
                sourceImageUri = "content://source",
                thumbnailPath = "   ",
                priority = ScreenshotImageResolvePriority.Preview,
            ),
        )
    }

    @Test
    fun `fullscreen priority prefers stored then source over thumbnail`() {
        val model = resolveScreenshotImageModel(
            storedImagePath = "/stored/image",
            sourceImageUri = "content://source",
            thumbnailPath = "/thumbs/image.jpg",
            priority = ScreenshotImageResolvePriority.Fullscreen,
        )

        assertEquals("/stored/image", model)
        assertEquals(
            "content://source",
            resolveScreenshotImageModel(
                storedImagePath = null,
                sourceImageUri = "content://source",
                thumbnailPath = "/thumbs/image.jpg",
                priority = ScreenshotImageResolvePriority.Fullscreen,
            ),
        )
        assertEquals(
            "/thumbs/image.jpg",
            resolveScreenshotImageModel(
                storedImagePath = "  ",
                sourceImageUri = null,
                thumbnailPath = "/thumbs/image.jpg",
                priority = ScreenshotImageResolvePriority.Fullscreen,
            ),
        )
    }

    @Test
    fun `blank and null candidates resolve to null`() {
        assertNull(
            resolveScreenshotImageModel(
                storedImagePath = null,
                sourceImageUri = "",
                thumbnailPath = " ",
                priority = ScreenshotImageResolvePriority.Preview,
            ),
        )
        assertNull(
            resolveScreenshotImageModel(
                storedImagePath = null,
                sourceImageUri = null,
                thumbnailPath = null,
                priority = ScreenshotImageResolvePriority.Fullscreen,
            ),
        )
    }
}
