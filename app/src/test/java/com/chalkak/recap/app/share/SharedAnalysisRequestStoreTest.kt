package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SharedAnalysisRequestStoreTest {
    @Test
    fun `registered request can be consumed once`() {
        val store = SharedAnalysisRequestStore()
        val images = listOf(
            LocalImage(
                uri = "content://share/image.jpg",
                displayName = "image.jpg",
                dateAddedMillis = 1L,
            ),
        )

        store.register(requestId = "req-1", images = images)

        assertEquals(images, store.consume("req-1"))
        assertNull(store.consume("req-1"))
    }

    @Test
    fun `unknown request id returns null`() {
        val store = SharedAnalysisRequestStore()

        assertNull(store.consume("missing"))
    }
}
