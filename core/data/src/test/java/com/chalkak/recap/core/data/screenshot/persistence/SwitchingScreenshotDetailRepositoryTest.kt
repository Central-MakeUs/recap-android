package com.chalkak.recap.core.data.screenshot.persistence

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class SwitchingScreenshotDetailRepositoryTest {
    @Test
    fun `observeCard delegates to mock in mock mode`() = runTest {
        val mockCard = storedCard(1L)
        val remoteCard = storedCard(2L)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.MOCK)
        val mock = mockk<MockScreenshotDetailRepository>()
        every { mock.observeCard(1L) } returns flowOf(mockCard)
        val remote = mockk<RemoteScreenshotDetailRepository>()
        every { remote.observeCard(1L) } returns flowOf(remoteCard)

        val repository = SwitchingScreenshotDetailRepository(
            screenshotBackendModeStore = modeStore,
            mockScreenshotDetailRepository = mock,
            remoteScreenshotDetailRepository = remote,
        )

        assertSame(mockCard, repository.observeCard(1L).first())
    }

    @Test
    fun `observeCard delegates to remote in remote mode`() = runTest {
        val mockCard = storedCard(1L)
        val remoteCard = storedCard(2L)
        val modeStore = mockk<ScreenshotBackendModeStore>()
        every { modeStore.mode } returns MutableStateFlow(ScreenshotBackendMode.REMOTE)
        val mock = mockk<MockScreenshotDetailRepository>()
        every { mock.observeCard(1L) } returns flowOf(mockCard)
        val remote = mockk<RemoteScreenshotDetailRepository>()
        every { remote.observeCard(1L) } returns flowOf(remoteCard)

        val repository = SwitchingScreenshotDetailRepository(
            screenshotBackendModeStore = modeStore,
            mockScreenshotDetailRepository = mock,
            remoteScreenshotDetailRepository = remote,
        )

        assertSame(remoteCard, repository.observeCard(1L).first())
    }

    private fun storedCard(captureId: Long): StoredScreenshotCard {
        return StoredScreenshotCard(
            analysisResult = ScreenshotAnalysisResult(
                captureId = captureId,
                typeCode = ScreenshotContentType.ETC,
                title = "title-$captureId",
                summary = "summary-$captureId",
                body = "body-$captureId",
                originalImageUrl = "mock://captures/$captureId",
                isFavorite = false,
                organizedAt = Instant.ofEpochMilli(1_000L),
            ),
            imageRefs = ScreenshotCardImageRefs(),
            updatedAtMillis = 2_000L,
        )
    }
}
