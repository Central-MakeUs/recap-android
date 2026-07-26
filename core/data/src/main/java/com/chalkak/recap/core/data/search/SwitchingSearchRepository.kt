package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendMode
import com.chalkak.recap.core.data.screenshot.backend.ScreenshotBackendModeStore
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwitchingSearchRepository @Inject constructor(
    private val screenshotBackendModeStore: ScreenshotBackendModeStore,
    private val mockSearchRepository: MockSearchRepository,
    private val remoteSearchRepository: RemoteSearchRepository,
) : SearchRepository {
    override suspend fun search(
        query: String,
        scope: SearchScope,
        typeCode: ScreenshotContentType?,
        page: Int,
        size: Int,
    ): Result<SearchPage> =
        resolveDelegate().search(
            query = query,
            scope = scope,
            typeCode = typeCode,
            page = page,
            size = size,
        )

    private suspend fun resolveDelegate(): SearchRepository {
        return when (screenshotBackendModeStore.currentMode()) {
            ScreenshotBackendMode.MOCK -> mockSearchRepository
            ScreenshotBackendMode.REMOTE -> remoteSearchRepository
        }
    }
}
