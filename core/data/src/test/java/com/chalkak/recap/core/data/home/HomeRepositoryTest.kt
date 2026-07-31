package com.chalkak.recap.core.data.home

import app.cash.turbine.test
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.home.remote.HomeSummaryResponseDto
import com.chalkak.recap.core.data.home.remote.TopTypeResponseDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepositoryTest {
    private val homeApi = mockk<HomeApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private val changeNotifier = RemoteCaptureChangeNotifier()
    private val sessionTokenStore = mockk<SessionTokenStore>()
    private val refreshToken = MutableStateFlow<String?>("refresh-token")
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: RemoteHomeRepository

    @BeforeEach
    fun setUp() {
        refreshToken.value = "refresh-token"
        every { sessionTokenStore.refreshToken } returns refreshToken
        coEvery { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }
        repository = RemoteHomeRepository(
            homeApi = homeApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
            sessionTokenStore = sessionTokenStore,
            repositoryScope = CoroutineScope(SupervisorJob() + testDispatcher),
        )
    }

    @Test
    fun `getSummary maps success response`() = runTest {
        coEvery { homeApi.getSummary() } returns ApiResponseDto(
            success = true,
            data = HomeSummaryResponseDto(
                recentCaptures = listOf(
                    CaptureSummaryResponseDto(
                        captureId = 1L,
                        title = "recent",
                        summary = "s",
                        typeCode = CardTypeDto.JOB,
                        thumbnailUrl = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
                favorites = emptyList(),
                topTypes = listOf(
                    TopTypeResponseDto(
                        typeCode = CardTypeDto.JOB,
                        count = 4L,
                        representativeThumbnailUrl = "https://thumb",
                    ),
                ),
                hasAnyCapture = true,
            ),
        )

        val result = repository.getSummary()

        assertEquals(true, result.getOrNull()?.hasAnyCapture)
        assertEquals(1, result.getOrNull()?.recentCaptures?.size)
        assertEquals(ScreenshotContentType.JOB, result.getOrNull()?.topTypes?.single()?.typeCode)
    }

    @Test
    fun `getSummary maps server error`() = runTest {
        coEvery { homeApi.getSummary() } returns ApiResponseDto(
            success = false,
            data = null,
            error = ApiErrorDto(code = "UNAUTHORIZED", message = "login required"),
        )

        val result = repository.getSummary()

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("UNAUTHORIZED", error.code)
    }

    @Test
    fun `getSummary maps network failure`() = runTest {
        coEvery { homeApi.getSummary() } throws IOException("offline")

        val result = repository.getSummary()

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }

    @Test
    fun `prefetchSummary shares result with observeSummary without second api call`() = runTest(testDispatcher) {
        coEvery { homeApi.getSummary() } returns successSummaryResponse()

        val prefetched = repository.prefetchSummary()
        assertTrue(prefetched.isSuccess)

        repository.observeSummary().test {
            assertSame(prefetched.getOrNull(), awaitItem().getOrNull())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { homeApi.getSummary() }
    }

    @Test
    fun `refreshSummary triggers another api call for active observers`() = runTest(testDispatcher) {
        coEvery { homeApi.getSummary() } returns successSummaryResponse()

        repository.observeSummary().test {
            assertTrue(awaitItem().isSuccess)
            repository.refreshSummary()
            assertTrue(awaitItem().isSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { homeApi.getSummary() }
    }

    @Test
    fun `prefetchSummary does not replay summary from previous session`() = runTest(testDispatcher) {
        coEvery { homeApi.getSummary() } returnsMany listOf(
            successSummaryResponse(title = "first session"),
            successSummaryResponse(title = "second session"),
        )

        val first = repository.prefetchSummary().getOrThrow()
        refreshToken.value = "another-refresh-token"
        val second = repository.prefetchSummary().getOrThrow()

        assertEquals("first session", first.recentCaptures.single().title)
        assertEquals("second session", second.recentCaptures.single().title)
        coVerify(exactly = 2) { homeApi.getSummary() }
    }

    @Test
    fun `prefetchSummary fails without calling api when session is unavailable`() = runTest(testDispatcher) {
        refreshToken.value = null

        val result = repository.prefetchSummary()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { homeApi.getSummary() }
    }

    private fun successSummaryResponse(title: String = "recent") = ApiResponseDto(
        success = true,
        data = HomeSummaryResponseDto(
            recentCaptures = listOf(
                CaptureSummaryResponseDto(
                    captureId = 1L,
                    title = title,
                    summary = "s",
                    typeCode = CardTypeDto.JOB,
                    thumbnailUrl = null,
                    isFavorite = false,
                    organizedAt = "2026-07-19T00:00:00Z",
                ),
            ),
            favorites = emptyList(),
            topTypes = listOf(
                TopTypeResponseDto(
                    typeCode = CardTypeDto.JOB,
                    count = 4L,
                    representativeThumbnailUrl = "https://thumb",
                ),
            ),
            hasAnyCapture = true,
        ),
    )
}
