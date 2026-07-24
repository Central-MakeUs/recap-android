package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-process registry for internal shared-analysis requests.
 * External apps can forge intent action/extras, but cannot register here.
 */
@Singleton
class SharedAnalysisRequestStore @Inject constructor() {
    private val pendingByRequestId = ConcurrentHashMap<String, List<LocalImage>>()

    fun register(requestId: String, images: List<LocalImage>) {
        require(requestId.isNotBlank())
        require(images.isNotEmpty())
        pendingByRequestId[requestId] = images.toList()
    }

    fun consume(requestId: String): List<LocalImage>? {
        if (requestId.isBlank()) {
            return null
        }
        return pendingByRequestId.remove(requestId)
    }

    fun peek(requestId: String): List<LocalImage>? = pendingByRequestId[requestId]
}
