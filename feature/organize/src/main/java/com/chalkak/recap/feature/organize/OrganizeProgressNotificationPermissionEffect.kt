package com.chalkak.recap.feature.organize

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.chalkak.recap.core.data.notification.NotificationPermissionRequestDestination
import com.chalkak.recap.core.data.notification.areAppNotificationsEnabled
import com.chalkak.recap.core.data.notification.hasRequestedNotificationPermission
import com.chalkak.recap.core.data.notification.markNotificationPermissionRequested
import com.chalkak.recap.core.data.notification.notificationPermissionRequestDestination
import com.chalkak.recap.core.data.notification.openAppNotificationSettings
import com.chalkak.recap.core.data.notification.shouldShowOrganizeNotificationPermissionPrompt
import com.chalkak.recap.core.design.component.bottomsheet.NotificationPermissionRequestBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * 분석 상태 화면 최초 진입 시 기존 organize 알림 권한 프롬프트 조건으로 바텀시트를 1회 표시한다.
 * 한 번 표시된 시트는 분석 상태가 완료로 전환되어도 사용자가 처리할 때까지 유지한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeProgressNotificationPermissionEffect(
    organizeCompleteNotificationEnabled: Boolean?,
    onOrganizeCompleteNotificationEnabledChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var showNotificationPermissionSheet by rememberSaveable { mutableStateOf(false) }
    var hasEvaluatedPrompt by rememberSaveable { mutableStateOf(false) }
    var awaitingSettingsPermissionResult by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(organizeCompleteNotificationEnabled) {
        if (hasEvaluatedPrompt) return@LaunchedEffect
        val notificationEnabledPreference =
            organizeCompleteNotificationEnabled ?: return@LaunchedEffect
        hasEvaluatedPrompt = true
        val shouldShowPrompt = shouldShowOrganizeNotificationPermissionPrompt(
            hasRequestedPermission = context.hasRequestedNotificationPermission(),
            notificationsEnabled = context.areAppNotificationsEnabled(),
            organizeCompleteNotificationEnabled = notificationEnabledPreference,
        )
        if (shouldShowPrompt) {
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
