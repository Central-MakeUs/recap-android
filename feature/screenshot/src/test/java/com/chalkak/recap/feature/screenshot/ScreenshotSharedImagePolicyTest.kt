package com.chalkak.recap.feature.screenshot

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotSharedImagePolicyTest {
    @Test
    fun fullscreenOwnershipSuppressesTargetBeforeTransitionBecomesActive() {
        assertTrue(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = true,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun activeTransitionKeepsTargetSuppressedAfterFullscreenPopCommits() {
        assertTrue(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = false,
                isSharedTransitionActive = true,
            ),
        )
    }

    @Test
    fun targetContentReturnsAfterTransitionCompletes() {
        assertFalse(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = true,
                fullscreenOwnsSharedImageRaster = false,
                isSharedTransitionActive = false,
            ),
        )
    }

    @Test
    fun disabledSharedBoundsNeverSuppressesTargetContent() {
        assertFalse(
            shouldSuppressSharedImageContent(
                enableSharedImageBounds = false,
                fullscreenOwnsSharedImageRaster = true,
                isSharedTransitionActive = true,
            ),
        )
    }
}
