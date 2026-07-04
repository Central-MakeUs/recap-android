package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.feature.onboarding.CleanupRange
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingCleanupStartScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRangeTitle = stringResource(uiState.selectedRange.titleResId)
    val ocrJob = uiState.activeOcrJob
    val progress = ocrJob?.progress ?: 0f
    val completedCount = ocrJob?.completedCount ?: 0
    val totalCount = ocrJob?.totalCount?.takeIf { it > 0 } ?: uiState.selectedRangeCount
    val isCompleted = ocrJob?.isCompleted == true
    val buttonLabelResId = if (isCompleted) {
        R.string.onboarding_cleanup_start_home_button
    } else {
        R.string.onboarding_cleanup_start_preview_button
    }

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
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(180.dp),
                strokeWidth = 12.dp,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_cleanup_start_progress_percent, (progress * 100).toInt()),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.onboarding_cleanup_start_progress_count, completedCount, totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        RecapButton(
            text = stringResource(buttonLabelResId),
            onClick = { onAction(OnboardingAction.StartCleanup) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingCleanupStartScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingCleanupStartScreen(
            uiState = OnboardingUiState(selectedRange = CleanupRange.Last30Days),
            onAction = {},
        )
    }
}
