package com.chalkak.recap.app.notification

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.chalkak.recap.core.data.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganizeNotificationCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val progressTracker: OrganizeProgressTracker,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationNotifier: OrganizeNotificationNotifier,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val isAppInForeground = MutableStateFlow(true)
    private var started = false

    fun start() {
        if (started) return
        started = true
        notificationNotifier.ensureChannels()

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    isAppInForeground.value = true
                }

                override fun onStop(owner: LifecycleOwner) {
                    isAppInForeground.value = false
                }
            },
        )

        scope.launch {
            combine(
                progressTracker.snapshot.map { snapshot -> snapshot.isRunning },
                userPreferencesRepository.organizeCompleteNotificationEnabled,
            ) { isRunning, organizeCompleteNotificationEnabled ->
                shouldShowProgress(
                    isRunning = isRunning,
                    organizeCompleteNotificationEnabled = organizeCompleteNotificationEnabled,
                )
            }
                .distinctUntilChanged()
                .collect { showProgress ->
                    if (showProgress) {
                        startProgressService()
                    } else {
                        stopProgressService()
                    }
                }
        }

        scope.launch {
            progressTracker.terminalResults.collect { terminal ->
                stopProgressService()
                val organizeCompleteNotificationEnabled =
                    userPreferencesRepository.organizeCompleteNotificationEnabled.first()
                if (
                    shouldNotifyTerminal(
                        isAppInForeground = isAppInForeground.value,
                        organizeCompleteNotificationEnabled = organizeCompleteNotificationEnabled,
                        notificationsEnabled = notificationNotifier.areNotificationsEnabled(),
                    )
                ) {
                    notificationNotifier.notifyTerminal(terminal)
                }
            }
        }
    }

    private fun startProgressService() {
        try {
            OrganizeForegroundService.start(context)
        } catch (error: Exception) {
            Timber.w(error, "Failed to start organize foreground service")
        }
    }

    private fun stopProgressService() {
        try {
            OrganizeForegroundService.stop(context)
        } catch (error: Exception) {
            Timber.w(error, "Failed to stop organize foreground service")
        } finally {
            notificationNotifier.cancelProgress()
        }
    }
}

internal fun shouldShowProgress(
    isRunning: Boolean,
    organizeCompleteNotificationEnabled: Boolean,
): Boolean = isRunning && organizeCompleteNotificationEnabled

internal fun shouldNotifyTerminal(
    isAppInForeground: Boolean,
    organizeCompleteNotificationEnabled: Boolean,
    notificationsEnabled: Boolean,
): Boolean = !isAppInForeground &&
    organizeCompleteNotificationEnabled &&
    notificationsEnabled
