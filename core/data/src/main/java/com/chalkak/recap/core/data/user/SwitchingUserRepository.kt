package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDataSummary(): Flow<Result<DataSummary>> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            when (mode) {
                ScreenshotBackendMode.MOCK -> mockUserRepository.observeDataSummary()
                ScreenshotBackendMode.REMOTE -> remoteUserRepository.observeDataSummary()
            }
        }
    }

    override suspend fun prefetchDataSummary(): Result<DataSummary> {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockUserRepository.prefetchDataSummary()
            ScreenshotBackendMode.REMOTE -> remoteUserRepository.prefetchDataSummary()
        }
    }

    override fun refreshDataSummary() {
        remoteUserRepository.refreshDataSummary()
        mockUserRepository.refreshDataSummary()
    }

    override suspend fun getDataSummary(): Result<DataSummary> =
        prefetchDataSummary()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeConsentStatus(): Flow<Result<ConsentStatus>> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            when (mode) {
                ScreenshotBackendMode.MOCK -> mockUserRepository.observeConsentStatus()
                ScreenshotBackendMode.REMOTE -> remoteUserRepository.observeConsentStatus()
            }
        }
    }

    override suspend fun prefetchConsentStatus(): Result<ConsentStatus> {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockUserRepository.prefetchConsentStatus()
            ScreenshotBackendMode.REMOTE -> remoteUserRepository.prefetchConsentStatus()
        }
    }

    override fun refreshConsentStatus() {
        remoteUserRepository.refreshConsentStatus()
        mockUserRepository.refreshConsentStatus()
    }

    override suspend fun getConsentStatus(): Result<ConsentStatus> =
        prefetchConsentStatus()

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
