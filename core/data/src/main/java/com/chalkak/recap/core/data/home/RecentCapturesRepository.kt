package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.model.capture.CapturePage
import kotlinx.coroutines.flow.Flow

interface RecentCapturesRepository {
    fun observeRecentCaptures(
        page: Int = 0,
        size: Int = 20,
    ): Flow<Result<CapturePage>>

    suspend fun getRecentCaptures(
        page: Int = 0,
        size: Int = 20,
    ): Result<CapturePage>
}
