package com.chalkak.recap.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

private const val SafeDataMaxAttempts = 3
private val SafeDataRetryDelaysMs = longArrayOf(100L, 300L)

/**
 * Reads [DataStore.data] with up to two automatic re-collections on [IOException]
 * (3 attempts total). Non-IO failures and cancellation are not swallowed.
 *
 * [name] is only used for logs so failures can be traced to the right DataStore file.
 */
internal fun DataStore<Preferences>.safeData(name: String): Flow<Preferences> = flow {
    var attempt = 0
    while (true) {
        try {
            data.collect { preferences -> emit(preferences) }
            return@flow
        } catch (exception: IOException) {
            attempt++
            if (attempt >= SafeDataMaxAttempts) {
                Timber.e(
                    exception,
                    "%s DataStore read failed after %d attempts",
                    name,
                    SafeDataMaxAttempts,
                )
                throw exception
            }
            Timber.w(
                exception,
                "%s DataStore read failed; retrying (%d/%d)",
                name,
                attempt,
                SafeDataMaxAttempts,
            )
            delay(SafeDataRetryDelaysMs[attempt - 1].milliseconds)
        }
    }
}
