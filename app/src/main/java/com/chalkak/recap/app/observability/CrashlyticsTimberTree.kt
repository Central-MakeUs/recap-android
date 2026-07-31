package com.chalkak.recap.app.observability

import android.util.Log
import com.chalkak.recap.core.model.observability.CrashReporter
import timber.log.Timber

/**
 * Forwards only explicitly approved WARN+ Timber messages as Crashlytics breadcrumbs.
 * Non-fatals are recorded explicitly via [CrashReporter] to avoid duplicates.
 */
class CrashlyticsTimberTree(
    private val crashReporter: CrashReporter,
) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        if (message !in SAFE_BREADCRUMBS) return
        crashReporter.log(message)
    }

    private companion object {
        val SAFE_BREADCRUMBS = setOf(
            "Collection overview prefetch failed",
            "Consent status prefetch failed",
            "Data summary prefetch failed",
            "Failed to create screenshot thumbnail",
            "Failed to start organize foreground service",
            "Failed to stop organize foreground service",
            "Home summary prefetch failed",
            "Kakao email consent failed",
            "Kakao me() failed",
            "KakaoTalk login failed",
            "Screenshot analysis failed",
        )
    }
}
