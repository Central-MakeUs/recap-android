package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader
import com.chalkak.recap.feature.onboarding.launchOnboardingAddToFavoriteShareSheet

private val AddToFavoriteIllustrationSize = 238.dp
private val AddToFavoriteSpeechBubbleOverlap = 4.dp
private val AddToFavoriteGuideLinkTopPadding = 18.dp
private val AddToFavoriteGuideLinkBottomGap = 8.dp

@Composable
fun OnboardingAddToFavoriteScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

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
        AddToFavoriteIllustrationContent(
            onGuideClick = {
                onAction(OnboardingAction.OpenAddToFavoriteGuide)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        OnboardingBottomActions(
            primaryText = stringResource(R.string.onboarding_first_organize_select_button),
            secondaryText = stringResource(R.string.onboarding_first_organize_later_button),
            onPrimaryClick = { context.launchOnboardingAddToFavoriteShareSheet() },
            onSecondaryClick = { onAction(OnboardingAction.SkipFirstOrganize) },
        )
    }
}

@Composable
private fun AddToFavoriteIllustrationContent(
    onGuideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = AddToFavoriteGuideLinkBottomGap),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.onboarding_add_to_favorite),
            contentDescription = stringResource(
                R.string.onboarding_add_to_favorite_image_content_description,
            ),
            modifier = Modifier
                .weight(weight = 1f, fill = false)
                .sizeIn(
                    maxWidth = AddToFavoriteIllustrationSize,
                    maxHeight = AddToFavoriteIllustrationSize,
                )
                .aspectRatio(1f),
            contentScale = ContentScale.Fit,
        )
        RecapSpeechBubble(
            text = stringResource(R.string.onboarding_add_to_favorite_speech_bubble),
            arrowDirection = RecapSpeechBubbleArrowDirection.Down,
            modifier = Modifier.offset(y = -AddToFavoriteSpeechBubbleOverlap),
        )
        Text(
            text = stringResource(R.string.onboarding_first_organize_description),
            modifier = Modifier
                .padding(
                    top = AddToFavoriteGuideLinkTopPadding -
                            AddToFavoriteSpeechBubbleOverlap,
                )
                .clickable(role = Role.Button, onClick = onGuideClick),
            style = RecapBody1,
            color = RecapGray700,
            textDecoration = TextDecoration.Underline,
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
