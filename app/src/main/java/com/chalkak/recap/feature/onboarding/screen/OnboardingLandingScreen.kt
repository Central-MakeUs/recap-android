package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.LandingCleanupIllustration
import com.chalkak.recap.feature.onboarding.component.OnboardingPrimaryButton
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingLandingScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_brand_mark_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        LandingCleanupIllustration(
            modifier = Modifier
                .padding(top = 48.dp)
                .fillMaxWidth()
                .height(170.dp),
        )
        StepHeader(
            title = stringResource(R.string.onboarding_landing_title),
            description = stringResource(R.string.onboarding_landing_description),
            modifier = Modifier.padding(top = 44.dp),
            titleStyle = MaterialTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OnboardingPrimaryButton(
                label = stringResource(R.string.onboarding_landing_start_button),
                onClick = { onAction(OnboardingAction.StartOnboarding) },
            )
            TextButton(
                onClick = { onAction(OnboardingAction.OpenLogin) },
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append(stringResource(R.string.onboarding_landing_login_prompt))
                            append(" ")
                        }
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(stringResource(R.string.onboarding_landing_login_link))
                        }
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingLandingScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingLandingScreen(onAction = {})
    }
}
