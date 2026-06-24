package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StepHeader(
            title = "선택 정리 시작",
            description = "${uiState.selectedRange.title} 범위의 선택 정리를 시작합니다. 지금은 온보딩 완료 처리만 진행합니다.",
        )
        OnboardingPrimaryButton(
            label = "홈으로 이동",
            onClick = { onAction(OnboardingAction.StartCleanup) },
        )
    }
}
