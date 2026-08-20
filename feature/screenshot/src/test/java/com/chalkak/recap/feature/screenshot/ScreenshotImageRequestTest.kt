package com.chalkak.recap.feature.screenshot

import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotImageRequestTest {
    @Test
    fun `decode size keeps a window that is within the pixel budget`() {
        assertEquals(
            IntSize(1440, 3000),
            boundedScreenshotDecodeSize(width = 1440, height = 3000),
        )
    }

    @Test
    fun `decode size preserves aspect ratio while enforcing the pixel budget`() {
        val result = boundedScreenshotDecodeSize(
            width = 2400,
            height = 3200,
            pixelBudget = 4_500_000,
        )

        assertTrue(result.width.toLong() * result.height <= 4_500_000)
        assertEquals(0.75f, result.width.toFloat() / result.height, 0.001f)
    }

    @Test
    fun `decode size guards invalid window dimensions`() {
        assertEquals(
            IntSize(1, 1),
            boundedScreenshotDecodeSize(width = 0, height = -10),
        )
    }
}
