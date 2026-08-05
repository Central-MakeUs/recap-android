package com.chalkak.recap.core.data.backend

import javax.inject.Provider

/**
 * Build-time Mock/Remote selection helpers shared by Hilt modules and observability.
 */
object BackendSelection {
    fun <T> select(
        useMockBackend: Boolean,
        mockProvider: Provider<out T>,
        remoteProvider: Provider<out T>,
    ): T {
        return if (useMockBackend) {
            mockProvider.get()
        } else {
            remoteProvider.get()
        }
    }

    fun backendModeLabel(useMockBackend: Boolean): String =
        if (useMockBackend) "mock" else "remote"
}
fun <T> select(
    useMockBackend: Boolean,
    mockProvider: Provider<out T>,
    remoteProvider: Provider<out T>,
): T {
    return if (useMockBackend) {
        mockProvider.get()
    } else {
        remoteProvider.get()
    }
}
