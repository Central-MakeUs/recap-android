package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheetDefaults
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingAddToFavoriteScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPhotoAccessPermissionBottomSheet by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecapLogo(
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 24.dp)
                    .width(58.dp)
                    .aspectRatio(RecapLogoAspectRatio),
            )
            StepHeader(
                title = stringResource(R.string.onboarding_first_cleanup_title),
                description = stringResource(R.string.onboarding_first_cleanup_body_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentSpacing = 24.dp,
                descriptionFontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                AddToFavoritePlaceholder()
            }
            Text(
                text = stringResource(R.string.onboarding_first_cleanup_description),
                modifier = Modifier
                    .padding(bottom = 48.dp)
                    .clickable(role = Role.Button) {
                        onAction(OnboardingAction.OpenAddToFavoriteGuide)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
            )
            OnboardingBottomActions(
                primaryText = stringResource(R.string.onboarding_first_cleanup_select_button),
                secondaryText = stringResource(R.string.onboarding_first_cleanup_later_button),
                onPrimaryClick = {
                    if (uiState.imageAccessLevel == ImageAccessLevel.Full) {
                        onAction(OnboardingAction.SelectFirstScreenshots)
                    } else {
                        showPhotoAccessPermissionBottomSheet = true
                    }
                },
                onSecondaryClick = { onAction(OnboardingAction.SkipFirstCleanup) },
            )
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

@Composable
private fun AddToFavoritePlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 192.dp, height = 192.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
            ),
    )
}

@OnboardingScreenPreview
@Composable
private fun OnboardingAddToFavoriteScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingAddToFavoriteScreen(
            uiState = OnboardingUiState(),
            onAction = {},
        )
    }
}
