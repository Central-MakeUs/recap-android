package com.chalkak.recap.core.model.observability

interface PerformanceTrace {
    fun putAttribute(key: String, value: String)

    fun stop()
}

interface PerformanceTracer {
    fun startTrace(name: String): PerformanceTrace

    companion object {
        val NoOp: PerformanceTracer = object : PerformanceTracer {
            override fun startTrace(name: String): PerformanceTrace =
                object : PerformanceTrace {
                    override fun putAttribute(key: String, value: String) = Unit

                    override fun stop() = Unit
                }
        }
    }
}

object PerformanceTraceNames {
    const val ORGANIZE_REMOTE_END_TO_END = "organize_remote_end_to_end"
    const val SHARE_INTAKE_TO_ORGANIZE = "share_intake_to_organize"
}

object ObservabilityKeys {
    const val ONBOARDING_COMPLETED = "onboarding_completed"
    const val LOGGED_IN = "logged_in"
    const val BACKEND_MODE = "backend_mode"
    const val ORGANIZE_ACTIVE = "organize_active"
    const val IMAGE_COUNT = "image_count"
    const val ORGANIZE_PHASE = "organize_phase"
    const val SHARE_ENTRY = "share_entry"
    const val AUTH_ERROR_CODE = "auth_error_code"
    const val ENTRY = "entry"
    const val OUTCOME = "outcome"
    const val GATE = "gate"
}

object OrganizeTraceEntry {
    const val ONBOARDING_FIRST = "onboarding_first"
    const val HOME_ORGANIZE = "home_organize"
    const val SHARE = "share"
}

