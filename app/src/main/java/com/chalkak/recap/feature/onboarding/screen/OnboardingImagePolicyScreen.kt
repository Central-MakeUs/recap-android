package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.StepHeader

@Composable
fun OnboardingImagePolicyScreen(
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
        ) {
            Text(
                text = stringResource(R.string.onboarding_policy_progress),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            StepHeader(
                title = stringResource(R.string.onboarding_policy_title),
                modifier = Modifier.padding(top = 12.dp),
                titleStyle = MaterialTheme.typography.displayLarge,
            )
            Column(
                modifier = Modifier.padding(top = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ImagePolicyCard(
                    iconType = PolicyIconType.DirectSelect,
                    title = stringResource(R.string.onboarding_policy_direct_select_title),
                    description = stringResource(R.string.onboarding_policy_direct_select_description),
                )
                ImagePolicyCard(
                    iconType = PolicyIconType.ShareSend,
                    title = stringResource(R.string.onboarding_policy_share_send_title),
                    description = stringResource(R.string.onboarding_policy_share_send_description),
                )
                ImagePolicyCard(
                    iconType = PolicyIconType.NoAutoUpload,
                    title = stringResource(R.string.onboarding_policy_no_auto_upload_title),
                    description = stringResource(R.string.onboarding_policy_no_auto_upload_description),
                )
            }
        }
        PolicyNotice(
            text = stringResource(R.string.onboarding_policy_notice),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
        RecapButton(
            text = stringResource(R.string.onboarding_policy_next_button),
            onClick = { onAction(OnboardingAction.ContinuePolicy) },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun ImagePolicyCard(
    iconType: PolicyIconType,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PolicyIconTile(iconType = iconType)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PolicyIconTile(
    iconType: PolicyIconType,
    modifier: Modifier = Modifier,
) {
    val iconColor = if (iconType == PolicyIconType.NoAutoUpload) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = modifier
            .size(52.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconType.imageVector,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PolicyNotice(
    text: String,
    modifier: Modifier = Modifier,
) {
    val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val strokeWidth = 1.8.dp.toPx()
            drawCircle(
                color = iconColor,
                radius = 7.dp.toPx(),
                center = center,
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = iconColor,
                start = Offset(center.x, 8.dp.toPx()),
                end = Offset(center.x, 13.dp.toPx()),
                strokeWidth = strokeWidth,
            )
            drawCircle(
                color = iconColor,
                radius = 1.dp.toPx(),
                center = Offset(center.x, 5.dp.toPx()),
            )
        }
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class PolicyIconType {
    DirectSelect,
    ShareSend,
    NoAutoUpload,
}

private val PolicyIconType.imageVector: ImageVector
    get() = when (this) {
        PolicyIconType.DirectSelect -> Icons.Outlined.CheckBox
        PolicyIconType.ShareSend -> Icons.Outlined.Share
        PolicyIconType.NoAutoUpload -> Icons.Outlined.CloudOff
    }

@OnboardingScreenPreview
@Composable
private fun OnboardingImagePolicyScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingImagePolicyScreen(onAction = {})
    }
}
