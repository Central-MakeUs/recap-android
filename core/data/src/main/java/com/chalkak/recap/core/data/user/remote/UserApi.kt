package com.chalkak.recap.core.data.user.remote

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {
    @GET("api/v1/users/me")
    suspend fun getAccountInfo(): AccountInfoApiResponse

    @DELETE("api/v1/users/me")
    suspend fun withdraw()

    @GET("api/v1/users/me/data-summary")
    suspend fun getDataSummary(): DataSummaryApiResponse

    @DELETE("api/v1/users/me/data")
    suspend fun deleteAccountData()

    @GET("api/v1/users/me/consent")
    suspend fun getConsentStatus(): ConsentStatusApiResponse

    @POST("api/v1/users/me/consent")
    suspend fun giveConsent()

    @DELETE("api/v1/users/me/consent")
    suspend fun withdrawConsent()
}
