package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.home.remote.toDomain
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CaptureSummary
import com.chalkak.recap.core.model.home.HomeSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

@Singleton
class RemoteHomeRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : HomeRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSummary(): Flow<HomeSummary> {
        return changeNotifier.changes
            .onStart { emit(Unit) }
            .mapLatest {
                fetchSummary().getOrElse {
                    HomeSummary(
                        recentCaptures = emptyList(),
                        favorites = emptyList(),
                        topTypes = emptyList(),
                        hasAnyCapture = false,
                    )
                }
            }
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
}