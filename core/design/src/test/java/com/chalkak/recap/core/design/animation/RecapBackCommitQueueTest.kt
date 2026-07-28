package com.chalkak.recap.core.design.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecapBackCommitQueueTest {

    @Test
    fun `single back starts one commit without replay`() {
        val queue = RecapBackCommitQueue()

        assertEquals(BackCompletionDecision.StartCommit, queue.onBackCompleted())

        assertEquals(0, queue.finishCommit())
    }

    @Test
    fun `back during commit is queued for replay`() {
        val queue = RecapBackCommitQueue()
        queue.onBackCompleted()

        assertEquals(BackCompletionDecision.Queued, queue.onBackCompleted())

        assertEquals(1, queue.finishCommit())
    }

    @Test
    fun `multiple backs preserve their replay count`() {
        val queue = RecapBackCommitQueue()
        queue.onBackCompleted()
        repeat(3) {
            queue.onBackCompleted()
        }

        assertEquals(3, queue.finishCommit())
    }

    @Test
    fun `cancel is ignored while a commit is running`() {
        val queue = RecapBackCommitQueue()

        assertTrue(queue.shouldHandleCancellation())

        queue.onBackCompleted()

        assertFalse(queue.shouldHandleCancellation())
        assertEquals(0, queue.finishCommit())
        assertTrue(queue.shouldHandleCancellation())
    }
}
