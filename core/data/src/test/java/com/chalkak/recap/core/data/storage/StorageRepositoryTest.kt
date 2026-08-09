package com.chalkak.recap.core.data.storage

import app.cash.turbine.test
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.CaptureListResponseDto
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.storage.remote.StorageApi
import com.chalkak.recap.core.data.storage.remote.StorageTypeResponseDto
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StorageRepositoryTest {
    private val storageApi = mockk<StorageApi>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private val changeNotifier = RemoteCaptureChangeNotifier()
    private val sessionTokenStore = mockk<SessionTokenStore>()
    private val refreshToken = MutableStateFlow<String?>("refresh-token")
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: RemoteStorageRepository

    @BeforeEach
    fun setUp() {
        refreshToken.value = "refresh-token"
        every { sessionTokenStore.refreshToken } returns refreshToken
        every { thumbnailCache.resolveThumbnailSources(any()) } answers {
            firstArg<List<Pair<Long, String?>>>().associate { (id, url) -> id to url }
        }
        repository = RemoteStorageRepository(
            storageApi = storageApi,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
            sessionTokenStore = sessionTokenStore,
            repositoryScope = CoroutineScope(SupervisorJob() + testDispatcher),
        )
    }

    @Test
    fun `getStorageTypes maps success response`() = runTest {
        coEvery { storageApi.getTypes() } returns ApiResponseDto(
            success = true,
            data = listOf(
                StorageTypeResponseDto(
                    typeCode = CardTypeDto.KNOWLEDGE,
                    count = 2L,
                    representativeTitles = listOf("a", "b"),
                ),
            ),
        )

        val result = repository.getStorageTypes()

        assertEquals(ScreenshotContentType.KNOWLEDGE, result.getOrNull()?.single()?.typeCode)
        assertEquals(2L, result.getOrNull()?.single()?.count)
    }

    @Test
    fun `observeOverview includes zero count categories in display order`() = runTest {
        stubOverviewApis(
            types = listOf(
                StorageTypeResponseDto(
                    typeCode = CardTypeDto.SHOPPING,
                    count = 1L,
                    representativeTitles = listOf("택배"),
                ),
            ),
        )

        val overview = repository.observeOverview("").first().getOrThrow()

        assertEquals(StorageOverviewCategoryOrder, overview.types.map { it.typeCode })
        assertEquals(1L, overview.types.single { it.typeCode == ScreenshotContentType.SHOPPING }.count)
        assertTrue(
            overview.types
                .filter { it.typeCode != ScreenshotContentType.SHOPPING }
                .all { it.count == 0L },
        )
    }

    @Test
    fun `prefetchOverview shares result with observeOverview without second api call`() = runTest {
        stubOverviewApis(
            types = listOf(
                StorageTypeResponseDto(
                    typeCode = CardTypeDto.SHOPPING,
                    count = 1L,
                    representativeTitles = listOf("택배"),
                ),
            ),
        )

        val prefetched = repository.prefetchOverview()
        assertTrue(prefetched.isSuccess)

        repository.observeOverview("").test {
            assertSame(prefetched.getOrNull(), awaitItem().getOrNull())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { storageApi.getTypes() }
        coVerify(exactly = 1) { storageApi.getFavorites() }
    }

    @Test
    fun `refreshOverview triggers another api call for active observers`() = runTest(testDispatcher) {
        stubOverviewApis(
            types = listOf(
                StorageTypeResponseDto(
                    typeCode = CardTypeDto.SHOPPING,
                    count = 1L,
                    representativeTitles = listOf("택배"),
                ),
            ),
        )

        repository.observeOverview("").test {
            assertTrue(awaitItem().isSuccess)
            repository.refreshOverview()
            assertTrue(awaitItem().isSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { storageApi.getTypes() }
        coVerify(exactly = 2) { storageApi.getFavorites() }
    }

    @Test
    fun `prefetchOverview does not replay overview from previous session`() =
        runTest(testDispatcher) {
            coEvery { storageApi.getTypes() } returnsMany listOf(
                ApiResponseDto(
                    success = true,
                    data = listOf(
                        StorageTypeResponseDto(
                            typeCode = CardTypeDto.SHOPPING,
                            count = 1L,
                            representativeTitles = listOf("first session"),
                        ),
                    ),
                ),
                ApiResponseDto(
                    success = true,
                    data = listOf(
                        StorageTypeResponseDto(
                            typeCode = CardTypeDto.SHOPPING,
                            count = 1L,
                            representativeTitles = listOf("second session"),
                        ),
                    ),
                ),
            )
            coEvery { storageApi.getFavorites() } returns ApiResponseDto(
                success = true,
                data = CaptureListResponseDto(count = 0, items = emptyList()),
            )

            val first = repository.prefetchOverview().getOrThrow()
            refreshToken.value = "another-refresh-token"
            val second = repository.prefetchOverview().getOrThrow()

            assertEquals(
                listOf("first session"),
                first.types.single { it.typeCode == ScreenshotContentType.SHOPPING }
                    .representativeTitles,
            )
            assertEquals(
                listOf("second session"),
                second.types.single { it.typeCode == ScreenshotContentType.SHOPPING }
                    .representativeTitles,
            )
            coVerify(exactly = 2) { storageApi.getTypes() }
            coVerify(exactly = 2) { storageApi.getFavorites() }
        }

    @Test
    fun `prefetchOverview fails without calling api when session is unavailable`() =
        runTest(testDispatcher) {
            refreshToken.value = null

            val result = repository.prefetchOverview()

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { storageApi.getTypes() }
            coVerify(exactly = 0) { storageApi.getFavorites() }
        }

    @Test
    fun `getCapturesByType sends type code and sort query`() = runTest {
        coEvery {
            storageApi.getTypeCaptures(typeCode = "KNOWLEDGE", sort = "oldest")
        } returns ApiResponseDto(
            success = true,
            data = CaptureListResponseDto(
                count = 1,
                items = listOf(
                    CaptureSummaryResponseDto(
                        captureId = 7L,
                        title = "t",
                        summary = "s",
                        typeCode = CardTypeDto.KNOWLEDGE,
                        thumbnailUrl = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )

        val result = repository.getCapturesByType(
            typeCode = ScreenshotContentType.KNOWLEDGE,
            sort = CaptureSort.Oldest,
        )

        assertEquals(1, result.getOrNull()?.count)
        assertEquals(7L, result.getOrNull()?.items?.single()?.captureId)
        coVerify(exactly = 1) {
            storageApi.getTypeCaptures(typeCode = "KNOWLEDGE", sort = "oldest")
        }
    }

    @Test
    fun `getFavoriteCaptures maps server error`() = runTest {
        coEvery { storageApi.getFavorites() } returns ApiResponseDto(
            success = false,
            data = null,
            error = ApiErrorDto(code = "INVALID_INPUT", message = "bad"),
        )

        val result = repository.getFavoriteCaptures()

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("INVALID_INPUT", error.code)
    }

    @Test
    fun `getEtcCaptures maps network failure`() = runTest {
        coEvery { storageApi.getEtc(any()) } throws IOException("offline")

        val result = repository.getEtcCaptures()

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }

    private fun stubOverviewApis(types: List<StorageTypeResponseDto>) {
        coEvery { storageApi.getTypes() } returns ApiResponseDto(
            success = true,
            data = types,
        )
        coEvery { storageApi.getFavorites() } returns ApiResponseDto(
            success = true,
            data = CaptureListResponseDto(count = 0, items = emptyList()),
        )
    }
}
