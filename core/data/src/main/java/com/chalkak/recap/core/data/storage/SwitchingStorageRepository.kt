package com.chalkak.recap.core.data.storage

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageOverview
import com.chalkak.recap.core.model.storage.StorageType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@Singleton
class SwitchingStorageRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockStorageRepository: MockStorageRepository,
    private val remoteStorageRepository: RemoteStorageRepository,
) : StorageRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOverview(searchQuery: String): Flow<StorageOverview> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            resolve(mode).observeOverview(searchQuery)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort,
        searchQuery: String,
    ): Flow<CaptureList> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            resolve(mode).observeCapturesByType(
                typeCode = typeCode,
                sort = sort,
                searchQuery = searchQuery,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFavoriteCaptures(
        sort: CaptureSort,
        searchQuery: String,
    ): Flow<CaptureList> {
        return screenshotBackendModeStore.mode.flatMapLatest { mode ->
            resolve(mode).observeFavoriteCaptures(sort = sort, searchQuery = searchQuery)
        }
    }

    override suspend fun getStorageTypes(): Result<List<StorageType>> =
        resolveDelegate().getStorageTypes()

    override suspend fun getCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort,
    ): Result<CaptureList> =
        resolveDelegate().getCapturesByType(typeCode = typeCode, sort = sort)

    override suspend fun getFavoriteCaptures(): Result<CaptureList> =
        resolveDelegate().getFavoriteCaptures()

    override suspend fun getEtcCaptures(sort: CaptureSort): Result<CaptureList> =
        resolveDelegate().getEtcCaptures(sort = sort)

    private fun resolve(mode: ScreenshotBackendMode): StorageRepository {
        return when (mode) {
            ScreenshotBackendMode.MOCK -> mockStorageRepository
            ScreenshotBackendMode.REMOTE -> remoteStorageRepository
        }
    }

    private suspend fun resolveDelegate(): StorageRepository {
        return resolve(screenshotBackendModeStore.currentMode())
    }
}
