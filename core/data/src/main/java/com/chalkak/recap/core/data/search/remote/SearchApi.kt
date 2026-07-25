package com.chalkak.recap.core.data.search.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {
    @GET("api/v1/search")
    suspend fun search(
        @Query("q") q: String,
        @Query("scope") scope: String,
        @Query("typeCode") typeCode: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): SearchApiResponse
}
