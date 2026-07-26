package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
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

@Singleton
class RemoteSearchRepository @Inject constructor(
    private val searchApi: SearchApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
) : SearchRepository {
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

    private suspend fun SearchPage.withCachedThumbnails(): SearchPage {
        val resolved = thumbnailCache.resolveThumbnailSources(
            items.map { result -> result.captureId to result.thumbnailUrl },
        )
        val enriched = items.map { result ->
            result.copy(thumbnailUrl = resolved[result.captureId] ?: result.thumbnailUrl)
        }
        return copy(items = enriched)
    }
}
