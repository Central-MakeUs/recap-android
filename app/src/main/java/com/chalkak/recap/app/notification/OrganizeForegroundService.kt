package com.chalkak.recap.app.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Organize progress foreground service.
 *
 * Starts with user-initiated organize work and remains in the foreground until that work finishes.
 */
@AndroidEntryPoint
class OrganizeForegroundService : Service() {
    @Inject
    lateinit var progressTracker: OrganizeProgressTracker

    @Inject
    lateinit var notificationNotifier: OrganizeNotificationNotifier

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var progressJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val snapshot = progressTracker.snapshot.value
        startProgressForeground(snapshot.completedCount, snapshot.totalCount)
        if (snapshot.isRunning) {
            observeProgress()
        } else {
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        progressJob?.cancel()
        progressJob = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationNotifier.cancelProgress()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun observeProgress() {
        if (progressJob?.isActive == true) return
        progressJob = serviceScope.launch {
            progressTracker.snapshot.collectLatest { snapshot ->
                if (!snapshot.isRunning) {
                    stopSelf()
                    return@collectLatest
                }
                startProgressForeground(snapshot.completedCount, snapshot.totalCount)
            }
        }
    }

    private fun startProgressForeground(completedCount: Int, totalCount: Int) {
        notificationNotifier.ensureChannels()
        val notification = notificationNotifier.buildProgressNotification(
            completedCount = completedCount,
            totalCount = totalCount,
        )
        ServiceCompat.startForeground(
            this,
            OrganizeNotificationNotifier.PROGRESS_NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC, // minSdk >= 29
        )
    }

    companion object {
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, OrganizeForegroundService::class.java),
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OrganizeForegroundService::class.java))
        }
    }
}
