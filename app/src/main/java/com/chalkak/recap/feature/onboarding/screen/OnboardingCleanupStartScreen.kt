package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.OnboardingPrimaryButton
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingCleanupStartScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRangeTitle = stringResource(uiState.selectedRange.titleResId)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StepHeader(
            title = stringResource(R.string.onboarding_cleanup_start_title),
            description = stringResource(
                R.string.onboarding_cleanup_start_description,
                selectedRangeTitle
            ),
        )
        OnboardingPrimaryButton(
            label = stringResource(R.string.onboarding_cleanup_start_home_button),
            onClick = { onAction(OnboardingAction.StartCleanup) },
        )
    }
}
