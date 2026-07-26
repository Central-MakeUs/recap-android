package com.chalkak.recap.core.data.search

import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.search.SearchPage
import com.chalkak.recap.core.model.search.SearchScope

interface SearchRepository {
    suspend fun search(
        query: String,
        scope: SearchScope,
        typeCode: ScreenshotContentType? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<SearchPage>
}
