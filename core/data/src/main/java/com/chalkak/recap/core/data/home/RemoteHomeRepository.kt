package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.home.remote.toDomain
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.home.HomeSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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
class RemoteHomeRepository(
    private val homeApi: HomeApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
    private val sessionTokenStore: SessionTokenStore,
    repositoryScope: CoroutineScope,
) : HomeRepository {
    @Inject
    constructor(
        homeApi: HomeApi,
        thumbnailCache: RemoteCaptureThumbnailCache,
        changeNotifier: RemoteCaptureChangeNotifier,
        sessionTokenStore: SessionTokenStore,
    ) : this(
        homeApi = homeApi,
        thumbnailCache = thumbnailCache,
        changeNotifier = changeNotifier,
        sessionTokenStore = sessionTokenStore,
        repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedSummary =
        sessionTokenStore.refreshToken
            .flatMapLatest { sessionKey ->
                if (sessionKey == null) {
                    flowOf(
                        SessionHomeSummary(
                            sessionKey = null,
                            result = Result.failure(MissingHomeSessionException()),
                        ),
                    )
                } else {
                    changeNotifier.changes
                        .onStart { emit(Unit) }
                        .mapLatest {
                            SessionHomeSummary(
                                sessionKey = sessionKey,
                                result = fetchSummary(),
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
    override fun observeSummary(): Flow<Result<HomeSummary>> =
        sessionTokenStore.refreshToken
            .flatMapLatest { sessionKey ->
                sharedSummary
                    .filter { summary -> summary.sessionKey == sessionKey }
                    .map { summary -> summary.result }
            }

    override suspend fun prefetchSummary(): Result<HomeSummary> = observeSummary().first()

    override fun refreshSummary() {
        changeNotifier.notifyCaptureChanged()
    }

    suspend fun getSummary(): Result<HomeSummary> = fetchSummary()

    private suspend fun fetchSummary(): Result<HomeSummary> =
        runRemoteCatchingSuspend {
            val summary = mapApiResponse(homeApi.getSummary()) { it.toDomain() }.getOrThrow()
            summary.copy(
                recentCaptures = summary.recentCaptures.withCachedThumbnails(),
                favorites = summary.favorites.withCachedThumbnails(),
                topTypes = summary.topTypes.map { topType ->
                    val cached = topType.representativeThumbnailUrl?.let { url ->
                        // topType has no captureId; keep remote URL / leave as-is
                        url
                    }
                    topType.copy(representativeThumbnailUrl = cached)
                },
            )
        }

    private suspend fun List<CaptureSummary>.withCachedThumbnails(): List<CaptureSummary> {
        val resolved = thumbnailCache.resolveThumbnailSources(
            map { summary -> summary.captureId to summary.thumbnailUrl },
        )
        return map { summary ->
            summary.copy(thumbnailUrl = resolved[summary.captureId] ?: summary.thumbnailUrl)
        }
    }

    private data class SessionHomeSummary(
        val sessionKey: String?,
        val result: Result<HomeSummary>,
    )

    private class MissingHomeSessionException : IllegalStateException(
        "A session is required to load the home summary",
    )
}
