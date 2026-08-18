package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.CaptureChange
import com.chalkak.recap.core.data.capture.CaptureChangeNotifier
import com.chalkak.recap.core.data.capture.remote.toCardTypeDto
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.data.search.remote.SearchApi
import com.chalkak.recap.core.data.search.remote.toDomain
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

@Singleton
class RemoteSearchRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: CaptureChangeNotifier,
) : SearchRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSearch(
        query: String,
        scope: SearchScope,
        typeCode: ScreenshotContentType?,
        size: Int,
    ): Flow<Result<SearchPage>> =
        changeNotifier.changes
            .onStart { emit(CaptureChange.Invalidated) }
            .mapLatest {
                search(
                    query = query,
                    scope = scope,
                    typeCode = typeCode,
                    page = 0,
                    size = size,
                )
            }

    override suspend fun search(
        query: String,
        scope: SearchScope,
        typeCode: ScreenshotContentType?,
        page: Int,
        size: Int,
    ): Result<SearchPage> =
        runRemoteCatchingSuspend {
            mapApiResponse(
                searchApi.search(
                    q = query,
                    scope = scope.name,
                    typeCode = typeCode?.toCardTypeDto()?.name,
                    page = page,
                    size = size,
                ),
            ) { it.toDomain() }.getOrThrow().withCachedThumbnails()
        }

    private fun SearchPage.withCachedThumbnails(): SearchPage {
        val resolved = thumbnailCache.resolveThumbnailSources(
            items.map { result -> result.captureId to result.thumbnailUrl },
        )
        val enriched = items.map { result ->
            result.copy(thumbnailUrl = resolved[result.captureId])
        }
        return copy(items = enriched)
    }
}
