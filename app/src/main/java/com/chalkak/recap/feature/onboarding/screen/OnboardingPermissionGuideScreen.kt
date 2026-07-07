package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.GuideBullet
import com.chalkak.recap.feature.onboarding.component.OnboardingBottomActions
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingPermissionGuideScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RecapLogo(
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 24.dp)
                    .width(58.dp)
                    .aspectRatio(RecapLogoAspectRatio),
            )
            StepHeader(
                title = stringResource(R.string.onboarding_permission_title),
                description = stringResource(R.string.onboarding_permission_description)
            )
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                GuideBullet(text = stringResource(R.string.onboarding_permission_bullet_selected_only))
                GuideBullet(text = stringResource(R.string.onboarding_permission_bullet_original_available))
                GuideBullet(text = stringResource(R.string.onboarding_permission_bullet_sensitive_cards))
            }
        }
        OnboardingBottomActions(
            primaryText = stringResource(R.string.onboarding_permission_grant_button),
            secondaryText = stringResource(R.string.onboarding_permission_skip_button),
            onPrimaryClick = { onAction(OnboardingAction.GrantPermission) },
            onSecondaryClick = { onAction(OnboardingAction.SkipPermission) },
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingPermissionGuideScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingPermissionGuideScreen(onAction = {})
    }
}
