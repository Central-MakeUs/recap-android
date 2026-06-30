package com.chalkak.recap.feature.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    viewModelKey: String? = null,
    viewModel: OnboardingViewModel = hiltViewModel(key = viewModelKey),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.onAction(OnboardingAction.RefreshImagePermission)
    }

    OnboardingScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                OnboardingAction.GrantPermission -> {
                    permissionLauncher.launch(viewModel.imagePermissionRequest())
                    viewModel.onAction(action)
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
