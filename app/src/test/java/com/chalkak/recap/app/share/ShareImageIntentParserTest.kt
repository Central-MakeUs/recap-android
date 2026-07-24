package com.chalkak.recap.app.share

import android.content.ContentResolver
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ShareImageIntentParserTest {
    @Test
    fun `resolver failure rejects uri instead of throwing`() {
        val contentResolver = mockk<ContentResolver>()
        val uri = mockk<Uri>()
        every { contentResolver.query(uri, any(), null, null, null) } throws
            SecurityException("denied")
        every { contentResolver.getType(uri) } throws SecurityException("denied")
        every { uri.lastPathSegment } returns "image.jpg"
        val parser = ShareImageIntentParser(contentResolver)

        val result = parser.parseUri(uri = uri, dateAddedMillis = 1L)

        assertNull(result)
    }
}
