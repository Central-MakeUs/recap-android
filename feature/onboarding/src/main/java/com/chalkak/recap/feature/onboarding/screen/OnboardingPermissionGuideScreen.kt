package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingPermissionGuideScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
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
                description = stringResource(R.string.onboarding_permission_description),
                modifier = Modifier.padding(top = 10.dp),
            )
            PermissionInfoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
        }
        RecapButton(
            text = stringResource(R.string.onboarding_permission_grant_button),
            onClick = { onAction(OnboardingAction.GrantPermission) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 11.dp),
            colors = RecapBlue300
        )
    }
}

@Composable
private fun PermissionInfoCard(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(RecapGray50)
            .padding(horizontal = 18.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        PermissionInfoSection(
            label = stringResource(R.string.onboarding_permission_name_label),
            items = listOf(stringResource(R.string.onboarding_permission_name_value)),
        )
        PermissionInfoSection(
            label = stringResource(R.string.onboarding_permission_purpose_label),
            items = listOf(
                stringResource(R.string.onboarding_permission_purpose_select),
                stringResource(R.string.onboarding_permission_purpose_selected_only),
            ),
        )
    }
}

@Composable
private fun PermissionInfoSection(
    label: String,
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_24),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = RecapGray300,
            )
            Text(
                text = label,
                style = RecapBody1,
                color = RecapGray700,
                fontWeight = FontWeight.Medium,
            )
        }
        Column(
            modifier = Modifier.padding(start = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                PermissionInfoBullet(text = item)
            }
        }
    }
}

@Composable
private fun PermissionInfoBullet(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(RecapGray500),
        )
        Text(
            text = text,
            style = RecapBody2,
            color = RecapGray500,
            modifier = Modifier.weight(1f),
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
