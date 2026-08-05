package com.chalkak.recap.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NetworkConnectivityMonitorTest {
    private val context = mockk<Context>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private val network = mockk<Network>()

    private lateinit var monitor: NetworkConnectivityMonitor

    @BeforeEach
    fun setUp() {
        every {
            context.getSystemService(ConnectivityManager::class.java)
        } returns connectivityManager
        monitor = NetworkConnectivityMonitor(context)
    }

    @Test
    fun `internet is not validated when active network is absent`() {
        every { connectivityManager.activeNetwork } returns null

        assertFalse(monitor.isInternetValidated())
    }

    @Test
    fun `internet is not validated when validated capability is absent`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities(
            internet = true,
            validated = false,
        )

        assertFalse(monitor.isInternetValidated())
    }

    @Test
    fun `internet is validated when internet and validated capabilities are present`() {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities(
            internet = true,
            validated = true,
        )

        assertTrue(monitor.isInternetValidated())
    }

    private fun capabilities(
        internet: Boolean,
        validated: Boolean,
    ): NetworkCapabilities = mockk<NetworkCapabilities>().also { capabilities ->
        every {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } returns internet
        every {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } returns validated
    }
}
