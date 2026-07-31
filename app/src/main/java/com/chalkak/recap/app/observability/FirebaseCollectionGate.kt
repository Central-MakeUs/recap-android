package com.chalkak.recap.app.observability

import com.chalkak.recap.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

object FirebaseCollectionGate {
    fun apply() {
        val enableCrashAndPerf = !BuildConfig.DEBUG
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enableCrashAndPerf
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enableCrashAndPerf
    }
}
