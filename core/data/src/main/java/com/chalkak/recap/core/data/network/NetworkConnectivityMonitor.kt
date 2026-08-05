package com.chalkak.recap.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val connectivityManager =
        context.getSystemService(ConnectivityManager::class.java)

    fun hasActiveNetwork(): Boolean = connectivityManager.activeNetwork != null

    /**
     * UI 게이트용 동기 판정. active network에 INTERNET + VALIDATED capability가 있는지 본다.
     */
    fun isInternetValidated(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasInternetAndValidated()
    }
}

private fun NetworkCapabilities.hasInternetAndValidated(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
