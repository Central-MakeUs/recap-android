package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonIconPlacement
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.OnboardingTopBar
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingLoginScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        OnboardingTopBar(
            progress = stringResource(R.string.onboarding_login_progress),
            onBack = { onAction(OnboardingAction.Back) },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepHeader(
                title = stringResource(R.string.onboarding_login_title),
                description = stringResource(R.string.onboarding_login_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecapButton(
                text = stringResource(R.string.onboarding_kakao_login_full),
                compactText = stringResource(R.string.onboarding_kakao_login_short),
                onClick = { onAction(OnboardingAction.LoginWithKakao) },
                modifier = Modifier.fillMaxWidth(),
                colors = RecapButtonDefaults.kakaoColors(),
                leadingIcon = {
                    Image(
                        painter = painterResource(R.drawable.kakao_96px),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                iconPlacement = RecapButtonIconPlacement.FixedStart,
            )
            RecapButton(
                text = stringResource(R.string.onboarding_apple_login_button),
                onClick = { onAction(OnboardingAction.LoginWithApple) },
                modifier = Modifier.fillMaxWidth(),
                colors = RecapButtonDefaults.appleColors(),
            )
            TextButton(
                onClick = { onAction(OnboardingAction.LoginWithEmail) },
            ) {
                Text(
                    text = stringResource(R.string.onboarding_email_login_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingLoginScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingLoginScreen(onAction = {})
    }
}
