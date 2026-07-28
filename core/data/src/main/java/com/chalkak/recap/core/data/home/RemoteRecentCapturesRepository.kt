package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.capture.CaptureSummary
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
    override fun observeRecentCaptures(): Flow<List<CaptureSummary>> {
        return changeNotifier.changes
            .onStart { emit(Unit) }
            .mapLatest {
                fetchAllRecentCaptures().getOrElse { emptyList() }
            }
    }

    suspend fun getRecentCapturesPage(
        page: Int = 0,
        size: Int = DEFAULT_PAGE_SIZE,
    ) = runRemoteCatchingSuspend {
        mapApiResponse(homeApi.getRecentCaptures(page = page, size = size)) { it.toDomain() }.getOrThrow()
    }

    private suspend fun fetchAllRecentCaptures(): Result<List<CaptureSummary>> =
        runRemoteCatchingSuspend {
            val items = mutableListOf<CaptureSummary>()
            var page = 0
            while (true) {
                val response = mapApiResponse(
                    homeApi.getRecentCaptures(page = page, size = DEFAULT_PAGE_SIZE),
                ) { it.toDomain() }.getOrThrow()
                items += response.items
                if (!response.hasNext) {
                    break
                }
                page += 1
            }
            items.withCachedThumbnails()
        }

    private suspend fun List<CaptureSummary>.withCachedThumbnails(): List<CaptureSummary> {
        val resolved = thumbnailCache.resolveThumbnailSources(
            map { summary -> summary.captureId to summary.thumbnailUrl },
        )
        return map { summary ->
            summary.copy(thumbnailUrl = resolved[summary.captureId] ?: summary.thumbnailUrl)
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
