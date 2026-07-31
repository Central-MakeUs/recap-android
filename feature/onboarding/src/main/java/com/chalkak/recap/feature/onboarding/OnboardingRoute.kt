package com.chalkak.recap.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.imagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.openApplicationDetailsSettings
import com.chalkak.recap.core.data.screenshot.permission.openPhotoAccessPermission
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.animation.RecapNavDisplay
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteGuideScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

private val NoPendingSampleShareAdvanceRequestIds: StateFlow<Int?> = MutableStateFlow(null)

@Composable
fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    onOpenScreenshotPicker: () -> Unit = {},
    viewModelKey: String? = null,
    pendingSampleShareAdvanceRequestIds: StateFlow<Int?> = NoPendingSampleShareAdvanceRequestIds,
    onSampleShareAdvanceComplete: (Int) -> Unit = {},
    viewModel: OnboardingViewModel = hiltViewModel(key = viewModelKey),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val toastDispatcher = LocalRecapToastDispatcher.current
    val loginFailedMessage = stringResource(
        R.string.onboarding_login_failed_message
    )
    val loginCancelledMessage = stringResource(
        R.string.onboarding_login_cancelled_message
    )
    val pendingSampleShareAdvanceRequestId by
        pendingSampleShareAdvanceRequestIds.collectAsStateWithLifecycle()
    var showPhotoPermissionPopup by rememberSaveable { mutableStateOf(false) }
    var awaitPermissionFromSettings by rememberSaveable { mutableStateOf(false) }

    fun advanceFromPermissionGuide() {
        showPhotoPermissionPopup = false
        awaitPermissionFromSettings = false
        viewModel.onAction(OnboardingAction.SkipPermission)
    }

    fun handlePermissionRequestResult() {
        val accessLevel = viewModel.refreshImagePermission()
        awaitPermissionFromSettings = false
        val currentState = viewModel.uiState.value
        if (currentState.step != OnboardingStep.PermissionGuide) {
            showPhotoPermissionPopup = false
            return
        }
        if (currentState.hasResolvedPermissionStep || accessLevel != ImageAccessLevel.Denied) {
            advanceFromPermissionGuide()
        } else {
            showPhotoPermissionPopup = true
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingEvent.ShowLoginError -> {
                    toastDispatcher.showToast(
                        message = if (event.isCancelled) {
                            loginCancelledMessage
                        } else {
                            loginFailedMessage
                        },
                        type = RecapToastType.Error,
                    )
                }
            }
        }
    }

    LaunchedEffect(pendingSampleShareAdvanceRequestId) {
        val requestId = pendingSampleShareAdvanceRequestId ?: return@LaunchedEffect
        viewModel.onAction(OnboardingAction.CompleteAddToFavorite)
        onSampleShareAdvanceComplete(requestId)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        handlePermissionRequestResult()
    }

    fun requestPhotoAccessPermission() {
        if (
            context.imagePermissionRequestDestination() ==
            ImagePermissionRequestDestination.ApplicationSettings
        ) {
            awaitPermissionFromSettings = true
        }
        openPhotoAccessPermission(
            context = context,
            photoAccessLevel = viewModel.uiState.value.imageAccessLevel,
            onRequestPermissions = { permissions ->
                permissionLauncher.launch(permissions)
            },
        )
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshImagePermission()
        if (awaitPermissionFromSettings) {
            handlePermissionRequestResult()
        }
        onPauseOrDispose { }
    }

    val onboardingBackStack = rememberNavBackStack(OnboardingDestination.Flow)

    Box(modifier = Modifier.fillMaxSize()) {
        RecapNavDisplay(
            backStack = onboardingBackStack,
            onBack = {
                if (onboardingBackStack.size > 1) {
                    onboardingBackStack.removeLastOrNull()
                } else {
                    viewModel.onAction(OnboardingAction.Back)
                }
            },
            transitionSpec = { RecapNavigationMotion.forward() },
            popTransitionSpec = { RecapNavigationMotion.pop() },
            entryProvider = { destination ->
                when (destination) {
                    OnboardingDestination.Flow -> NavEntry(destination) {
                        OnboardingScreen(
                            uiState = uiState,
                            snackbarHostState = snackbarHostState,
                            illustrationSignalFlow = viewModel.illustrationSignals,
                            onAction = { action ->
                                when (action) {
                                    OnboardingAction.LoginWithKakao -> {
                                        viewModel.loginWithKakao(context)
                                    }

                                    OnboardingAction.GrantPermission -> {
                                        if (viewModel.uiState.value.hasResolvedPermissionStep) {
                                            advanceFromPermissionGuide()
                                        } else {
                                            viewModel.onAction(action)
                                            requestPhotoAccessPermission()
                                        }
                                    }

                                    OnboardingAction.OpenPhotoPermissionSettings -> {
                                        viewModel.onAction(action)
                                        context.openApplicationDetailsSettings()
                                    }

                                    OnboardingAction.OpenScreenshotPicker -> {
                                        viewModel.onAction(action)
                                        onOpenScreenshotPicker()
                                    }

                                    OnboardingAction.OpenAddToFavoriteGuide -> {
                                        onboardingBackStack.add(
                                            OnboardingDestination.AddToFavoriteGuide
                                        )
                                    }

                                    OnboardingAction.SkipFirstOrganize -> {
                                        viewModel.onAction(action)
                                    }

                                    OnboardingAction.SkipStartFirstAnalyze -> {
                                        onOnboardingComplete()
                                    }

                                    else -> viewModel.onAction(action)
                                }
                            },
                        )
                    }

                    OnboardingDestination.AddToFavoriteGuide -> NavEntry(destination) {
                        OnboardingAddToFavoriteGuideScreen(
                            onBackClick = { onboardingBackStack.removeLastOrNull() },
                        )
                    }

                    else -> error("Unknown onboarding destination: $destination")
                }
            },
        )

        if (showPhotoPermissionPopup) {
            RecapPopup(
                title = stringResource(R.string.photo_access_permission_title),
                description = stringResource(R.string.photo_access_permission_description),
                confirmButtonText = stringResource(
                    R.string.photo_access_permission_request_permission,
                ),
                cancelButtonText = stringResource(R.string.photo_access_permission_later_button),
                onConfirmClick = {
                    requestPhotoAccessPermission()
                },
                onCancelClick = {
                    advanceFromPermissionGuide()
                },
                onDismissRequest = {
                    advanceFromPermissionGuide()
                },
                confirmButtonColor = RecapBlue300,
                confirmButtonContentColor = White,
            )
        }
    }
}

@Serializable
private sealed interface OnboardingDestination : NavKey {
    @Serializable
    data object Flow : OnboardingDestination

    @Serializable
    data object AddToFavoriteGuide : OnboardingDestination
}
