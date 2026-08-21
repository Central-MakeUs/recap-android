package com.chalkak.recap.core.data.backend

import javax.inject.Provider

/**
 * Build-time Mock/Remote selection helpers shared by Hilt modules.
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
