package com.chalkak.recap.feature.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
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
import kotlinx.coroutines.launch

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
        val accessLevel = viewModel.refreshImagePermissionAndMove()
        if (accessLevel != ImageAccessLevel.Full) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(permissionRequiredMessage)
            }
        }
    }
    val appSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshImagePermissionAndMove()
    }

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

                OnboardingAction.StartCleanup -> {
                    viewModel.onAction(action)
                    onOnboardingComplete()
                }

                OnboardingAction.SkipFirstCleanup -> {
                    onOnboardingComplete()
                }

                else -> viewModel.onAction(action)
            }
        },
    )
}
