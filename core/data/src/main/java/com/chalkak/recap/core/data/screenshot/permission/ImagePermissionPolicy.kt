package com.chalkak.recap.core.data.screenshot.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.chalkak.recap.core.model.ImageAccessLevel

enum class ImagePermissionRequestDestination {
    PermissionDialog,
    ApplicationSettings,
}

fun imagePermissionRequest(accessLevel: ImageAccessLevel): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            accessLevel == ImageAccessLevel.Selected -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
        )

        else -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }
}

fun Context.currentImageAccessLevel(): ImageAccessLevel {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            hasPermission(Manifest.permission.READ_MEDIA_IMAGES) -> ImageAccessLevel.Full

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> {
            ImageAccessLevel.Selected
        }

        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> ImageAccessLevel.Full

        else -> ImageAccessLevel.Denied
    }
}

fun Context.imagePermissionRequestDestination(): ImagePermissionRequestDestination {
    val accessLevel = currentImageAccessLevel()
    val hasRequestedPermission = imagePermissionPreferences()
        .getBoolean(accessLevel.requestHistoryKey(), false)
    val shouldShowRequestRationale = findActivity()
        ?.shouldShowRequestPermissionRationale(fullImageAccessPermission())
        ?: false

    return resolveImagePermissionRequestDestination(
        accessLevel = accessLevel,
        hasRequestedPermission = hasRequestedPermission,
        shouldShowRequestRationale = shouldShowRequestRationale,
    )
}

fun Context.markImagePermissionRequested(accessLevel: ImageAccessLevel) {
    imagePermissionPreferences()
        .edit()
        .putBoolean(accessLevel.requestHistoryKey(), true)
        .apply()
}

internal fun resolveImagePermissionRequestDestination(
    accessLevel: ImageAccessLevel,
    hasRequestedPermission: Boolean,
    shouldShowRequestRationale: Boolean,
): ImagePermissionRequestDestination {
    return when {
        accessLevel == ImageAccessLevel.Full -> {
            ImagePermissionRequestDestination.ApplicationSettings
        }

        !hasRequestedPermission || shouldShowRequestRationale -> {
            ImagePermissionRequestDestination.PermissionDialog
        }

        else -> ImagePermissionRequestDestination.ApplicationSettings
    }
}

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_GRANTED
}

private fun fullImageAccessPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
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

private fun Context.imagePermissionPreferences() = getSharedPreferences(
    IMAGE_PERMISSION_PREFERENCES,
    Context.MODE_PRIVATE,
)

private fun ImageAccessLevel.requestHistoryKey(): String {
    return if (this == ImageAccessLevel.Selected) {
        FULL_IMAGE_PERMISSION_FROM_SELECTED_REQUESTED_KEY
    } else {
        IMAGE_PERMISSION_REQUESTED_KEY
    }
}

private const val IMAGE_PERMISSION_PREFERENCES = "image_permission_request"
private const val IMAGE_PERMISSION_REQUESTED_KEY = "has_requested"
private const val FULL_IMAGE_PERMISSION_FROM_SELECTED_REQUESTED_KEY =
    "has_requested_full_from_selected"
