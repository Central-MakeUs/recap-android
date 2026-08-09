package com.chalkak.recap.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import app.cash.turbine.test
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun `validatedInternet emits current value and capability changes`() = runTest {
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities(
            internet = true,
            validated = false,
        )
        val callbackSlot = slot<ConnectivityManager.NetworkCallback>()
        every { connectivityManager.registerDefaultNetworkCallback(capture(callbackSlot)) } just Runs
        every {
            connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
        } just Runs

        monitor.validatedInternet.test {
            assertEquals(false, awaitItem())

            every { connectivityManager.getNetworkCapabilities(network) } returns capabilities(
                internet = true,
                validated = true,
            )
            callbackSlot.captured.onCapabilitiesChanged(
                network,
                capabilities(internet = true, validated = true),
            )
            assertEquals(true, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) {
            connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
        }
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
