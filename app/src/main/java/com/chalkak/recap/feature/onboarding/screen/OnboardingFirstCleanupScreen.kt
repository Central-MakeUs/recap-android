package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheetDefaults
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.FirstCleanupIllustration
import com.chalkak.recap.feature.onboarding.component.StepHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFirstCleanupScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPhotoAccessPermissionBottomSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        StepHeader(
            title = stringResource(R.string.onboarding_first_cleanup_title),
            titleStyle = MaterialTheme.typography.displayLarge,
        )
        FirstCleanupIllustration(
            modifier = Modifier
                .padding(top = 52.dp)
                .fillMaxWidth()
                .height(170.dp),
        )
        StepHeader(
            title = stringResource(R.string.onboarding_first_cleanup_body_title),
            description = stringResource(R.string.onboarding_first_cleanup_description),
            modifier = Modifier.padding(top = 42.dp),
            titleStyle = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecapButton(
                text = stringResource(R.string.onboarding_first_cleanup_select_button),
                onClick = {
                    if (uiState.imageAccessLevel == ImageAccessLevel.Full) {
                        onAction(OnboardingAction.SelectFirstScreenshots)
                    } else {
                        showPhotoAccessPermissionBottomSheet = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = { onAction(OnboardingAction.SkipFirstCleanup) },
            ) {
                Text(
                    text = stringResource(R.string.onboarding_first_cleanup_later_button),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (showPhotoAccessPermissionBottomSheet) {
        val isSelectedPhotoAccess = uiState.imageAccessLevel == ImageAccessLevel.Selected

        RecapActionBottomSheet(
            icon = Icons.Outlined.Image,
            iconContentDescription = stringResource(
                R.string.photo_access_permission_icon_content_description
            ),
            iconStyle = RecapActionBottomSheetDefaults.primaryIconStyle(),
            title = if (isSelectedPhotoAccess) {
                stringResource(R.string.onboarding_full_access_permission_title)
            } else {
                stringResource(R.string.photo_access_permission_title)
            },
            description = if (isSelectedPhotoAccess) {
                stringResource(R.string.onboarding_full_access_permission_description)
            } else {
                stringResource(R.string.photo_access_permission_description)
            },
            topNotice = if (isSelectedPhotoAccess) {
                stringResource(R.string.onboarding_full_access_permission_notice)
            } else {
                stringResource(R.string.photo_access_permission_notice)
            },
            primaryButtonText = if (isSelectedPhotoAccess) {
                stringResource(R.string.onboarding_full_access_permission_settings_button)
            } else {
                stringResource(R.string.photo_access_permission_request_permission)
            },
            secondaryButtonText = stringResource(R.string.photo_access_permission_later_button),
            onDismissRequest = { showPhotoAccessPermissionBottomSheet = false },
            onPrimaryClick = {
                showPhotoAccessPermissionBottomSheet = false
                if (isSelectedPhotoAccess) {
                    onAction(OnboardingAction.OpenPhotoPermissionSettings)
                } else {
                    onAction(OnboardingAction.GrantPermission)
                }
            },
            onSecondaryClick = {
                showPhotoAccessPermissionBottomSheet = false
                onAction(OnboardingAction.SkipFirstCleanup)
            },
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingFirstCleanupScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingFirstCleanupScreen(
            uiState = OnboardingUiState(),
            onAction = {},
        )
    }
}
