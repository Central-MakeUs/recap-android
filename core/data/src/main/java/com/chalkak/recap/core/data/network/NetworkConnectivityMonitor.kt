package com.chalkak.recap.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

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

    /**
     * Active default network의 INTERNET + VALIDATED 여부를 방출한다.
     * 콜드 Flow이며, 수집이 시작될 때 [NetworkCallback]을 등록한다.
     */
    val validatedInternet: Flow<Boolean> = callbackFlow {
        val sendCurrent = {
            trySend(isInternetValidated())
            Unit
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                sendCurrent()
            }

            override fun onLost(network: Network) {
                sendCurrent()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(networkCapabilities.hasInternetAndValidated())
            }
        }
        sendCurrent()
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}

internal fun NetworkCapabilities.hasInternetAndValidated(): Boolean =
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
