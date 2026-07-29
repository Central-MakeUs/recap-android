package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.PreparedScreenshot
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SharedAnalysisRequestStoreTest {
    @Test
    fun `registered request can be consumed once`() {
        val store = SharedAnalysisRequestStore()
        val prepared = listOf(
            ScreenshotUploadCandidate(
                localImage = LocalImage(
                    uri = "content://share/image.jpg",
                    displayName = "image.jpg",
                    dateAddedMillis = 1L,
                ),
                preparedScreenshot = PreparedScreenshot(
                    localImage = LocalImage(
                        uri = "content://share/image.jpg",
                        displayName = "image.jpg",
                        dateAddedMillis = 1L,
                    ),
                    jpegBytes = byteArrayOf(1, 2, 3),
                ),
                completedPreparationAttempts = 1,
            ),
        )

        store.register(requestId = "req-1", candidates = prepared)

        assertEquals(prepared, store.consume("req-1"))
        assertNull(store.consume("req-1"))
    }

    @Test
    fun `unknown request id returns null`() {
        val store = SharedAnalysisRequestStore()

        assertNull(store.consume("missing"))
    }
}
