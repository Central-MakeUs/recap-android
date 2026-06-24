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
import androidx.compose.ui.unit.dp
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.component.AppleButton
import com.chalkak.recap.feature.onboarding.component.BrandMark
import com.chalkak.recap.feature.onboarding.component.KakaoButton
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
        Spacer(modifier = Modifier.height(48.dp))
        BrandMark()
        Text(
            text = "SCREENSHOT ORGANIZER",
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
            title = "저장된 스크린샷을\n필요한 순간 다시 찾을 수 있게",
            description = "맛집, 상품, 일정, 레퍼런스까지\n스크린샷 속 정보를 카드와 컬렉션으로 정리해요.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
        )
        Spacer(modifier = Modifier.height(28.dp))
        KakaoButton(onClick = { onAction(OnboardingAction.LoginWithKakao) })
        Spacer(modifier = Modifier.height(12.dp))
        AppleButton(onClick = { onAction(OnboardingAction.LoginWithApple) })
        TextButton(
            onClick = { onAction(OnboardingAction.LoginWithEmail) },
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(text = "이메일로 로그인")
        }
    }
}
