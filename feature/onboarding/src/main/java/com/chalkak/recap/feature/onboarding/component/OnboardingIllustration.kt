package com.chalkak.recap.feature.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapIllustrationPlaceholderGray
import com.chalkak.recap.feature.onboarding.OnboardingIllustrationSignal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal enum class OnboardingIllustrationVariant {
    Landing,
    Default,
}

@Composable
internal fun OnboardingIllustration(
    signalFlow: Flow<OnboardingIllustrationSignal>,
    modifier: Modifier = Modifier,
    variant: OnboardingIllustrationVariant = OnboardingIllustrationVariant.Default,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 220.dp, height = 210.dp)
                .background(RecapIllustrationPlaceholderGray),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingIllustrationPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingIllustration(
            signalFlow = emptyFlow(),
            variant = OnboardingIllustrationVariant.Landing,
        )
    }
}
