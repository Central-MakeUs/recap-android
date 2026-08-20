package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.EnterExitState
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenshotFullscreenZoomPolicyTest {
    @Test
    fun `visible fullscreen keeps current pinch transform`() {
        assertEquals(
            0f,
            fullscreenZoomUnwindTarget(
                enterExitState = EnterExitState.Visible,
                hasCompletedSharedEntry = true,
            ),
            Delta,
        )
    }

    @Test
    fun `shared enter does not unwind zoom`() {
        assertEquals(
            0f,
            fullscreenZoomUnwindTarget(
                enterExitState = EnterExitState.PreEnter,
                hasCompletedSharedEntry = false,
            ),
            Delta,
        )
    }

    @Test
    fun `shared exit after entry unwinds zoom to Fit`() {
        assertEquals(
            1f,
            fullscreenZoomUnwindTarget(
                enterExitState = EnterExitState.PostExit,
                hasCompletedSharedEntry = true,
            ),
            Delta,
        )
    }

    @Test
    fun `pre-entry post exit state does not trigger unwind`() {
        assertEquals(
            0f,
            fullscreenZoomUnwindTarget(
                enterExitState = EnterExitState.PostExit,
                hasCompletedSharedEntry = false,
            ),
            Delta,
        )
    }

    private companion object {
        const val Delta = 0.001f
    }
}
