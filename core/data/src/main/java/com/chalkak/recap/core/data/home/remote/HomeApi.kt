package com.chalkak.recap.core.data.home.remote

import retrofit2.http.GET

interface HomeApi {
    @GET("api/v1/home/summary")
    suspend fun getSummary(): HomeSummaryApiResponse
}
