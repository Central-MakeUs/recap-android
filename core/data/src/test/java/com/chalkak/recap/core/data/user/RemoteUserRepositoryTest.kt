package com.chalkak.recap.core.data.user

import app.cash.turbine.test
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.user.remote.AccountInfoResponseDto
import com.chalkak.recap.core.data.user.remote.ConsentStatusResponseDto
import com.chalkak.recap.core.data.user.remote.DataSummaryResponseDto
import com.chalkak.recap.core.data.user.remote.UserApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
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
class RemoteUserRepositoryTest {
    private val userApi = mockk<UserApi>()
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
    private val screenshotCardRepository = mockk<ScreenshotCardRepository>()
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private val changeNotifier = RemoteCaptureChangeNotifier()
    private val refreshToken = MutableStateFlow<String?>("refresh-token")
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: RemoteUserRepository

    @BeforeEach
    fun setUp() {
        refreshToken.value = "refresh-token"
        every { sessionTokenStore.refreshToken } returns refreshToken
        coEvery { screenshotCardRepository.deleteAllCards() } just runs
        every { thumbnailCache.clearAll() } just runs
        repository = RemoteUserRepository(
            userApi = userApi,
            sessionTokenStore = sessionTokenStore,
            screenshotCardRepository = screenshotCardRepository,
            thumbnailCache = thumbnailCache,
            changeNotifier = changeNotifier,
            repositoryScope = CoroutineScope(SupervisorJob() + testDispatcher),
        )
    }

    @Test
    fun `getAccountInfo maps success response`() = runTest {
        coEvery { userApi.getAccountInfo() } returns ApiResponseDto(
            success = true,
            data = AccountInfoResponseDto(
                platform = "KAKAO",
                createdAt = "2026-07-01T00:00:00Z",
            ),
        )

        val result = repository.getAccountInfo()

        assertEquals("KAKAO", result.getOrNull()?.platform)
        assertEquals("2026-07-01T00:00:00Z", result.getOrNull()?.createdAt)
    }

    @Test
    fun `getDataSummary maps captured count`() = runTest {
        coEvery { userApi.getDataSummary() } returns ApiResponseDto(
            success = true,
            data = DataSummaryResponseDto(capturedCount = 12L),
        )

        val result = repository.getDataSummary()

        assertEquals(12L, result.getOrNull()?.capturedCount)
    }

    @Test
    fun `prefetchDataSummary shares result with observeDataSummary without second api call`() =
        runTest(testDispatcher) {
            coEvery { userApi.getDataSummary() } returns ApiResponseDto(
                success = true,
                data = DataSummaryResponseDto(capturedCount = 12L),
            )

            val prefetched = repository.prefetchDataSummary()
            assertTrue(prefetched.isSuccess)

            repository.observeDataSummary().test {
                assertSame(prefetched.getOrNull(), awaitItem().getOrNull())
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) { userApi.getDataSummary() }
        }

    @Test
    fun `refreshDataSummary triggers another api call for active observers`() =
        runTest(testDispatcher) {
            coEvery { userApi.getDataSummary() } returns ApiResponseDto(
                success = true,
                data = DataSummaryResponseDto(capturedCount = 12L),
            )

            repository.observeDataSummary().test {
                assertTrue(awaitItem().isSuccess)
                repository.refreshDataSummary()
                assertTrue(awaitItem().isSuccess)
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 2) { userApi.getDataSummary() }
        }

    @Test
    fun `prefetchDataSummary does not replay summary from previous session`() =
        runTest(testDispatcher) {
            coEvery { userApi.getDataSummary() } returnsMany listOf(
                ApiResponseDto(
                    success = true,
                    data = DataSummaryResponseDto(capturedCount = 1L),
                ),
                ApiResponseDto(
                    success = true,
                    data = DataSummaryResponseDto(capturedCount = 2L),
                ),
            )

            val first = repository.prefetchDataSummary().getOrThrow()
            refreshToken.value = "another-refresh-token"
            val second = repository.prefetchDataSummary().getOrThrow()

            assertEquals(1L, first.capturedCount)
            assertEquals(2L, second.capturedCount)
            coVerify(exactly = 2) { userApi.getDataSummary() }
        }

