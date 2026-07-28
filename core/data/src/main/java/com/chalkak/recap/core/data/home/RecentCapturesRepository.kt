package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.model.capture.CapturePage

interface RecentCapturesRepository {
    suspend fun getRecentCaptures(
        page: Int = 0,
        size: Int = 20,
    ): Result<CapturePage>
}
