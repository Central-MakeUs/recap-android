package com.chalkak.recap.core.data.storage

import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.capture.remote.toCardTypeDto
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.capture.sortedByOrganizedAt
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.data.storage.remote.StorageApi
import com.chalkak.recap.core.data.storage.remote.toDomain
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageOverview
import com.chalkak.recap.core.model.storage.StorageType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

@Singleton
class RemoteStorageRepository @Inject constructor(
    private val storageApi: StorageApi,
    private val thumbnailCache: RemoteCaptureThumbnailCache,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : StorageRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOverview(searchQuery: String): Flow<StorageOverview> {
        // Remote search is deferred; searchQuery is intentionally ignored.
        return changeNotifier.changes
            .onStart { emit(Unit) }
            .mapLatest {
                fetchOverview().getOrElse {
                    StorageOverview(
                        hasAnyCapture = false,
                        favoriteCount = 0,
                        types = emptyList(),
                    )
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort,
        searchQuery: String,
    ): Flow<CaptureList> {
        return changeNotifier.changes
            .onStart { emit(Unit) }
            .mapLatest {
                getCapturesByType(typeCode = typeCode, sort = sort)
                    .getOrElse { CaptureList(count = 0, items = emptyList()) }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFavoriteCaptures(
        sort: CaptureSort,
        searchQuery: String,
    ): Flow<CaptureList> {
        return changeNotifier.changes
            .onStart { emit(Unit) }
            .mapLatest {
                getFavoriteCaptures()
                    .map { list -> list.copy(items = list.items.sortedByOrganizedAt(sort)) }
                    .getOrElse { CaptureList(count = 0, items = emptyList()) }
            }
    }

    override suspend fun getStorageTypes(): Result<List<StorageType>> =
        runRemoteCatchingSuspend {
            mapApiResponse(storageApi.getTypes()) { list ->
                list.map { it.toDomain() }
            }.getOrThrow()
        }

    override suspend fun getCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort,
    ): Result<CaptureList> =
        runRemoteCatchingSuspend {
            mapApiResponse(
                storageApi.getTypeCaptures(
                    typeCode = typeCode.toCardTypeDto().name,
                    sort = sort.toQuery(),
                ),
            ) { it.toDomain() }.getOrThrow().withCachedThumbnails()
        }

    override suspend fun getFavoriteCaptures(): Result<CaptureList> =
        runRemoteCatchingSuspend {
            mapApiResponse(storageApi.getFavorites()) { it.toDomain() }
                .getOrThrow()
                .withCachedThumbnails()
        }

    override suspend fun getEtcCaptures(sort: CaptureSort): Result<CaptureList> =
        runRemoteCatchingSuspend {
            mapApiResponse(storageApi.getEtc(sort = sort.toQuery())) { it.toDomain() }
                .getOrThrow()
                .withCachedThumbnails()
        }

    private suspend fun fetchOverview(): Result<StorageOverview> =
        runRemoteCatchingSuspend {
            val types = getStorageTypes().getOrThrow()
            val favorites = getFavoriteCaptures().getOrThrow()
            StorageOverview(
                hasAnyCapture = types.any { it.count > 0 } || favorites.count > 0,
                favoriteCount = favorites.count,
                types = types.filter { it.count > 0 },
            )
        }

    private suspend fun CaptureList.withCachedThumbnails(): CaptureList {
        val resolved = thumbnailCache.resolveThumbnailSources(
            items.map { summary -> summary.captureId to summary.thumbnailUrl },
        )
        val enriched = items.map { summary ->
            summary.copy(thumbnailUrl = resolved[summary.captureId] ?: summary.thumbnailUrl)
        }
        return copy(items = enriched)
    }

    private fun CaptureSort.toQuery(): String =
        when (this) {
            CaptureSort.Latest -> "latest"
            CaptureSort.Oldest -> "oldest"
        }
}
