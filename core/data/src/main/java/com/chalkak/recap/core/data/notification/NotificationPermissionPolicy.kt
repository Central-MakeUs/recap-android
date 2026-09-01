package com.chalkak.recap.core.data.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit

enum class NotificationPermissionRequestDestination {
    PermissionDialog,
    ApplicationSettings,
}

fun Context.notificationPermissionRequestDestination():
    NotificationPermissionRequestDestination {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        hasNotificationPermission()
    ) {
        return NotificationPermissionRequestDestination.ApplicationSettings
    }

    val hasRequestedPermission = hasRequestedNotificationPermission()
    val shouldShowRequestRationale = findActivity()
        ?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ?: false

    return resolveNotificationPermissionRequestDestination(
        hasRequestedPermission = hasRequestedPermission,
        shouldShowRequestRationale = shouldShowRequestRationale,
    )
}

fun Context.markNotificationPermissionRequested() {
    notificationPermissionPreferences()
        .edit {
            putBoolean(NOTIFICATION_PERMISSION_REQUESTED_KEY, true)
        }
}

fun Context.hasRequestedNotificationPermission(): Boolean =
    notificationPermissionPreferences()
        .getBoolean(NOTIFICATION_PERMISSION_REQUESTED_KEY, false)

fun Context.areAppNotificationsEnabled(): Boolean =
    NotificationManagerCompat.from(this).areNotificationsEnabled()

fun Context.openAppNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        },
    )
}

fun resolveNotificationPermissionRequestDestination(
    hasRequestedPermission: Boolean,
    shouldShowRequestRationale: Boolean,
): NotificationPermissionRequestDestination {
    return if (!hasRequestedPermission || shouldShowRequestRationale) {
        NotificationPermissionRequestDestination.PermissionDialog
    } else {
        NotificationPermissionRequestDestination.ApplicationSettings
    }
}

fun shouldShowOrganizeNotificationPermissionPrompt(
    organizeCompleteNotificationEnabled: Boolean,
): Boolean = !organizeCompleteNotificationEnabled

fun Context.hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return areAppNotificationsEnabled()
    }
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

private fun Context.notificationPermissionPreferences() = getSharedPreferences(
    NOTIFICATION_PERMISSION_PREFERENCES,
    Context.MODE_PRIVATE,
)

private const val NOTIFICATION_PERMISSION_PREFERENCES = "notification_permission_request"
private const val NOTIFICATION_PERMISSION_REQUESTED_KEY = "has_requested"
