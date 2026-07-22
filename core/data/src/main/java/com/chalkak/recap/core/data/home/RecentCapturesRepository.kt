package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.model.capture.CaptureSummary
import kotlinx.coroutines.flow.Flow

// TODO: 구현 전
interface RecentCapturesRepository {
    fun observeRecentCaptures(): Flow<List<CaptureSummary>>
}
