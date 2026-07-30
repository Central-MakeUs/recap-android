package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchingUserRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockUserRepository: MockUserRepository,
    private val remoteUserRepository: RemoteUserRepository,
) : UserRepository {
    override suspend fun getAccountInfo(): Result<AccountInfo> =
        remoteUserRepository.getAccountInfo()

    override suspend fun withdraw(): Result<Unit> =
        remoteUserRepository.withdraw()

    override suspend fun getDataSummary(): Result<DataSummary> =
        resolveDataDelegate().getDataSummary()

    override suspend fun getConsentStatus(): Result<ConsentStatus> =
        resolveDataDelegate().getConsentStatus()

    override suspend fun giveConsent(): Result<Unit> =
        resolveDataDelegate().giveConsent()

    override suspend fun withdrawConsent(): Result<Unit> =
        resolveDataDelegate().withdrawConsent()

    override suspend fun deleteAccountData(): Result<Unit> =
        resolveDataDelegate().deleteAccountData()

    private suspend fun resolveDataDelegate(): UserRepository {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockUserRepository
            ScreenshotBackendMode.REMOTE -> remoteUserRepository
        }
    }
}