    @Test
    fun `prefetchDataSummary fails without calling api when session is unavailable`() =
        runTest(testDispatcher) {
            refreshToken.value = null

            val result = repository.prefetchDataSummary()

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { userApi.getDataSummary() }
        }

    @Test
    fun `withdraw clears session even when api fails`() = runTest {
        coEvery { userApi.withdraw() } throws RemoteApiException(code = "ERR", message = "fail")

        val result = repository.withdraw()

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `withdraw clears session on success`() = runTest {
        coEvery { userApi.withdraw() } returns Unit

        val result = repository.withdraw()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { sessionTokenStore.clear() }
    }

    @Test
    fun `deleteAccountData clears local cache and does not clear session`() = runTest {
        coEvery { userApi.deleteAccountData() } returns Unit

        val result = repository.deleteAccountData()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionTokenStore.clear() }
        coVerify(exactly = 1) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 1) { thumbnailCache.clearAll() }
    }

    @Test
    fun `deleteAccountData skips local cleanup when api fails`() = runTest {
        coEvery { userApi.deleteAccountData() } throws RemoteApiException(code = "ERR", message = "fail")

        val result = repository.deleteAccountData()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { screenshotCardRepository.deleteAllCards() }
        verify(exactly = 0) { thumbnailCache.clearAll() }
    }

    @Test
    fun `getConsentStatus maps consented fields`() = runTest {
        coEvery { userApi.getConsentStatus() } returns ApiResponseDto(
            success = true,
            data = ConsentStatusResponseDto(
                consented = true,
                consentedAt = "2026-07-27T00:00:00Z",
            ),
        )

        val result = repository.getConsentStatus()

        assertEquals(true, result.getOrNull()?.consented)
        assertEquals("2026-07-27T00:00:00Z", result.getOrNull()?.consentedAt)
    }

    @Test
    fun `prefetchConsentStatus shares result with observeConsentStatus without second api call`() =
        runTest(testDispatcher) {
            coEvery { userApi.getConsentStatus() } returns ApiResponseDto(
                success = true,
                data = ConsentStatusResponseDto(
                    consented = true,
                    consentedAt = "2026-07-27T00:00:00Z",
                ),
            )

            val prefetched = repository.prefetchConsentStatus()
            assertTrue(prefetched.isSuccess)

            repository.observeConsentStatus().test {
                assertSame(prefetched.getOrNull(), awaitItem().getOrNull())
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 1) { userApi.getConsentStatus() }
        }

    @Test
    fun `refreshConsentStatus triggers another api call for active observers`() =
        runTest(testDispatcher) {
            coEvery { userApi.getConsentStatus() } returns ApiResponseDto(
                success = true,
                data = ConsentStatusResponseDto(consented = false),
            )

            repository.observeConsentStatus().test {
                assertTrue(awaitItem().isSuccess)
                repository.refreshConsentStatus()
                assertTrue(awaitItem().isSuccess)
                cancelAndIgnoreRemainingEvents()
            }

            coVerify(exactly = 2) { userApi.getConsentStatus() }
        }

    @Test
    fun `prefetchConsentStatus fails without calling api when session is unavailable`() =
        runTest(testDispatcher) {
            refreshToken.value = null

            val result = repository.prefetchConsentStatus()

            assertTrue(result.isFailure)
            coVerify(exactly = 0) { userApi.getConsentStatus() }
        }

    @Test
    fun `giveConsent returns success`() = runTest {
        coEvery { userApi.giveConsent() } returns Unit
        coEvery { userApi.getConsentStatus() } returns ApiResponseDto(
            success = true,
            data = ConsentStatusResponseDto(consented = true),
        )

        val result = repository.giveConsent()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userApi.giveConsent() }
    }

    @Test
    fun `withdrawConsent returns success`() = runTest {
        coEvery { userApi.withdrawConsent() } returns Unit
        coEvery { userApi.getConsentStatus() } returns ApiResponseDto(
            success = true,
            data = ConsentStatusResponseDto(consented = false),
        )

        val result = repository.withdrawConsent()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { userApi.withdrawConsent() }
    }
}
