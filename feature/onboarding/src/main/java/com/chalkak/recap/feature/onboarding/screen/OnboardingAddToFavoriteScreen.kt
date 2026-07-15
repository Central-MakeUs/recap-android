package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheetDefaults
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader

private val AddToFavoriteIllustrationSize = 238.dp
private val AddToFavoriteSpeechBubbleOffsetY = 22.dp
private val AddToFavoriteGuideLinkTopPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingAddToFavoriteScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPhotoAccessPermissionBottomSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
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
            title = stringResource(R.string.onboarding_first_organize_title),
            description = stringResource(R.string.onboarding_first_organize_body_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            contentSpacing = 16.dp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.padding(bottom = AddToFavoriteSpeechBubbleOffsetY),
                ) {
                    Image(
                        painter = painterResource(R.drawable.onboarding_add_to_favorite),
                        contentDescription = stringResource(
                            R.string.onboarding_add_to_favorite_image_content_description,
                        ),
                        modifier = Modifier
                            .size(AddToFavoriteIllustrationSize)
                            .align(Alignment.TopCenter),
                        contentScale = ContentScale.Fit,
                    )
                    RecapSpeechBubble(
                        text = stringResource(R.string.onboarding_add_to_favorite_speech_bubble),
                        arrowDirection = RecapSpeechBubbleArrowDirection.Down,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = AddToFavoriteSpeechBubbleOffsetY),
                    )
                }
                Text(
                    text = stringResource(R.string.onboarding_first_organize_description),
                    modifier = Modifier
                        .padding(top = AddToFavoriteGuideLinkTopPadding)
                        .clickable(role = Role.Button) {
                            onAction(OnboardingAction.OpenAddToFavoriteGuide)
                        },
                    style = RecapBody1,
                    color = RecapGray700,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
        OnboardingBottomActions(
            primaryText = stringResource(R.string.onboarding_first_organize_select_button),
            secondaryText = stringResource(R.string.onboarding_first_organize_later_button),
            onPrimaryClick = {
                if (uiState.imageAccessLevel == ImageAccessLevel.Full) {
                    onAction(OnboardingAction.SelectFirstScreenshots)
                } else {
                    showPhotoAccessPermissionBottomSheet = true
                }
            },
            onSecondaryClick = { onAction(OnboardingAction.SkipFirstOrganize) },
        )
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
                onAction(OnboardingAction.SkipFirstOrganize)
            },
        )
    }
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
