package com.chalkak.recap.core.data.storage.remote

import com.chalkak.recap.core.data.capture.remote.CaptureListApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StorageApi {
    @GET("api/v1/storage/types")
    suspend fun getTypes(): StorageTypesApiResponse

    @GET("api/v1/storage/types/{typeCode}/captures")
    suspend fun getTypeCaptures(
        @Path("typeCode") typeCode: String,
        @Query("sort") sort: String? = null,
    ): CaptureListApiResponse

    @GET("api/v1/storage/favorites")
    suspend fun getFavorites(): CaptureListApiResponse

    @GET("api/v1/storage/etc")
    suspend fun getEtc(
        @Query("sort") sort: String? = null,
    ): CaptureListApiResponse
}
