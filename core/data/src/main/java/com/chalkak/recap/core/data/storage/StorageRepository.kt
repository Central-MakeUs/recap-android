package com.chalkak.recap.core.data.storage

import com.chalkak.recap.core.model.capture.CaptureList
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import com.chalkak.recap.core.model.storage.StorageOverview
import com.chalkak.recap.core.model.storage.StorageType
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun observeOverview(searchQuery: String): Flow<StorageOverview>

    fun observeCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort = CaptureSort.Latest,
        searchQuery: String = "",
    ): Flow<CaptureList>

    fun observeFavoriteCaptures(
        sort: CaptureSort = CaptureSort.Latest,
        searchQuery: String = "",
    ): Flow<CaptureList>

    suspend fun getStorageTypes(): Result<List<StorageType>>

    suspend fun getCapturesByType(
        typeCode: ScreenshotContentType,
        sort: CaptureSort = CaptureSort.Latest,
    ): Result<CaptureList>

    suspend fun getFavoriteCaptures(): Result<CaptureList>

    suspend fun getEtcCaptures(sort: CaptureSort = CaptureSort.Latest): Result<CaptureList>
}
