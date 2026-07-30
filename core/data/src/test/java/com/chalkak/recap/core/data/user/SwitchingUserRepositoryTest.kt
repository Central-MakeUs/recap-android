package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SwitchingUserRepositoryTest {
    @Test
    fun `getDataSummary delegates to mock in mock mode`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        val mock = mockk<MockUserRepository>()
        coEvery { mock.getDataSummary() } returns Result.success(DataSummary(4))
        val remote = mockk<RemoteUserRepository>()

        val repository = SwitchingUserRepository(
            screenshotBackendModeStore = modeStore,
            mockUserRepository = mock,
            remoteUserRepository = remote,
        )

        repository.getDataSummary()

        coVerify(exactly = 1) { mock.getDataSummary() }
        coVerify(exactly = 0) { remote.getDataSummary() }
    }

    @Test
    fun `consent and delete delegate to remote in remote mode`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.REMOTE
        val mock = mockk<MockUserRepository>()
        val remote = mockk<RemoteUserRepository>()
        coEvery { remote.getConsentStatus() } returns Result.success(ConsentStatus(true))
        coEvery { remote.giveConsent() } returns Result.success(Unit)
        coEvery { remote.withdrawConsent() } returns Result.success(Unit)
        coEvery { remote.deleteAccountData() } returns Result.success(Unit)

        val repository = SwitchingUserRepository(
            screenshotBackendModeStore = modeStore,
            mockUserRepository = mock,
            remoteUserRepository = remote,
        )

        repository.getConsentStatus()
        repository.giveConsent()
        repository.withdrawConsent()
        repository.deleteAccountData()

        coVerify(exactly = 1) { remote.getConsentStatus() }
        coVerify(exactly = 1) { remote.giveConsent() }
        coVerify(exactly = 1) { remote.withdrawConsent() }
        coVerify(exactly = 1) { remote.deleteAccountData() }
        coVerify(exactly = 0) { mock.getConsentStatus() }
        coVerify(exactly = 0) { mock.giveConsent() }
        coVerify(exactly = 0) { mock.withdrawConsent() }
        coVerify(exactly = 0) { mock.deleteAccountData() }
    }

    @Test
    fun `auth always uses remote even in mock mode`() = runTest {
        val modeStore = mockk<ScreenshotBackendModeStore>()
        coEvery { modeStore.currentMode() } returns ScreenshotBackendMode.MOCK
        val mock = mockk<MockUserRepository>()
        val remote = mockk<RemoteUserRepository>()
        coEvery { remote.getAccountInfo() } returns Result.success(
            AccountInfo(platform = "KAKAO", createdAt = "2026-07-01T00:00:00Z"),
        )
        coEvery { remote.withdraw() } returns Result.success(Unit)

        val repository = SwitchingUserRepository(
            screenshotBackendModeStore = modeStore,
            mockUserRepository = mock,
            remoteUserRepository = remote,
        )

        repository.getAccountInfo()
        repository.withdraw()

        coVerify(exactly = 1) { remote.getAccountInfo() }
        coVerify(exactly = 1) { remote.withdraw() }
        coVerify(exactly = 0) { mock.getAccountInfo() }
        coVerify(exactly = 0) { mock.withdraw() }
    }
}
