package com.chalkak.recap.app.share

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareImageFormatFilterTest {
    @Test
    fun `accepts png jpeg heic heif mime types`() {
        assertTrue(ShareImageFormatFilter.isAccepted("image/png", null))
        assertTrue(ShareImageFormatFilter.isAccepted("image/jpeg", null))
        assertTrue(ShareImageFormatFilter.isAccepted("image/jpg", null))
        assertTrue(ShareImageFormatFilter.isAccepted("image/heic", null))
        assertTrue(ShareImageFormatFilter.isAccepted("image/heif", null))
        assertTrue(ShareImageFormatFilter.isAccepted("IMAGE/PNG", "photo.webp"))
    }

    @Test
    fun `accepts by extension when mime is missing`() {
        assertTrue(ShareImageFormatFilter.isAccepted(null, "shot.PNG"))
        assertTrue(ShareImageFormatFilter.isAccepted(null, "shot.jpg"))
        assertTrue(ShareImageFormatFilter.isAccepted(null, "shot.jpeg"))
        assertTrue(ShareImageFormatFilter.isAccepted(null, "shot.heic"))
        assertTrue(ShareImageFormatFilter.isAccepted(null, "shot.heif"))
        assertTrue(ShareImageFormatFilter.isAccepted("", "shot.jpeg"))
        assertTrue(ShareImageFormatFilter.isAccepted("image/*", "shot.jpeg"))
    }

    @Test
    fun `rejects unsupported mime and extension`() {
        assertFalse(ShareImageFormatFilter.isAccepted("image/webp", "shot.webp"))
        assertFalse(ShareImageFormatFilter.isAccepted("image/gif", "shot.gif"))
        assertFalse(ShareImageFormatFilter.isAccepted("application/pdf", "doc.pdf"))
        assertFalse(ShareImageFormatFilter.isAccepted("application/pdf", "fake.jpg"))
        assertFalse(ShareImageFormatFilter.isAccepted("image/webp", "fake.jpg"))
        assertFalse(ShareImageFormatFilter.isAccepted(null, "video.mp4"))
        assertFalse(ShareImageFormatFilter.isAccepted(null, "no-extension"))
        assertFalse(ShareImageFormatFilter.isAccepted(null, null))
    }
}
