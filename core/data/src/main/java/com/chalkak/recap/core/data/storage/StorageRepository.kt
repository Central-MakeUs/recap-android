package com.chalkak.recap.core.data.storage

import com.chalkak.recap.core.data.capture.remote.toCardTypeDto
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.data.storage.remote.StorageApi
import com.chalkak.recap.core.data.storage.remote.toDomain
import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageType
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val storageApi: StorageApi,
) {
    suspend fun getTypes(): Result<List<StorageType>> =
        runRemoteCatchingSuspend {
            mapApiResponse(storageApi.getTypes()) { list ->
                list.map { it.toDomain() }
            }.getOrThrow()
        }

    suspend fun getTypeCaptures(
        typeCode: ScreenshotContentType,
        sort: CaptureSort = CaptureSort.Latest,
    ): Result<CaptureList> =
        runRemoteCatchingSuspend {
            mapApiResponse(
                storageApi.getTypeCaptures(
                    typeCode = typeCode.toCardTypeDto().name,
                    sort = sort.toQuery(),
                ),
            ) { it.toDomain() }.getOrThrow()
        }

    suspend fun getFavorites(): Result<CaptureList> =
        runRemoteCatchingSuspend {
            mapApiResponse(storageApi.getFavorites()) { it.toDomain() }.getOrThrow()
        }

    suspend fun getEtc(sort: CaptureSort = CaptureSort.Latest): Result<CaptureList> =
        runRemoteCatchingSuspend {
            mapApiResponse(storageApi.getEtc(sort = sort.toQuery())) { it.toDomain() }.getOrThrow()
        }

    private fun CaptureSort.toQuery(): String =
        when (this) {
            CaptureSort.Latest -> "latest"
            CaptureSort.Oldest -> "oldest"
        }
}
