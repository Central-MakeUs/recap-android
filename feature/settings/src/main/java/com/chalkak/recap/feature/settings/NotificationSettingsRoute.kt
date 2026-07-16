package com.chalkak.recap.feature.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.feature.settings.screen.NotificationSettingsScreen

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

    LifecycleResumeEffect(Unit) {
        deviceNotificationsEnabled = context.areAppNotificationsEnabled()
        onPauseOrDispose { }
    }

    NotificationSettingsScreen(
        uiState = savedUiState.copy(
            deviceNotificationsEnabled = deviceNotificationsEnabled,
        ),
        onAction = { action ->
            when (action) {
                NotificationSettingsAction.NavigateBack -> onNavigateBack()
                NotificationSettingsAction.OpenDeviceNotificationSettings -> {
                    context.openAppNotificationSettings()
                }
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
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
