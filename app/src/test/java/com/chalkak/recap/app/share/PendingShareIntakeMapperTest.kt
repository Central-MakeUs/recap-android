package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.feature.organize.MAX_SELECTION_COUNT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PendingShareIntakeMapperTest {
    @Test
    fun `empty accepted becomes Unsupported`() {
        val pending = ShareImageParseResult(accepted = emptyList(), rejectedCount = 3)
            .toPendingShareIntake(sessionId = TEST_SESSION_ID)

        assertEquals(PendingShareIntake.Unsupported(TEST_SESSION_ID), pending)
    }

    @Test
    fun `accepted within max keeps all images`() {
        val images = sampleImages(3)
        val pending = ShareImageParseResult(accepted = images, rejectedCount = 1)
            .toPendingShareIntake(sessionId = TEST_SESSION_ID)

        val confirmation = pending as PendingShareIntake.Confirmation
        assertEquals(images, confirmation.images)
        assertEquals(1, confirmation.rejectedCount)
        assertFalse(confirmation.trimmedByMax)
    }

    @Test
    fun `saved state round trip preserves Unsupported`() {
        val unsupported = PendingShareIntake.Unsupported(TEST_SESSION_ID)
        val encoded = unsupported.encodeForSavedState()

        assertEquals(
            unsupported,
            decodePendingShareIntakeFromSavedState(encoded),
        )
    }

    @Test
    fun `saved state round trip preserves Confirmation`() {
        val confirmation = PendingShareIntake.Confirmation(
            sessionId = TEST_SESSION_ID,
            images = sampleImages(2),
            rejectedCount = 4,
            trimmedByMax = true,
        )

        val restored = decodePendingShareIntakeFromSavedState(confirmation.encodeForSavedState())

        assertEquals(confirmation, restored)
    }

    @Test
    fun `accepted over max trims to first twenty`() {
        val images = sampleImages(MAX_SELECTION_COUNT + 5)
        val pending = ShareImageParseResult(accepted = images, rejectedCount = 0)
            .toPendingShareIntake(sessionId = TEST_SESSION_ID)

        val confirmation = pending as PendingShareIntake.Confirmation
        assertEquals(images.take(MAX_SELECTION_COUNT), confirmation.images)
        assertEquals(0, confirmation.rejectedCount)
        assertTrue(confirmation.trimmedByMax)
    }

    private fun sampleImages(count: Int): List<LocalImage> {
        return List(count) { index ->
            LocalImage(
                uri = "content://share/$index",
                displayName = "image-$index.jpg",
                dateAddedMillis = index.toLong(),
            )
        }
    }
}

private const val TEST_SESSION_ID = "share-session"
