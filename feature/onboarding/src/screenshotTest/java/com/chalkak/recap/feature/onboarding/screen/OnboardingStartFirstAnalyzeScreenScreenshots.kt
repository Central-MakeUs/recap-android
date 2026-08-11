package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.LottieTask
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.android.tools.screenshot.PreviewTest
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.qa.QaPhoneMatrix
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.R as OnboardingR
import java.util.concurrent.Executor

private val GuideLottieAspectRatio = 375f / 200f

@PreviewTest
@QaPhoneMatrix
@Composable
fun OnboardingStartFirstAnalyzeScreenScreenshot() {
    // Layoutlib screenshot renders crash when Lottie loads on a background executor.
    LottieTask.EXECUTOR = Executor { it.run() }

    OnboardingPreviewContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            OnboardingStartFirstAnalyzeScreen(
                uiState = OnboardingUiState(),
                onAction = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            )
            StartFirstAnalyzeGuideIconsScreenshot(
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun StartFirstAnalyzeGuideIconsScreenshot(
    modifier: Modifier = Modifier,
) {
    val guideDescription = stringResource(
        R.string.onboarding_start_first_analyze_guide_content_description,
    )
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(OnboardingR.raw.onboarding_start_first_analyze_guide),
    )

    LottieAnimation(
        composition = composition,
        progress = { 0f },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(GuideLottieAspectRatio)
            .semantics { contentDescription = guideDescription },
        contentScale = ContentScale.FillWidth,
    )
}
