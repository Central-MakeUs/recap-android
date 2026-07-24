package com.chalkak.recap.feature.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.model.ImageAccessLevel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.core.design.animation.RecapNavDisplay
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteGuideScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun OnboardingRoute(
    onOnboardingComplete: (openOrganize: Boolean) -> Unit,
    viewModelKey: String? = null,
    viewModel: OnboardingViewModel = hiltViewModel(key = viewModelKey),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val toastDispatcher = LocalRecapToastDispatcher.current
    val coroutineScope = rememberCoroutineScope()
    val permissionRequiredMessage = stringResource(
        R.string.onboarding_full_access_permission_snackbar
    )
    val loginFailedMessage = stringResource(
        R.string.onboarding_login_failed_message
    )
    val loginCancelledMessage = stringResource(
        R.string.onboarding_login_cancelled_message
    )
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
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val accessLevel = viewModel.refreshImagePermissionAndMoveToFirstOrganize()
        if (accessLevel != ImageAccessLevel.Full) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(permissionRequiredMessage)
            }
        }
    }
    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshImagePermissionAndMoveToFirstOrganize()
    }
    val onboardingBackStack = rememberNavBackStack(OnboardingDestination.Flow)

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
                                    permissionLauncher.launch(viewModel.imagePermissionRequest())
                                    viewModel.onAction(action)
                                }

                                OnboardingAction.OpenPhotoPermissionSettings -> {
                                    viewModel.onAction(action)
                                    appSettingsLauncher.launch(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null),
                                        )
                                    )
                                }

                                OnboardingAction.OpenScreenshotPicker -> {
                                    viewModel.onAction(action)
                                    onOnboardingComplete(true)
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
                                    onOnboardingComplete(false)
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
}

@Serializable
private sealed interface OnboardingDestination : NavKey {
    @Serializable
    data object Flow : OnboardingDestination

    @Serializable
    data object AddToFavoriteGuide : OnboardingDestination
}
