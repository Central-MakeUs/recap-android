package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingStartFirstAnalyzeScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            title = stringResource(R.string.onboarding_start_first_analyze_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentSpacing = 24.dp,
            descriptionFontWeight = FontWeight.Bold,
        )
        StartFirstAnalyzeDescription(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            StartFirstAnalyzeCharacterPlaceholder(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 18.dp, y = 18.dp),
            )
            StartFirstAnalyzeGuidePlaceholder(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 32.dp),
            )
        }
        OnboardingBottomActions(
            primaryText = stringResource(R.string.onboarding_start_first_analyze_select_button),
            secondaryText = stringResource(R.string.onboarding_start_first_analyze_later_button),
            onPrimaryClick = { onAction(OnboardingAction.OpenScreenshotPicker) },
            onSecondaryClick = { onAction(OnboardingAction.SkipStartFirstAnalyze) },
        )
    }
}

@Composable
private fun StartFirstAnalyzeDescription(
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val galleryPrefix = stringResource(
        R.string.onboarding_start_first_analyze_description_gallery_prefix
    )
    val selectHighlight = stringResource(
        R.string.onboarding_start_first_analyze_description_select_highlight
    )
    val gallerySuffix = stringResource(
        R.string.onboarding_start_first_analyze_description_gallery_suffix
    )
    val captureHighlight = stringResource(
        R.string.onboarding_start_first_analyze_description_capture_highlight
    )
    val captureSuffix = stringResource(
        R.string.onboarding_start_first_analyze_description_capture_suffix
    )

    Text(
        text = buildAnnotatedString {
            append(galleryPrefix)
            append(" ")
            pushStyle(SpanStyle(color = primaryColor))
            append(selectHighlight)
            pop()
            append(gallerySuffix)
            append("\n")
            pushStyle(SpanStyle(color = primaryColor))
            append(captureHighlight)
            pop()
            append(captureSuffix)
        },
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun StartFirstAnalyzeCharacterPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 104.dp, height = 76.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
            ),
    )
}

@Composable
private fun StartFirstAnalyzeGuidePlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
            ),
    )
}

@OnboardingScreenPreview
@Composable
private fun OnboardingStartFirstAnalyzeScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingStartFirstAnalyzeScreen(
            uiState = OnboardingUiState(),
            onAction = {},
        )
    }
}
