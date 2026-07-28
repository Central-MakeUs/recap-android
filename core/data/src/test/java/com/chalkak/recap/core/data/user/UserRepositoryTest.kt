package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.user.remote.AccountInfoResponseDto
import com.chalkak.recap.core.data.user.remote.DataSummaryResponseDto
import com.chalkak.recap.core.data.user.remote.UserApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserRepositoryTest {
    private val userApi = mockk<UserApi>()
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)

    private lateinit var repository: UserRepository

    @BeforeEach
    fun setUp() {
        repository = UserRepository(
            userApi = userApi,
            sessionTokenStore = sessionTokenStore,
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
    fun `deleteAccountData does not clear session`() = runTest {
        coEvery { userApi.deleteAccountData() } returns Unit

        val result = repository.deleteAccountData()

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { sessionTokenStore.clear() }
    }
}
