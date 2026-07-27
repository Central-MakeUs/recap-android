package com.chalkak.recap.feature.settings.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit

internal enum class NotificationPermissionRequestDestination {
    PermissionDialog,
    ApplicationSettings,
}

internal fun Context.notificationPermissionRequestDestination():
    NotificationPermissionRequestDestination {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        hasNotificationPermission()
    ) {
        return NotificationPermissionRequestDestination.ApplicationSettings
    }

    val hasRequestedPermission = notificationPermissionPreferences()
        .getBoolean(NOTIFICATION_PERMISSION_REQUESTED_KEY, false)
    val shouldShowRequestRationale = findActivity()
        ?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ?: false

    return resolveNotificationPermissionRequestDestination(
        hasRequestedPermission = hasRequestedPermission,
        shouldShowRequestRationale = shouldShowRequestRationale,
    )
}

internal fun Context.markNotificationPermissionRequested() {
    notificationPermissionPreferences()
        .edit {
            putBoolean(NOTIFICATION_PERMISSION_REQUESTED_KEY, true)
        }
}

internal fun resolveNotificationPermissionRequestDestination(
    hasRequestedPermission: Boolean,
    shouldShowRequestRationale: Boolean,
): NotificationPermissionRequestDestination {
    return if (!hasRequestedPermission || shouldShowRequestRationale) {
        NotificationPermissionRequestDestination.PermissionDialog
    } else {
        NotificationPermissionRequestDestination.ApplicationSettings
    }
}

private fun Context.hasNotificationPermission(): Boolean {
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
