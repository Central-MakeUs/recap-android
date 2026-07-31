package com.chalkak.recap.app.observability

import com.chalkak.recap.core.model.observability.CrashReporter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import timber.log.Timber

class CrashlyticsTimberTreeTest {
    private val crashReporter = RecordingCrashReporter()
    private val tree = CrashlyticsTimberTree(crashReporter)

    @AfterEach
    fun tearDown() {
        Timber.uproot(tree)
    }

    @Test
    fun `forwards an approved static breadcrumb`() {
        Timber.plant(tree)

        Timber.w("Screenshot analysis failed")

        assertEquals(listOf("Screenshot analysis failed"), crashReporter.logs)
    }

    @Test
    fun `drops breadcrumb containing screenshot metadata`() {
        Timber.plant(tree)

        Timber.w(
            "Failed screenshot upload jpeg preparation displayName=%s uri=%s",
            "private-photo.jpg",
            "content://photos/private-photo.jpg",
        )

        assertTrue(crashReporter.logs.isEmpty())
    }

    private class RecordingCrashReporter : CrashReporter {
        val logs = mutableListOf<String>()

        override fun setCustomKey(key: String, value: String) = Unit

        override fun setCustomKey(key: String, value: Boolean) = Unit

        override fun setCustomKey(key: String, value: Int) = Unit

        override fun log(message: String) {
            logs += message
        }

        override fun recordException(throwable: Throwable) = Unit
    }
}
