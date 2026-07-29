package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-process registry for internal shared-analysis requests.
 * External apps can forge intent action/extras, but cannot register here.
 * Prepared JPEG bytes stay in this process-only store — never in Intent extras.
 */
@Singleton
class SharedAnalysisRequestStore @Inject constructor() {
    private val pendingByRequestId = ConcurrentHashMap<String, List<ScreenshotUploadCandidate>>()

    fun register(requestId: String, candidates: List<ScreenshotUploadCandidate>) {
        require(requestId.isNotBlank())
        require(candidates.isNotEmpty())
        pendingByRequestId[requestId] = candidates.toList()
    }

    fun consume(requestId: String): List<ScreenshotUploadCandidate>? {
        if (requestId.isBlank()) {
            return null
        }
        return pendingByRequestId.remove(requestId)
    }

    fun peek(requestId: String): List<ScreenshotUploadCandidate>? = pendingByRequestId[requestId]
}
