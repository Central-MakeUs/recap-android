package com.chalkak.recap.app.observability

import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.model.observability.CrashReporter
import com.chalkak.recap.core.model.observability.ObservabilityKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservabilityBootstrap @Inject constructor(
    private val crashReporter: CrashReporter,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionTokenStore: SessionTokenStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            combine(
                userPreferencesRepository.onboardingCompleted,
                sessionTokenStore.refreshToken.map { token -> !token.isNullOrBlank() },
            ) { onboardingCompleted, loggedIn ->
                SessionKeys(
                    onboardingCompleted = onboardingCompleted,
                    loggedIn = loggedIn,
                )
            }
                .distinctUntilChanged()
                .collect { keys ->
                    crashReporter.setCustomKey(
                        ObservabilityKeys.ONBOARDING_COMPLETED,
                        keys.onboardingCompleted,
                    )
                    crashReporter.setCustomKey(ObservabilityKeys.LOGGED_IN, keys.loggedIn)
                }
        }
    }

    private data class SessionKeys(
        val onboardingCompleted: Boolean,
        val loggedIn: Boolean,
    )
}
