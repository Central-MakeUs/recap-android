package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.R as OnboardingR
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader

private val CharacterAspectRatio = 552f / 426f
private val CharacterWidth = 148.dp
private val ScreenHorizontalPadding = 24.dp
private val GuideLottieAspectRatio = 375f / 200f

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
            // 화면 오른쪽 끝 padding을 상쇄해 캐릭터가 우측에 딱 붙도록 배치한다.
            // Guide Lottie는 pager 바깥 Box 오버레이로 그려 좌우 padding 0을 보장한다.
            StartFirstAnalyzeCharacter(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = ScreenHorizontalPadding, y = 8.dp),
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
    Text(
        text = stringResource(R.string.onboarding_start_first_analyze_description),
        modifier = modifier,
        style = RecapBody1,
        color = RecapGray500,
    )
}

@Composable
private fun StartFirstAnalyzeCharacter(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.onboarding_start_first_analyze_character),
        contentDescription = stringResource(
            R.string.onboarding_start_first_analyze_character_content_description,
        ),
        modifier = modifier
            .width(CharacterWidth)
            .aspectRatio(CharacterAspectRatio),
        contentScale = ContentScale.Fit,
    )
}

@Composable
internal fun StartFirstAnalyzeGuideIcons(
    modifier: Modifier = Modifier,
) {
    val guideDescription = stringResource(
        R.string.onboarding_start_first_analyze_guide_content_description,
    )
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(OnboardingR.raw.onboarding_start_first_analyze_guide),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GuideLottieAspectRatio)
            .semantics { contentDescription = guideDescription },
        contentScale = ContentScale.FillWidth,
    )
}

@OnboardingScreenPreview
@Composable
private fun OnboardingStartFirstAnalyzeScreenPreview() {
    OnboardingPreviewContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            OnboardingStartFirstAnalyzeScreen(
                uiState = OnboardingUiState(),
                onAction = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            )
            StartFirstAnalyzeGuideIcons(
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
