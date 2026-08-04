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
 */
internal fun DataStore<Preferences>.safeData(): Flow<Preferences> = flow {
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
                    "user_preferences DataStore read failed after %d attempts",
                    SafeDataMaxAttempts,
                )
                throw exception
            }
            Timber.w(
                exception,
                "user_preferences DataStore read failed; retrying (%d/%d)",
                attempt,
                SafeDataMaxAttempts,
            )
            delay(SafeDataRetryDelaysMs[attempt - 1].milliseconds)
        }
    }
}
