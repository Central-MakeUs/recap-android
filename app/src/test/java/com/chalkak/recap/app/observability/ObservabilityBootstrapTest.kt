package com.chalkak.recap.app.observability

import com.chalkak.recap.core.data.BuildConfig
import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.backend.BackendSelection
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.ObservabilityKeys
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ObservabilityBootstrapTest {
    @Test
    fun `start sets backend_mode from BuildConfig immediately`() {
        val crashReporter = RecordingCrashReporter()
        val userPreferencesRepository = mockk<UserPreferencesRepository>()
        val sessionTokenStore = mockk<SessionTokenStore>()
        every { userPreferencesRepository.onboardingCompleted } returns MutableStateFlow(false)
        every { sessionTokenStore.refreshToken } returns MutableStateFlow(null)

        ObservabilityBootstrap(
            crashReporter = crashReporter,
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
        ).start()

        assertEquals(
            BackendSelection.backendModeLabel(BuildConfig.USE_MOCK_BACKEND),
            crashReporter.stringKeys[ObservabilityKeys.BACKEND_MODE],
        )
    }

    private class RecordingCrashReporter : CrashReporter {
        val stringKeys = mutableMapOf<String, String>()

        override fun setCustomKey(key: String, value: String) {
            stringKeys[key] = value
        }

        override fun setCustomKey(key: String, value: Boolean) = Unit

        override fun setCustomKey(key: String, value: Int) = Unit

        override fun log(message: String) = Unit

        override fun recordException(throwable: Throwable) = Unit
    }
}
