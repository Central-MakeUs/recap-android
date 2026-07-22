package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.model.capture.CaptureSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class StubRemoteRecentCapturesRepository @Inject constructor() : RecentCapturesRepository {
    override fun observeRecentCaptures(): Flow<List<CaptureSummary>> {
        // Full recent list API is deferred; Remote shows empty state for now.
        return flowOf(emptyList())
    }
}
