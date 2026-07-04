package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.feature.onboarding.CleanupRange
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.CleanupRangeOptionCard
import com.chalkak.recap.feature.onboarding.component.OnboardingTopBar
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingCleanupRangeScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        OnboardingTopBar(
            progress = "2 / 3",
            onBack = { onAction(OnboardingAction.Back) },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepHeader(
                title = stringResource(R.string.onboarding_cleanup_range_title),
                description = stringResource(R.string.onboarding_cleanup_range_description),
                modifier = Modifier.padding(top = 32.dp),
            )
            CleanupRange.entries.forEach { range ->
                val count = uiState.rangeCounts[range] ?: 0
                CleanupRangeOptionCard(
                    title = stringResource(range.titleResId),
                    count = if (uiState.isRangeCountLoading) {
                        stringResource(R.string.onboarding_cleanup_range_count_loading)
                    } else {
                        stringResource(range.countLabelResId, count)
                    },
                    badge = range.badgeResId?.let { stringResource(it) },
                    selected = uiState.selectedRange == range,
                    onClick = { onAction(OnboardingAction.SelectRange(range)) },
                )
            }
            val guideMessage = when {
                uiState.imageAccessLevel == ImageAccessLevel.Denied ->
                    stringResource(R.string.onboarding_cleanup_range_permission_required)

                !uiState.isRangeCountLoading && uiState.selectedRangeCount == 0 ->
                    stringResource(R.string.onboarding_cleanup_range_empty)

                else -> null
            }
            if (guideMessage != null) {
                Text(
                    text = guideMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        RecapButton(
            text = stringResource(R.string.onboarding_cleanup_range_confirm_button),
            onClick = { onAction(OnboardingAction.ConfirmRange) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canConfirmRange,
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingCleanupRangeScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingCleanupRangeScreen(
            uiState = OnboardingUiState(
                selectedRange = CleanupRange.Last30Days,
                imageAccessLevel = ImageAccessLevel.Full,
            ),
            onAction = {},
        )
    }
}
