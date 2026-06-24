package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.component.GuideBullet
import com.chalkak.recap.feature.onboarding.component.OnboardingPrimaryButton
import com.chalkak.recap.feature.onboarding.component.OnboardingTopBar
import com.chalkak.recap.feature.onboarding.component.PermissionIconTile
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingPermissionGuideScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        OnboardingTopBar(
            progress = "1 / 3",
            onBack = { onAction(OnboardingAction.Back) },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            PermissionIconTile(modifier = Modifier.padding(top = 40.dp))
            StepHeader(
                title = "스크린샷을 정리하려면\n사진 접근 권한이 필요해요",
                description = "RE-CAP은 사용자가 허용한 범위 안에서\n스크린샷만 찾아 정리합니다.",
            )
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                GuideBullet(text = "선택한 범위의 스크린샷만 분석해요")
                GuideBullet(text = "원본 이미지는 앱에서 다시 확인할 수 있어요")
                GuideBullet(text = "민감한 정보는 확인이 필요한 카드로 분리할 수 있어요")
            }
        }
        OnboardingPrimaryButton(
            label = "권한 허용하기",
            onClick = { onAction(OnboardingAction.GrantPermission) },
        )
        TextButton(
            onClick = { onAction(OnboardingAction.SkipPermission) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "나중에 하기")
        }
    }
}
