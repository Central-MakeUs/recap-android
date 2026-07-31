package com.chalkak.recap.app.observability

import com.chalkak.recap.core.model.observability.PerformanceTrace
import com.chalkak.recap.core.model.observability.PerformanceTracer
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePerformanceTracer @Inject constructor() : PerformanceTracer {
    override fun startTrace(name: String): PerformanceTrace {
        val trace = FirebasePerformance.getInstance().newTrace(name)
        trace.start()
        return FirebasePerformanceTrace(trace)
    }
}

private class FirebasePerformanceTrace(
    private val trace: Trace,
) : PerformanceTrace {
    override fun putAttribute(key: String, value: String) {
        trace.putAttribute(key.take(MAX_ATTR_KEY), value.take(MAX_ATTR_VALUE))
    }

    override fun stop() {
        trace.stop()
    }

    private companion object {
        const val MAX_ATTR_KEY = 32
        const val MAX_ATTR_VALUE = 100
    }
}
