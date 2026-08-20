package com.chalkak.recap.feature.screenshot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotSharedImagePolicyTest {
    @Test
    fun `pending fullscreen handoff keeps source raster visible`() {
        assertFalse(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = false,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun `active transition suppresses detail or edit raster`() {
        assertTrue(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = false,
                isSharedTransitionActive = true,
            ),
        )
    }

    @Test
    fun `completed handoff keeps resting fullscreen as single raster owner`() {
        assertTrue(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = true,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun `detail or edit raster returns after fullscreen pop completes`() {
        assertFalse(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = false,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun `disabled shared bounds never suppresses target content`() {
        assertFalse(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = false,
                fullscreenOwnsSharedImageRaster = true,
                isSharedTransitionActive = true,
            ),
        )
    }

    @Test
    fun `forward shared transition completes raster handoff`() {
        assertTrue(
            nextFullscreenRasterHandoffCompleted(
                currentValue = false,
                fullscreenIsTop = true,
                isSharedTransitionActive = true,
            ),
        )
    }

    @Test
    fun `fullscreen rest and predictive back cancellation keep raster handoff`() {
        assertTrue(
            nextFullscreenRasterHandoffCompleted(
                currentValue = true,
                fullscreenIsTop = true,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun `completed predictive back resets raster handoff`() {
        assertFalse(
            nextFullscreenRasterHandoffCompleted(
                currentValue = true,
                fullscreenIsTop = false,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun `restored resting fullscreen initializes raster ownership`() {
        assertTrue(initialFullscreenRasterHandoffCompleted(fullscreenIsTop = true))
    }

    @Test
    fun `detail or edit start does not initialize raster ownership`() {
        assertFalse(initialFullscreenRasterHandoffCompleted(fullscreenIsTop = false))
    }
}
