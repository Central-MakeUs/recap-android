package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chalkak.recap.feature.onboarding.CleanupRange
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingUiState
import com.chalkak.recap.feature.onboarding.component.CleanupRangeOptionCard
import com.chalkak.recap.feature.onboarding.component.OnboardingPrimaryButton
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
                title = "처음 정리할 스크린샷\n범위를 선택해주세요",
                description = "먼저 최근 스크린샷부터 정리해보세요.\n이후 새로 저장되는 스크린샷은 자동으로 정리됩니다.",
                modifier = Modifier.padding(top = 32.dp),
            )
            CleanupRange.entries.forEach { range ->
                CleanupRangeOptionCard(
                    title = range.title,
                    count = range.countLabel,
                    badge = range.badge,
                    selected = uiState.selectedRange == range,
                    onClick = { onAction(OnboardingAction.SelectRange(range)) },
                )
            }
        }
        OnboardingPrimaryButton(
            label = "이 범위로 정리 시작",
            onClick = { onAction(OnboardingAction.ConfirmRange) },
        )
    }
}
