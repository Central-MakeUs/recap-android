package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CapturePage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

@Singleton
class RemoteRecentCapturesRepository @Inject constructor(
    private val homeApi: HomeApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : RecentCapturesRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeRecentCaptures(
        page: Int,
        size: Int,
    ): Flow<Result<CapturePage>> {
        return changeNotifier.changes
            .onStart { emit(Unit) }
            .mapLatest {
                getRecentCaptures(page = page, size = size)
            }
    }

    override suspend fun getRecentCaptures(
        page: Int,
        size: Int,
    ): Result<CapturePage> =
        runRemoteCatchingSuspend {
            mapApiResponse(
                homeApi.getRecentCaptures(page = page, size = size),
            ) { it.toDomain() }.getOrThrow().withCachedThumbnails()
        }

    private fun CapturePage.withCachedThumbnails(): CapturePage {
        val resolved = thumbnailCache.resolveThumbnailSources(
            items.map { summary -> summary.captureId to summary.thumbnailUrl },
        )
        val enriched = items.map { summary ->
            summary.copy(thumbnailUrl = resolved[summary.captureId])
        }
        return copy(items = enriched)
    }
}
