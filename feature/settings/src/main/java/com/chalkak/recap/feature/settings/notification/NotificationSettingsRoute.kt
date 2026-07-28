package com.chalkak.recap.feature.settings.notification

import android.Manifest
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.data.notification.NotificationPermissionRequestDestination
import com.chalkak.recap.core.data.notification.areAppNotificationsEnabled
import com.chalkak.recap.core.data.notification.markNotificationPermissionRequested
import com.chalkak.recap.core.data.notification.notificationPermissionRequestDestination
import com.chalkak.recap.core.data.notification.openAppNotificationSettings

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
                NotificationSettingsAction.OrganizeCompleteNotificationEnabledChanged(true)
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

                is NotificationSettingsAction.OrganizeCompleteNotificationEnabledChanged -> {
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
    context: android.content.Context,
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
