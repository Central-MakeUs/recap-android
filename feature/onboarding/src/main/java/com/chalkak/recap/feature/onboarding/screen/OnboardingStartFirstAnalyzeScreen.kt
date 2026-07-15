package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader

private val CharacterAspectRatio = 552f / 426f
private val CharacterWidth = 148.dp
private val ScreenHorizontalPadding = 24.dp
private val GuideIconCardSize = 88.dp
private val GuideIconSize = 48.dp
private val GuideIconEdgeOverflow = 24.dp

private val GuideIconResources = listOf(
    R.drawable.onboarding_background_4,
    R.drawable.onboarding_background_1,
    R.drawable.onboarding_background_3,
    R.drawable.onboarding_background_2,
)

private val GuideIconRotations = listOf(-4f, 3f, -2f, 5f)

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // 화면 오른쪽 끝 padding을 상쇄해 캐릭터가 우측에 딱 붙도록 배치한다.
            StartFirstAnalyzeCharacter(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = ScreenHorizontalPadding, y = 8.dp),
            )
            val guideIconsBreakout = ScreenHorizontalPadding + GuideIconEdgeOverflow
            StartFirstAnalyzeGuideIcons(
                modifier = Modifier
                    .align(Alignment.Center)
                    // 좌우 padding + overflow만큼 넓혀 양끝 아이콘이 화면 가장자리를 살짝 넘기게 한다.
                    .width(maxWidth + guideIconsBreakout * 2)
                    .offset(y = 36.dp),
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
private fun StartFirstAnalyzeGuideIcons(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GuideIconResources.forEachIndexed { index, iconRes ->
            StartFirstAnalyzeGuideIconCard(
                iconRes = iconRes,
                rotationZ = GuideIconRotations[index],
            )
        }
    }
}

@Composable
private fun StartFirstAnalyzeGuideIconCard(
    iconRes: Int,
    rotationZ: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(GuideIconCardSize)
            .graphicsLayer { this.rotationZ = rotationZ }
            .clip(RoundedCornerShape(20.dp))
            .background(RecapGray50),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(GuideIconSize),
            contentScale = ContentScale.Fit,
        )
    }
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
