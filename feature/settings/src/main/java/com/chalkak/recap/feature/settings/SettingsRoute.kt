package com.chalkak.recap.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.chalkak.recap.core.data.screenshot.permission.currentImageAccessLevel
import com.chalkak.recap.core.data.screenshot.permission.openPhotoAccessPermission

@Composable
fun SettingsRoute(
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var photoAccessLevel by remember {
        mutableStateOf(context.currentImageAccessLevel())
    }

    LaunchedEffect(viewModel) {
        viewModel.prefetchDataManagement()
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
