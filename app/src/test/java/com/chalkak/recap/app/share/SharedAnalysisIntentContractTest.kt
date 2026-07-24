package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SharedAnalysisIntentContractTest {
    @Test
    fun `payload round trip preserves image order`() {
        val images = listOf(
            LocalImage(uri = "content://share/a.jpg", displayName = "a.jpg", dateAddedMillis = 1L),
            LocalImage(uri = "content://share/b.jpg", displayName = "b.jpg", dateAddedMillis = 2L),
            LocalImage(uri = "content://share/c.jpg", displayName = "c.jpg", dateAddedMillis = 3L),
        )

        val payload = encodeSharedAnalysisPayload(requestId = "req-1", images = images)
        val decoded = decodeSharedAnalysisPayload(
            requestId = payload.requestId,
            uris = payload.uris,
            displayNames = payload.displayNames,
            dateAddedMillis = payload.dateAddedMillis,
        )

        assertEquals("req-1", decoded?.requestId)
        assertEquals(images, decoded?.images)
    }

    @Test
    fun `empty payload is rejected`() {
        val decoded = decodeSharedAnalysisPayload(
            requestId = "req-1",
            uris = emptyList(),
            displayNames = emptyList(),
            dateAddedMillis = longArrayOf(),
        )

        assertNull(decoded)
    }

    @Test
    fun `mismatched array lengths are rejected`() {
        val decoded = decodeSharedAnalysisPayload(
            requestId = "req-1",
            uris = listOf("content://share/a.jpg"),
            displayNames = listOf("a.jpg", "b.jpg"),
            dateAddedMillis = longArrayOf(1L),
        )

        assertNull(decoded)
    }

    @Test
    fun `blank request id is rejected`() {
        val decoded = decodeSharedAnalysisPayload(
            requestId = " ",
            uris = listOf("content://share/a.jpg"),
            displayNames = listOf("a.jpg"),
            dateAddedMillis = longArrayOf(1L),
        )

        assertNull(decoded)
    }

    @Test
    fun `blank uri is rejected`() {
        val decoded = decodeSharedAnalysisPayload(
            requestId = "req-1",
            uris = listOf(" "),
            displayNames = listOf("a.jpg"),
            dateAddedMillis = longArrayOf(1L),
        )

        assertNull(decoded)
    }

    @Test
    fun `uri without scheme is rejected`() {
        val decoded = decodeSharedAnalysisPayload(
            requestId = "req-1",
            uris = listOf("not-a-uri"),
            displayNames = listOf("a.jpg"),
            dateAddedMillis = longArrayOf(1L),
        )

        assertNull(decoded)
    }
}
