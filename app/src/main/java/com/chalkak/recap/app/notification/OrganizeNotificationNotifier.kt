package com.chalkak.recap.app.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chalkak.recap.MainActivity
import com.chalkak.recap.core.design.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganizeNotificationNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        // Delete legacy low/silent channel so progress is not buried on OEM shades.
        manager.deleteNotificationChannel(CHANNEL_PROGRESS_LEGACY)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PROGRESS,
                context.getString(R.string.organize_notification_channel_progress_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(
                    R.string.organize_notification_channel_progress_description,
                )
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_RESULT,
                context.getString(R.string.organize_notification_channel_result_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(
                    R.string.organize_notification_channel_result_description,
                )
            },
        )
    }

    fun areNotificationsEnabled(): Boolean =
        notificationManager.areNotificationsEnabled()

    fun buildProgressNotification(completedCount: Int, totalCount: Int): Notification {
        ensureChannels()
        val safeTotal = totalCount.coerceAtLeast(0)
        val safeCompleted = completedCount.coerceIn(0, safeTotal.coerceAtLeast(completedCount))
        return baseBuilder(CHANNEL_PROGRESS)
            .setContentTitle(context.getString(R.string.organize_notification_progress_title))
            .setContentText(
                context.getString(
                    R.string.organize_notification_progress_text,
                    safeCompleted,
                    safeTotal,
                ),
            )
            .setProgress(safeTotal.coerceAtLeast(1), safeCompleted, safeTotal <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyProgress(completedCount: Int, totalCount: Int) {
        if (!areNotificationsEnabled()) return
        notificationManager.notify(
            PROGRESS_NOTIFICATION_ID,
            buildProgressNotification(completedCount, totalCount),
        )
    }

    fun cancelProgress() {
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun notifyTerminal(result: OrganizeTerminalResult) {
        if (!areNotificationsEnabled()) return
        ensureChannels()
        cancelProgress()
        val (title, text) = when (result) {
            is OrganizeTerminalResult.AllSuccess -> {
                context.getString(R.string.organize_notification_success_title) to
                    context.getString(R.string.organize_notification_success_text)
            }

            is OrganizeTerminalResult.PartialSuccess -> {
                context.getString(R.string.organize_notification_partial_title) to
                    context.getString(
                        R.string.organize_notification_partial_text,
                        result.successCount,
                        result.failCount,
                    )
            }

            OrganizeTerminalResult.AllFailed -> {
                context.getString(R.string.organize_notification_failure_title) to
                    context.getString(R.string.organize_notification_failure_text)
            }
        }
        val notification = baseBuilder(CHANNEL_RESULT)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(RESULT_NOTIFICATION_ID, notification)
    }

    private fun baseBuilder(channelId: String): NotificationCompat.Builder {
        // TODO: deep-link to organize result / partial-failure screen when ready.
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_MAIN,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, channelId)
            // Monochrome status-bar icon; colored mipmaps are often invisible on Samsung.
            .setSmallIcon(R.drawable.ic_upload_24)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    companion object {
        const val CHANNEL_PROGRESS_LEGACY = "organize_progress"
        const val CHANNEL_PROGRESS = "organize_progress_v2"
        const val CHANNEL_RESULT = "organize_result"
        const val PROGRESS_NOTIFICATION_ID = 4101
        const val RESULT_NOTIFICATION_ID = 4102
        private const val REQUEST_CODE_OPEN_MAIN = 4100
    }
}
