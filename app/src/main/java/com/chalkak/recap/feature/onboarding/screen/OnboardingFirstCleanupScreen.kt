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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.FirstCleanupIllustration
import com.chalkak.recap.feature.onboarding.component.OnboardingPrimaryButton
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingFirstCleanupScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        StepHeader(
            title = stringResource(R.string.onboarding_first_cleanup_title),
            titleStyle = MaterialTheme.typography.displayLarge,
        )
        FirstCleanupIllustration(
            modifier = Modifier
                .padding(top = 52.dp)
                .fillMaxWidth()
                .height(170.dp),
        )
        StepHeader(
            title = stringResource(R.string.onboarding_first_cleanup_body_title),
            description = stringResource(R.string.onboarding_first_cleanup_description),
            modifier = Modifier.padding(top = 42.dp),
            titleStyle = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OnboardingPrimaryButton(
                label = stringResource(R.string.onboarding_first_cleanup_select_button),
                onClick = { onAction(OnboardingAction.SelectFirstScreenshots) },
            )
            TextButton(
                onClick = { onAction(OnboardingAction.SkipFirstCleanup) },
            ) {
                Text(
                    text = stringResource(R.string.onboarding_first_cleanup_later_button),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingFirstCleanupScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingFirstCleanupScreen(onAction = {})
    }
}
