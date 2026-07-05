package com.chalkak.recap.feature.onboarding.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R

@Composable
internal fun OnboardingTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    skipText: String? = null,
    onSkip: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(
                    R.string.onboarding_back_button_content_description
                ),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (skipText != null && onSkip != null) {
            TextButton(onClick = onSkip) {
                Text(
                    text = skipText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OnboardingComponentPreview
@Composable
private fun OnboardingTopBarPreview() {
    OnboardingComponentPreviewContainer {
        OnboardingTopBar(
            onBack = {},
            skipText = stringResource(R.string.onboarding_first_cleanup_skip_top_button),
            onSkip = {},
        )
    }
}
