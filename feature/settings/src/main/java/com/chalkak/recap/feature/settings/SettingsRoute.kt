package com.chalkak.recap.feature.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
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

    SettingsScreen(
        modifier = modifier,
        uiState = SettingsUiState(photoAccessLevel = photoAccessLevel),
        onAction = onAction,
    )
}

private fun Context.currentImageAccessLevel(): ImageAccessLevel {
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

private fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
