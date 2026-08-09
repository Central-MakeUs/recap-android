package com.chalkak.recap.feature.organize

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.currentImageAccessLevel
import com.chalkak.recap.core.data.screenshot.permission.imagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.openPhotoAccessPermission
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottomsheet.AiDataTransferConsentBottomSheet
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeRoute(
    onNavigateBack: () -> Unit,
    onOrganizeComplete: (List<ScreenshotUploadCandidate>) -> Unit,
    sharedImages: List<LocalImage>? = null,
    shareSessionId: String? = null,
    clearSelectionOnComplete: Boolean = true,
    viewModel: OrganizeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val startAtConfirmation = sharedImages != null && shareSessionId != null
    var imageAccessLevel by remember {
        mutableStateOf(context.currentImageAccessLevel())
    }
    var showPhotoPermissionPopup by rememberSaveable { mutableStateOf(false) }
    // 앱 설정에서 돌아온 뒤에만 피커 오픈을 시도한다.
    // 시스템 권한 다이얼로그는 pause/resume을 유발하므로 런처 콜백으로만 처리한다.
    var awaitPermissionFromSettings by rememberSaveable { mutableStateOf(false) }
    var openPickerAfterPermission by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var destination by rememberSaveable {
        mutableStateOf(
            if (startAtConfirmation) {
                OrganizeDestination.Confirmation
            } else {
                OrganizeDestination.Selection
            },
        )
    }
    // Destination과 분리: Confirmation fade-in과 시트 hide를 동시에 진행한다.
    var showScreenshotPicker by remember {
        mutableStateOf(destination == OrganizeDestination.Selection)
    }
    // hide() 완료 시 ModalBottomSheet가 onDismissRequest를 호출하므로 확인 이동/animated exit 중에는 무시한다.
    var suppressPickerDismiss by remember { mutableStateOf(false) }
    // Exiting 상태가 복원되면 취소된 hide coroutine을 재실행하지 않고 즉시 종료한다.
    var isAnimatedExitRunning by remember { mutableStateOf(false) }
    var showDiscardSelectionConfirm by remember { mutableStateOf(false) }
    // 시드 직후 uiState 반영 전 empty로 오판해 닫히지 않도록, 한 번이라도 선택이 있은 뒤에만 종료한다.
    var confirmationHadSelection by remember { mutableStateOf(false) }
    val sheetState = rememberScreenshotPickerSheetState(
        selectionCount = uiState.selectionCount,
        onAttemptDismissWithSelection = { showDiscardSelectionConfirm = true },
        allowHideWithoutConfirm = { suppressPickerDismiss },
    )

    fun refreshScreenshotList() {
        when {
            sharedImages == null -> viewModel.refreshScreenshots()
            destination == OrganizeDestination.Selection -> {
                viewModel.refreshScreenshotsMergingSelected()
            }
        }
    }

    fun refreshPhotoAccess() {
        imageAccessLevel = context.currentImageAccessLevel()
        refreshScreenshotList()
    }

    fun navigateBackToPicker() {
        destination = OrganizeDestination.Selection
        suppressPickerDismiss = false
        showScreenshotPicker = true
    }

    fun finishPickerPermissionRequest() {
        imageAccessLevel = context.currentImageAccessLevel()
        showPhotoPermissionPopup = false
        awaitPermissionFromSettings = false
        val shouldOpenPicker = openPickerAfterPermission
        openPickerAfterPermission = false
        if (shouldOpenPicker) {
            if (imageAccessLevel != ImageAccessLevel.Denied) {
                navigateBackToPicker()
            }
        } else {
            refreshScreenshotList()
        }
    }

    fun attemptNavigateBackToPicker() {
        imageAccessLevel = context.currentImageAccessLevel()
        if (imageAccessLevel == ImageAccessLevel.Denied) {
            openPickerAfterPermission = true
            showPhotoPermissionPopup = true
        } else {
            navigateBackToPicker()
        }
    }

    LifecycleResumeEffect(Unit) {
        if (awaitPermissionFromSettings) {
            finishPickerPermissionRequest()
        } else {
            refreshPhotoAccess()
        }
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        if (openPickerAfterPermission) {
            finishPickerPermissionRequest()
        } else {
            refreshPhotoAccess()
        }
    }

    LaunchedEffect(shareSessionId, sharedImages) {
        if (sharedImages != null && shareSessionId != null) {
            viewModel.seedSharedImages(
                sessionId = shareSessionId,
                images = sharedImages,
            )
        } else {
            viewModel.refreshScreenshots()
        }
    }

    fun completeOrganize(candidates: List<ScreenshotUploadCandidate>) {
        onOrganizeComplete(candidates)
        if (clearSelectionOnComplete) {
            viewModel.onAction(OrganizeAction.ClearSelection)
        }
    }

    LaunchedEffect(destination) {
        if (destination == OrganizeDestination.Confirmation) {
            viewModel.onConfirmationEntered()
        } else {
            viewModel.onConfirmationExited()
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onConfirmationExited()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OrganizeEvent.ProceedToOrganize -> {
                    completeOrganize(event.candidates)
                }
            }
        }
    }

    LaunchedEffect(destination, shareSessionId) {
        if (
            shareSessionId != null &&
            destination == OrganizeDestination.Selection
        ) {
            viewModel.refreshScreenshotsMergingSelected()
        }
    }

    fun exitOrganizeImmediately() {
        viewModel.onAction(OrganizeAction.ClearSelection)
        onNavigateBack()
    }

    fun dismissScreenshotPickerAndExit() {
        if (suppressPickerDismiss || !showScreenshotPicker) return
        suppressPickerDismiss = true
        isAnimatedExitRunning = true
        destination = OrganizeDestination.Exiting
        coroutineScope.launch {
            sheetState.hide()
            showScreenshotPicker = false
            exitOrganizeImmediately()
        }
    }

    fun navigateToConfirmation() {
        if (!uiState.canProceed) return
        if (destination != OrganizeDestination.Selection || suppressPickerDismiss) return
        suppressPickerDismiss = true
        destination = OrganizeDestination.Confirmation
        coroutineScope.launch {
            sheetState.hide()
            showScreenshotPicker = false
        }
    }

    LaunchedEffect(destination) {
        if (destination == OrganizeDestination.Exiting && !isAnimatedExitRunning) {
            exitOrganizeImmediately()
        }
    }

    LaunchedEffect(uiState.selectedUris, destination) {
        if (destination != OrganizeDestination.Confirmation) {
            confirmationHadSelection = false
            return@LaunchedEffect
        }
        if (uiState.selectedUris.isNotEmpty()) {
            confirmationHadSelection = true
        } else if (confirmationHadSelection) {
            exitOrganizeImmediately()
        }
    }

    BackHandler(enabled = destination == OrganizeDestination.Confirmation) {
        exitOrganizeImmediately()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = destination == OrganizeDestination.Confirmation,
            enter = fadeIn(animationSpec = tween(OrganizeTransitionTokens.ConfirmationFadeMs)),
            exit = fadeOut(animationSpec = tween(OrganizeTransitionTokens.ConfirmationFadeMs)),
        ) {
            ScreenshotConfirmationScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onBackClick = ::exitOrganizeImmediately,
                onAddMoreClick = ::attemptNavigateBackToPicker,
                onStartOrganizingClick = {
                    viewModel.onAction(OrganizeAction.StartOrganizing)
                },
            )
        }

        if (showScreenshotPicker) {
            ScreenshotPicker(
                uiState = uiState,
                imageAccessLevel = imageAccessLevel,
                onAction = viewModel::onAction,
                onDismissRequest = {
                    // Material이 이미 hide 애니메이션을 끝낸 뒤 호출된다.
                    if (!suppressPickerDismiss) {
                        exitOrganizeImmediately()
                    }
                },
                onCloseClick = ::dismissScreenshotPickerAndExit,
                onConfirmClick = ::navigateToConfirmation,
                onRequestFullPhotoAccess = {
                    openPhotoAccessPermission(
                        context = context,
                        photoAccessLevel = imageAccessLevel,
                        onRequestPermissions = { permissions ->
                            permissionLauncher.launch(permissions)
                        },
                    )
                },
                sheetState = sheetState,
                discardSelectionConfirmVisible = showDiscardSelectionConfirm,
                onDiscardSelectionConfirmVisibleChange = { showDiscardSelectionConfirm = it },
            )
        }

        if (uiState.showAiDataTransferConsentSheet) {
            AiDataTransferConsentBottomSheet(
                onDismissRequest = {
                    viewModel.onAction(OrganizeAction.DismissAiDataTransferConsent)
                },
                onAgreeClick = {
                    if (!uiState.isConsentSubmitting) {
                        viewModel.onAction(OrganizeAction.AgreeAiDataTransferConsent)
                    }
                },
                onCancelClick = {
                    viewModel.onAction(OrganizeAction.DismissAiDataTransferConsent)
                },
                onPrivacyPolicyClick = {},
            )
        }

        if (showPhotoPermissionPopup) {
            RecapPopup(
                title = stringResource(R.string.photo_access_permission_title),
                description = stringResource(R.string.photo_access_permission_description),
                confirmButtonText = stringResource(
                    R.string.photo_access_permission_request_permission,
                ),
                cancelButtonText = stringResource(R.string.photo_access_permission_later_button),
                onConfirmClick = {
                    if (
                        context.imagePermissionRequestDestination() ==
                        ImagePermissionRequestDestination.ApplicationSettings
                    ) {
                        awaitPermissionFromSettings = true
                    }
                    openPhotoAccessPermission(
                        context = context,
                        photoAccessLevel = imageAccessLevel,
                        onRequestPermissions = { permissions ->
                            permissionLauncher.launch(permissions)
                        },
                    )
                },
                onCancelClick = {
                    showPhotoPermissionPopup = false
                    openPickerAfterPermission = false
                    awaitPermissionFromSettings = false
                },
                onDismissRequest = {
                    showPhotoPermissionPopup = false
                    openPickerAfterPermission = false
                    awaitPermissionFromSettings = false
                },
                confirmButtonColor = RecapBlue300,
                confirmButtonContentColor = White,
            )
        }
    }
}

private enum class OrganizeDestination {
    Selection,
    Confirmation,
    Exiting,
}

private object OrganizeTransitionTokens {
    const val ConfirmationFadeMs = 350
}
