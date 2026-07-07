package com.chalkak.recap.feature.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.R
import com.chalkak.recap.core.model.ImageAccessLevel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteGuideScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    viewModelKey: String? = null,
    viewModel: OnboardingViewModel = hiltViewModel(key = viewModelKey),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val permissionRequiredMessage = stringResource(
        R.string.onboarding_full_access_permission_snackbar
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        val accessLevel = viewModel.refreshImagePermissionAndMoveToFirstCleanup()
        if (accessLevel != ImageAccessLevel.Full) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(permissionRequiredMessage)
            }
        }
    }
    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshImagePermissionAndMoveToFirstCleanup()
    }
    val screenshotPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            onOnboardingComplete()
        }
    }
    val onboardingBackStack = rememberNavBackStack(OnboardingDestination.Flow)

    NavDisplay(
        backStack = onboardingBackStack,
        onBack = {
            if (onboardingBackStack.size > 1) {
                onboardingBackStack.removeLastOrNull()
            } else {
                viewModel.onAction(OnboardingAction.Back)
            }
        },
        entryProvider = { destination ->
            when (destination) {
                OnboardingDestination.Flow -> NavEntry(destination) {
                    OnboardingScreen(
                        uiState = uiState,
                        snackbarHostState = snackbarHostState,
                        illustrationSignalFlow = viewModel.illustrationSignals,
                        onAction = { action ->
                            when (action) {
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
                                    screenshotPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }

                                OnboardingAction.OpenAddToFavoriteGuide -> {
                                    onboardingBackStack.add(
                                        OnboardingDestination.AddToFavoriteGuide
                                    )
                                }

                                OnboardingAction.SkipFirstCleanup -> {
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
}

@Serializable
private sealed interface OnboardingDestination : NavKey {
    @Serializable
    data object Flow : OnboardingDestination

    @Serializable
    data object AddToFavoriteGuide : OnboardingDestination
}
