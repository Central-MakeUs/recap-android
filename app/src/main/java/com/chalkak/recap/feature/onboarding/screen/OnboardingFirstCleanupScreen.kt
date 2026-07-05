package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.chalkak.recap.feature.onboarding.component.OnboardingTopBar

private val UploadGuideBlue = Color(0xFF5C74FF)
private val UploadGuideGray900 = Color(0xFF0B111D)
private val UploadGuideGray700 = Color(0xFF222B3C)
private val UploadGuideGray500 = Color(0xFF4D586C)
private val UploadGuideGray100 = Color(0xFFE2E6ED)
private val UploadGuideIconBackground = Color(0xFFF0F1FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFirstCleanupScreen(
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
        ) {
            OnboardingTopBar(
                onBack = { onAction(OnboardingAction.Back) },
                skipText = stringResource(R.string.onboarding_first_cleanup_skip_top_button),
                onSkip = { onAction(OnboardingAction.SkipFirstCleanup) },
            )
            Text(
                text = stringResource(R.string.onboarding_first_cleanup_upload_title),
                modifier = Modifier.padding(top = 28.dp),
                style = MaterialTheme.typography.displaySmall,
                color = UploadGuideGray900,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.onboarding_first_cleanup_upload_description),
                modifier = Modifier.padding(top = 20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = UploadGuideGray500,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier = Modifier.padding(top = 30.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UploadMethodCard(
                    icon = Icons.Outlined.Image,
                    title = stringResource(R.string.onboarding_first_cleanup_gallery_title),
                    description = stringResource(R.string.onboarding_first_cleanup_gallery_description),
                )
                UploadMethodCard(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.onboarding_first_cleanup_share_title),
                    description = stringResource(R.string.onboarding_first_cleanup_share_description),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            PageIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp),
            )
            RecapButton(
                text = stringResource(R.string.onboarding_first_cleanup_next_button),
                onClick = {
                    if (uiState.imageAccessLevel == ImageAccessLevel.Full) {
                        onAction(OnboardingAction.SelectFirstScreenshots)
                    } else {
                        showPhotoAccessPermissionBottomSheet = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 19.dp),
                shadowElevation = 12.dp,
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
private fun UploadMethodCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, UploadGuideGray100),
    ) {
        Row(
            modifier = Modifier.padding(
                start = 18.dp,
                top = 17.dp,
                end = 18.dp,
                bottom = 17.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(UploadGuideIconBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UploadGuideBlue,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = UploadGuideGray700,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = UploadGuideGray500,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(UploadGuideGray100, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 6.dp)
                .background(UploadGuideBlue, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(UploadGuideGray100, CircleShape),
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
