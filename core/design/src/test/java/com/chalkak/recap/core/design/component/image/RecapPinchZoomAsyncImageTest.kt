package com.chalkak.recap.core.design.component.image

import androidx.compose.ui.geometry.Offset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecapPinchZoomAsyncImageTest {

    @Test
    fun `clamp limits left empty area to 20 percent of viewport`() {
        val clamped = clampPinchZoomOffset(
            offset = Offset(10_000f, 0f),
            visualWidth = 2_000f,
            visualHeight = 2_000f,
            viewportWidth = ViewportWidth,
            viewportHeight = ViewportHeight,
            restCenter = ViewportCenter,
        )

        assertEquals(700f, clamped.x, Delta)
        assertEquals(0.2f * ViewportWidth, emptyStart(clamped.x, 2_000f, ViewportWidth), Delta)
    }

    @Test
    fun `clamp limits right empty area to 20 percent of viewport`() {
        val clamped = clampPinchZoomOffset(
            offset = Offset(-10_000f, 0f),
            visualWidth = 2_000f,
            visualHeight = 2_000f,
            viewportWidth = ViewportWidth,
            viewportHeight = ViewportHeight,
            restCenter = ViewportCenter,
        )

        assertEquals(-700f, clamped.x, Delta)
        assertEquals(
            0.2f * ViewportWidth,
            emptyEnd(clamped.x, 2_000f, ViewportWidth),
            Delta,
        )
    }

    @Test
    fun `clamp limits top empty area to 20 percent of viewport`() {
        val clamped = clampPinchZoomOffset(
            offset = Offset(0f, 10_000f),
            visualWidth = 2_000f,
            visualHeight = 3_000f,
            viewportWidth = ViewportWidth,
            viewportHeight = ViewportHeight,
            restCenter = ViewportCenter,
        )

        assertEquals(900f, clamped.y, Delta)
        assertEquals(0.2f * ViewportHeight, emptyStart(clamped.y, 3_000f, ViewportHeight), Delta)
    }

    @Test
    fun `clamp limits bottom empty area to 20 percent of viewport`() {
        val clamped = clampPinchZoomOffset(
            offset = Offset(0f, -10_000f),
            visualWidth = 2_000f,
            visualHeight = 3_000f,
            viewportWidth = ViewportWidth,
            viewportHeight = ViewportHeight,
            restCenter = ViewportCenter,
        )

        assertEquals(-900f, clamped.y, Delta)
        assertEquals(
            0.2f * ViewportHeight,
            emptyEnd(clamped.y, 3_000f, ViewportHeight),
            Delta,
        )
    }

    @Test
    fun `clamp keeps rest offset when image is too small to fill 60 percent`() {
        val clamped = clampPinchZoomOffset(
            offset = Offset(400f, -300f),
            visualWidth = 400f,
            visualHeight = 500f,
            viewportWidth = ViewportWidth,
            viewportHeight = ViewportHeight,
            restCenter = ViewportCenter,
        )

        assertEquals(0f, clamped.x, Delta)
        assertEquals(0f, clamped.y, Delta)
    }

    @Test
    fun `clamp leaves in-range pan unchanged`() {
        val clamped = clampPinchZoomOffset(
            offset = Offset(120f, -80f),
            visualWidth = 2_000f,
            visualHeight = 3_000f,
            viewportWidth = ViewportWidth,
            viewportHeight = ViewportHeight,
            restCenter = ViewportCenter,
        )

        assertEquals(120f, clamped.x, Delta)
        assertEquals(-80f, clamped.y, Delta)
    }

    @Test
    fun `pinchZoomFitSize letterboxes a tall image`() {
        val (width, height) = pinchZoomFitSize(
            availableWidth = 800f,
            availableHeight = 1_600f,
            imageAspectRatio = 0.5f,
        )

        assertEquals(800f, width, Delta)
        assertEquals(1_600f, height, Delta)
    }

    @Test
    fun `pinchZoomFitSize letterboxes a wide image`() {
        val (width, height) = pinchZoomFitSize(
            availableWidth = 800f,
            availableHeight = 1_600f,
            imageAspectRatio = 2f,
        )

        assertEquals(800f, width, Delta)
        assertEquals(400f, height, Delta)
    }

    private fun emptyStart(offset: Float, visualSize: Float, viewportSize: Float): Float {
        val restStart = viewportSize / 2f - visualSize / 2f
        return (restStart + offset).coerceAtLeast(0f)
    }

    private fun emptyEnd(offset: Float, visualSize: Float, viewportSize: Float): Float {
        val restEnd = viewportSize / 2f + visualSize / 2f
        return (viewportSize - (restEnd + offset)).coerceAtLeast(0f)
    }

    private companion object {
        const val ViewportWidth = 1_000f
        const val ViewportHeight = 2_000f
        const val Delta = 0.001f
        val ViewportCenter = Offset(ViewportWidth / 2f, ViewportHeight / 2f)
    }
}
