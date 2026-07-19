package com.chalkak.recap.core.data.home

import com.chalkak.recap.core.data.home.remote.HomeApi
import com.chalkak.recap.core.data.home.remote.toDomain
import com.chalkak.recap.core.data.network.mapApiResponse
import com.chalkak.recap.core.data.network.runRemoteCatchingSuspend
import com.chalkak.recap.core.model.home.HomeSummary
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeApi: HomeApi,
) {
    suspend fun getSummary(): Result<HomeSummary> =
        runRemoteCatchingSuspend {
            mapApiResponse(homeApi.getSummary()) { it.toDomain() }.getOrThrow()
        }
}
