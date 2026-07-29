package com.chalkak.recap.core.data.network

import android.content.Context
import android.net.ConnectivityManager
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
}
