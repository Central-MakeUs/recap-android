package com.chalkak.recap.feature.settings.notification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NotificationSettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val savedUiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deviceNotificationsEnabled by remember {
        mutableStateOf(context.areAppNotificationsEnabled())
    }
    var enableOrganizeAfterPermission by rememberSaveable {
        mutableStateOf(false)
    }

    fun applyNotificationPermissionResult() {
        val notificationsEnabled = context.areAppNotificationsEnabled()
        deviceNotificationsEnabled = notificationsEnabled
        if (notificationsEnabled && enableOrganizeAfterPermission) {
            viewModel.onAction(
                NotificationSettingsAction.OrganizeCompleteEnabledChanged(true)
            )
            enableOrganizeAfterPermission = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        applyNotificationPermissionResult()
    }

    LifecycleResumeEffect(Unit) {
        applyNotificationPermissionResult()
        onPauseOrDispose { }
    }

    NotificationSettingsScreen(
        uiState = savedUiState.copy(
            deviceNotificationsEnabled = deviceNotificationsEnabled,
        ),
        onAction = { action ->
            when (action) {
                NotificationSettingsAction.NavigateBack -> onNavigateBack()
                NotificationSettingsAction.RequestDeviceNotificationPermission -> {
                    requestNotificationPermission(
                        context = context,
                        onRequestPermission = {
                            context.markNotificationPermissionRequested()
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                    )
                }

                is NotificationSettingsAction.OrganizeCompleteEnabledChanged -> {
                    if (action.enabled && !deviceNotificationsEnabled) {
                        enableOrganizeAfterPermission = true
                        requestNotificationPermission(
                            context = context,
                            onRequestPermission = {
                                context.markNotificationPermissionRequested()
                                permissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            },
                        )
                    } else {
                        viewModel.onAction(action)
                    }
                }
            }
        },
        modifier = modifier,
    )
}

private fun requestNotificationPermission(
    context: Context,
    onRequestPermission: () -> Unit,
) {
    when (context.notificationPermissionRequestDestination()) {
        NotificationPermissionRequestDestination.PermissionDialog -> {
            onRequestPermission()
        }

        NotificationPermissionRequestDestination.ApplicationSettings -> {
            context.openAppNotificationSettings()
        }
    }
}

private fun Context.areAppNotificationsEnabled(): Boolean =
    NotificationManagerCompat.from(this).areNotificationsEnabled()

private fun Context.openAppNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        },
    )
}
