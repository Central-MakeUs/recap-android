package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.data.user.remote.UserApi
import com.chalkak.recap.core.data.user.remote.toDomain
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteUserRepository @Inject constructor(
    private val userApi: UserApi,
    private val sessionTokenStore: SessionTokenStore,
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : UserRepository {
    override suspend fun getAccountInfo(): Result<AccountInfo> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getAccountInfo()) { it.toDomain() }.getOrThrow()
        }

    override suspend fun getDataSummary(): Result<DataSummary> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getDataSummary()) { it.toDomain() }.getOrThrow()
        }

    override suspend fun getConsentStatus(): Result<ConsentStatus> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getConsentStatus()) { it.toDomain() }.getOrThrow()
        }

    override suspend fun giveConsent(): Result<Unit> =
        runRemoteCatchingSuspend {
            userApi.giveConsent()
        }

    override suspend fun withdrawConsent(): Result<Unit> =
        runRemoteCatchingSuspend {
            userApi.withdrawConsent()
        }

    override suspend fun withdraw(): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            userApi.withdraw()
        }
        // 서버 실패여도 로컬 세션은 비워 재로그인 가능하게 한다.
        sessionTokenStore.clear()
        return result
    }

    override suspend fun deleteAccountData(): Result<Unit> {
        val remoteResult = runRemoteCatchingSuspend {
            userApi.deleteAccountData()
        }
        if (remoteResult.isFailure) {
            return remoteResult
        }
        runCatching {
            screenshotCardRepository.deleteAllCards()
            thumbnailCache.clearAll()
            changeNotifier.notifyCaptureChanged()
        }
        return Result.success(Unit)
    }
}
