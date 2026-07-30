package com.chalkak.recap.core.data.home.remote

import com.chalkak.recap.core.data.capture.remote.CapturePageApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {
    @GET("api/v1/home/summary")
    suspend fun getSummary(): HomeSummaryApiResponse

    @GET("api/v1/home/recent-captures")
    suspend fun getRecentCaptures(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): CapturePageApiResponse
}
