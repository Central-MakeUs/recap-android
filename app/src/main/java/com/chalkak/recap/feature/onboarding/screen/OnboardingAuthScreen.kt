package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.KakaoLoginButton
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.AppleButton
import com.chalkak.recap.feature.onboarding.component.BrandMark
import com.chalkak.recap.feature.onboarding.component.ScreenshotIllustration
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingAuthScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        BrandMark()
        Text(
            text = stringResource(R.string.onboarding_auth_tagline),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        ScreenshotIllustration(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth()
                .height(220.dp),
        )
        StepHeader(
            title = stringResource(R.string.onboarding_auth_title),
            description = stringResource(R.string.onboarding_auth_description),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
        )
        Spacer(modifier = Modifier.height(28.dp))
        KakaoLoginButton(onClick = { onAction(OnboardingAction.LoginWithKakao) })
        Spacer(modifier = Modifier.height(12.dp))
        AppleButton(onClick = { onAction(OnboardingAction.LoginWithApple) })
        TextButton(
            onClick = { onAction(OnboardingAction.LoginWithEmail) },
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(text = stringResource(R.string.onboarding_email_login_button))
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingAuthScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingAuthScreen(onAction = {})
    }
}
