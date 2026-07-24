package com.chalkak.recap.feature.organize

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.model.LocalImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizeRoute(
    onNavigateBack: () -> Unit,
    onOrganizeComplete: (List<LocalImage>) -> Unit,
    sharedImages: List<LocalImage>? = null,
    shareSessionId: String? = null,
    viewModel: OrganizeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val startAtConfirmation = sharedImages != null && shareSessionId != null

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

    LaunchedEffect(destination, shareSessionId) {
        if (
            shareSessionId != null &&
            destination == OrganizeDestination.Selection
        ) {
            viewModel.refreshScreenshotsMergingSelected()
        }
    }

    fun navigateBackToPicker() {
        destination = OrganizeDestination.Selection
        suppressPickerDismiss = false
        showScreenshotPicker = true
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
                onAddMoreClick = ::navigateBackToPicker,
                onStartOrganizingClick = {
                    if (uiState.canProceed) {
                        val selectedScreenshots = uiState.availableScreenshots
                            .filter { screenshot -> screenshot.uri in uiState.selectedUris }
                            .sortedBy { screenshot ->
                                uiState.selectedUris.indexOf(screenshot.uri)
                            }
                        viewModel.onAction(OrganizeAction.ClearSelection)
                        onOrganizeComplete(selectedScreenshots)
                    }
                },
            )
        }

        if (showScreenshotPicker) {
            ScreenshotPicker(
                uiState = uiState,
                onAction = viewModel::onAction,
                onDismissRequest = {
                    // Material이 이미 hide 애니메이션을 끝낸 뒤 호출된다.
                    if (!suppressPickerDismiss) {
                        exitOrganizeImmediately()
                    }
                },
                onCloseClick = ::dismissScreenshotPickerAndExit,
                onConfirmClick = ::navigateToConfirmation,
                sheetState = sheetState,
                discardSelectionConfirmVisible = showDiscardSelectionConfirm,
                onDiscardSelectionConfirmVisibleChange = { showDiscardSelectionConfirm = it },
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
    const val ConfirmationFadeMs = 280
}
