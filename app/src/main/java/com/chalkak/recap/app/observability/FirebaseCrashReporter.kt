package com.chalkak.recap.app.observability

import com.chalkak.recap.core.model.observability.CrashReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor() : CrashReporter {
    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun log(message: String) {
        crashlytics.log(message.take(MAX_LOG_LENGTH))
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    private companion object {
        const val MAX_LOG_LENGTH = 1_024
    }
}
