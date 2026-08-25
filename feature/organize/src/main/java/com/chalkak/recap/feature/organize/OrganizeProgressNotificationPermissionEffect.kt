package com.chalkak.recap.feature.organize

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.chalkak.recap.core.data.notification.NotificationPermissionRequestDestination
import com.chalkak.recap.core.data.notification.areAppNotificationsEnabled
import com.chalkak.recap.core.data.notification.markNotificationPermissionRequested
import com.chalkak.recap.core.data.notification.notificationPermissionRequestDestination
import com.chalkak.recap.core.data.notification.openAppNotificationSettings
import com.chalkak.recap.core.data.notification.shouldShowOrganizeNotificationPermissionPrompt
import com.chalkak.recap.core.design.component.bottomsheet.NotificationPermissionRequestBottomSheet

/**
 * 앱 설치 후 한 번만 organize 알림 권한 프롬프트 바텀시트를 표시한다.
 * 시트 표시 후 사용자의 권한 처리 결과와 관계없이 다시 표시하지 않는다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeProgressNotificationPermissionEffect(
    organizeCompleteNotificationEnabled: Boolean?,
    onOrganizeCompleteNotificationEnabledChange: (Boolean) -> Unit,
    onTryMarkOrganizeNotificationPermissionPromptShown: suspend () -> Boolean,
) {
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    var showNotificationPermissionSheet by remember { mutableStateOf(false) }
    var awaitingSettingsPermissionResult by remember { mutableStateOf(false) }
    val currentTryMarkPromptShown = rememberUpdatedState(
        onTryMarkOrganizeNotificationPermissionPromptShown,
    )

    LaunchedEffect(organizeCompleteNotificationEnabled) {
        val notificationEnabledPreference =
            organizeCompleteNotificationEnabled ?: return@LaunchedEffect
        if (shouldShowOrganizeNotificationPermissionPrompt(notificationEnabledPreference) &&
            currentTryMarkPromptShown.value()
        ) {
            showNotificationPermissionSheet = true
        }
    }

    fun finishAfterPermissionResult() {
        awaitingSettingsPermissionResult = false
        showNotificationPermissionSheet = false
        if (context.areAppNotificationsEnabled()) {
            onOrganizeCompleteNotificationEnabledChange(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        finishAfterPermissionResult()
    }
    val currentAwaitingSettingsPermissionResult =
        rememberUpdatedState(awaitingSettingsPermissionResult)
    val currentFinishAfterPermissionResult =
        rememberUpdatedState { finishAfterPermissionResult() }

    LifecycleResumeEffect(Unit) {
        if (currentAwaitingSettingsPermissionResult.value) {
            currentFinishAfterPermissionResult.value()
        }
        onPauseOrDispose { }
    }

    fun requestNotificationPermissionForOrganize(context: Context) {
        when (context.notificationPermissionRequestDestination()) {
            NotificationPermissionRequestDestination.PermissionDialog -> {
                context.markNotificationPermissionRequested()
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            NotificationPermissionRequestDestination.ApplicationSettings -> {
                awaitingSettingsPermissionResult = true
                context.openAppNotificationSettings()
            }
        }
    }

    if (showNotificationPermissionSheet) {
        NotificationPermissionRequestBottomSheet(
            onDismissRequest = { showNotificationPermissionSheet = false },
            onAllowNotificationClick = {
                if (context.areAppNotificationsEnabled()) {
                    finishAfterPermissionResult()
                } else {
                    requestNotificationPermissionForOrganize(context)
                }
            },
            onLaterClick = { showNotificationPermissionSheet = false },
        )
    }
}
