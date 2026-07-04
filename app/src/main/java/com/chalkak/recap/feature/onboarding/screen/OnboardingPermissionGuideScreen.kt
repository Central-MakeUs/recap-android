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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.GuideBullet
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
                title = stringResource(R.string.onboarding_permission_title)
            )
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                GuideBullet(text = stringResource(R.string.onboarding_permission_bullet_selected_only))
                GuideBullet(text = stringResource(R.string.onboarding_permission_bullet_original_available))
                GuideBullet(text = stringResource(R.string.onboarding_permission_bullet_sensitive_cards))
            }
        }
        RecapButton(
            text = stringResource(R.string.onboarding_permission_grant_button),
            onClick = { onAction(OnboardingAction.GrantPermission) },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            onClick = { onAction(OnboardingAction.SkipPermission) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.onboarding_permission_skip_button))
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingPermissionGuideScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingPermissionGuideScreen(onAction = {})
    }
}
