package com.chalkak.recap.app.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

object FirebaseCollectionGate {
    fun apply() {
        // Demo qa/release must not send crashes or traces to the production Firebase project.
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = false
    }
}
