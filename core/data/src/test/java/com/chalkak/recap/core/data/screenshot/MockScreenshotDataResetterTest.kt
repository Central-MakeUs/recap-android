package com.chalkak.recap.core.data.screenshot

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MockScreenshotDataResetterTest {
    @Test
    fun `reset deletes cards then clears stored images`() = runTest {
        val cardRepository = mockk<ScreenshotCardRepository>()
        val imageStorage = mockk<ScreenshotImageStorage>()
        coEvery { cardRepository.deleteAllCards() } returns Unit
        every { imageStorage.clearStoredImages() } just Runs
        val resetter = MockScreenshotDataResetter(cardRepository, imageStorage)

        resetter.reset()

        coVerify(exactly = 1) { cardRepository.deleteAllCards() }
        verify(exactly = 1) { imageStorage.clearStoredImages() }
    }

    @Test
    fun `reset propagates card deletion failure`() = runTest {
        val cardRepository = mockk<ScreenshotCardRepository>()
        val imageStorage = mockk<ScreenshotImageStorage>(relaxed = true)
        coEvery { cardRepository.deleteAllCards() } throws IllegalStateException("db fail")
        val resetter = MockScreenshotDataResetter(cardRepository, imageStorage)

        assertThrows<IllegalStateException> {
            resetter.reset()
        }
        verify(exactly = 0) { imageStorage.clearStoredImages() }
    }
}
