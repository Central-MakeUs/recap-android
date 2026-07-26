package com.chalkak.recap.feature.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.currentImageAccessLevel
import com.chalkak.recap.core.data.screenshot.permission.imagePermissionRequest
import com.chalkak.recap.core.data.screenshot.permission.imagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.markImagePermissionRequested
import com.chalkak.recap.core.model.ImageAccessLevel

@Composable
fun SettingsRoute(
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var photoAccessLevel by remember {
        mutableStateOf(context.currentImageAccessLevel())
    }

    LifecycleResumeEffect(Unit) {
        photoAccessLevel = context.currentImageAccessLevel()
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        photoAccessLevel = context.currentImageAccessLevel()
    }

    SettingsScreen(
        modifier = modifier,
        uiState = SettingsUiState(photoAccessLevel = photoAccessLevel),
        onAction = { action ->
            when (action) {
                SettingsAction.OpenPhotoAccessPermission -> {
                    openPhotoAccessPermission(
                        context = context,
                        photoAccessLevel = photoAccessLevel,
                        onRequestPermissions = { permissions ->
                            permissionLauncher.launch(permissions)
                        },
                    )
                }

                else -> onAction(action)
            }
        },
    )
}

private fun openPhotoAccessPermission(
    context: Context,
    photoAccessLevel: ImageAccessLevel,
    onRequestPermissions: (Array<String>) -> Unit,
) {
    when (context.imagePermissionRequestDestination()) {
        ImagePermissionRequestDestination.PermissionDialog -> {
            context.markImagePermissionRequested(photoAccessLevel)
            onRequestPermissions(imagePermissionRequest(photoAccessLevel))
        }

        ImagePermissionRequestDestination.ApplicationSettings -> {
            context.openApplicationDetailsSettings()
        }
    }
}

private fun Context.openApplicationDetailsSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ),
    )
}
