package com.chalkak.recap.app.share

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal object OnboardingSampleShareSuccessStore {
    private val pendingEventIds = ConcurrentHashMap.newKeySet<String>()

    fun issueEventId(): String {
        return UUID.randomUUID().toString().also(pendingEventIds::add)
    }

    fun consume(eventId: String): Boolean = pendingEventIds.remove(eventId)
}
