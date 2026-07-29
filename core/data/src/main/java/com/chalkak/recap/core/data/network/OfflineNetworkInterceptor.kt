package com.chalkak.recap.core.data.network

import java.io.IOException
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Fails immediately when the device has no active network.
 * Reachability through an active network is determined by the actual request.
 */
class OfflineNetworkInterceptor @Inject constructor(
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!networkConnectivityMonitor.hasActiveNetwork()) {
            throw IOException(NO_ACTIVE_NETWORK_MESSAGE)
        }
        return chain.proceed(chain.request())
    }

    companion object {
        const val NO_ACTIVE_NETWORK_MESSAGE = "No active network connection"
    }
}
