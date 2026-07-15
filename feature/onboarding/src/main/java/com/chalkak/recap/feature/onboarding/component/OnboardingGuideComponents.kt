package com.chalkak.recap.feature.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3

@Composable
internal fun GuideBullet(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun StepHeader(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.displaySmall,
    descriptionStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    contentSpacing: Dp = 17.dp,
    descriptionFontWeight: FontWeight? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Text(
            text = title,
            style = titleStyle,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        if (description != null) {
            Text(
                text = description,
                style = descriptionStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = descriptionFontWeight,
            )
        }
    }
}

@Composable
internal fun OnboardingBottomActions(
    primaryText: String,
    secondaryText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        RecapButton(
            text = primaryText,
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = RecapBlue300
        )
        TextButton(
            onClick = onSecondaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 11.dp),
        ) {
            Text(
                text = secondaryText,
                style =  RecapHeading3,
                color = RecapGray500,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OnboardingComponentPreview
@Composable
private fun GuideBulletPreview() {
    OnboardingComponentPreviewContainer {
        GuideBullet(text = stringResource(R.string.onboarding_permission_purpose_selected_only))
    }
}

@OnboardingComponentPreview
@Composable
private fun StepHeaderPreview() {
    OnboardingComponentPreviewContainer {
        StepHeader(
            title = stringResource(R.string.onboarding_permission_title),
            description = stringResource(R.string.onboarding_permission_description),
        )
    }
}

@OnboardingComponentPreview
@Composable
private fun OnboardingBottomActionsPreview() {
    OnboardingComponentPreviewContainer {
        OnboardingBottomActions(
            primaryText = stringResource(R.string.onboarding_permission_grant_button),
            secondaryText = stringResource(R.string.onboarding_permission_skip_button),
            onPrimaryClick = {},
            onSecondaryClick = {},
        )
    }
}
