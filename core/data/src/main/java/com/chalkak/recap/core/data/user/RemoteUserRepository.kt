package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.capture.CaptureChange
import com.chalkak.recap.core.data.capture.CaptureChangeNotifier
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn

@Singleton
class RemoteUserRepository(
    private val userApi: UserApi,
    private val sessionTokenStore: SessionTokenStore,
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: CaptureChangeNotifier,
    repositoryScope: CoroutineScope,
) : UserRepository {
    @Inject
    constructor(
        userApi: UserApi,
        sessionTokenStore: SessionTokenStore,
        screenshotCardRepository: ScreenshotCardRepository,
        thumbnailCache: RemoteCaptureThumbnailCache,
        changeNotifier: CaptureChangeNotifier,
    ) : this(
        userApi = userApi,
        sessionTokenStore = sessionTokenStore,
        screenshotCardRepository = screenshotCardRepository,
        thumbnailCache = thumbnailCache,
        changeNotifier = changeNotifier,
        repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    private val consentRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val dataSummaryRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedDataSummary =
        sessionTokenStore.refreshToken
            .flatMapLatest { sessionKey ->
                if (sessionKey == null) {
                    flowOf(
                        SessionDataSummary(
                            sessionKey = null,
                            result = Result.failure(MissingDataSummarySessionException()),
                        ),
                    )
                } else {
                    kotlinx.coroutines.flow.merge(
                        changeNotifier.changes.map { Unit },
                        dataSummaryRefresh,
                    )
                        .onStart { emit(Unit) }
                        .mapLatest {
                            SessionDataSummary(
                                sessionKey = sessionKey,
                                result = fetchDataSummary(),
                            )
                        }
                }
            }
            .shareIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5_000),
                replay = 1,
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedConsentStatus =
        sessionTokenStore.refreshToken
            .flatMapLatest { sessionKey ->
                if (sessionKey == null) {
                    flowOf(
                        SessionConsentStatus(
                            sessionKey = null,
                            result = Result.failure(MissingConsentSessionException()),
                        ),
                    )
                } else {
                    consentRefresh
                        .onStart { emit(Unit) }
                        .mapLatest {
                            SessionConsentStatus(
                                sessionKey = sessionKey,
                                result = fetchConsentStatus(),
                            )
                        }
                }
            }
            .shareIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5_000),
                replay = 1,
            )

    override suspend fun getAccountInfo(): Result<AccountInfo> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getAccountInfo()) { it.toDomain() }.getOrThrow()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeDataSummary(): Flow<Result<DataSummary>> =
        sessionTokenStore.refreshToken
            .flatMapLatest { sessionKey ->
                sharedDataSummary
                    .filter { summary -> summary.sessionKey == sessionKey }
                    .map { summary -> summary.result }
            }

    override suspend fun prefetchDataSummary(): Result<DataSummary> =
        observeDataSummary().first()

    override fun refreshDataSummary() {
        dataSummaryRefresh.tryEmit(Unit)
    }

    override suspend fun getDataSummary(): Result<DataSummary> =
        prefetchDataSummary()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeConsentStatus(): Flow<Result<ConsentStatus>> =
        sessionTokenStore.refreshToken
            .flatMapLatest { sessionKey ->
                sharedConsentStatus
                    .filter { status -> status.sessionKey == sessionKey }
                    .map { status -> status.result }
            }

    override suspend fun prefetchConsentStatus(): Result<ConsentStatus> =
        observeConsentStatus().first()

    override fun refreshConsentStatus() {
        consentRefresh.tryEmit(Unit)
    }

    override suspend fun getConsentStatus(): Result<ConsentStatus> =
        prefetchConsentStatus()

    override suspend fun giveConsent(): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            userApi.giveConsent()
        }
        if (result.isSuccess) {
            refreshConsentStatus()
        }
        return result
    }

    override suspend fun withdrawConsent(): Result<Unit> {
        val result = runRemoteCatchingSuspend {
            userApi.withdrawConsent()
        }
        if (result.isSuccess) {
            refreshConsentStatus()
        }
        return result
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
            changeNotifier.emit(CaptureChange.Invalidated)
        }
        return Result.success(Unit)
    }

    private suspend fun fetchDataSummary(): Result<DataSummary> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getDataSummary()) { it.toDomain() }.getOrThrow()
        }

    private suspend fun fetchConsentStatus(): Result<ConsentStatus> =
        runRemoteCatchingSuspend {
            mapApiResponse(userApi.getConsentStatus()) { it.toDomain() }.getOrThrow()
        }

    private data class SessionDataSummary(
        val sessionKey: String?,
        val result: Result<DataSummary>,
    )

    private data class SessionConsentStatus(
        val sessionKey: String?,
        val result: Result<ConsentStatus>,
    )

    private class MissingDataSummarySessionException : IllegalStateException(
        "A session is required to load the data summary",
    )

    private class MissingConsentSessionException : IllegalStateException(
        "A session is required to load the consent status",
    )
}
