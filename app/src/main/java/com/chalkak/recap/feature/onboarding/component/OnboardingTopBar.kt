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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R

@Composable
internal fun OnboardingTopBar(
    progress: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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
        OnboardingProgressText(progress = progress)
    }
}

@Composable
private fun OnboardingProgressText(
    progress: String,
    modifier: Modifier = Modifier,
) {
    val progressParts = progress.split("/", limit = 2)
    val currentStep = progressParts.first().trim()
    val totalSteps = progressParts.getOrNull(1)?.trim()
    val progressText = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(currentStep)
        }
        if (totalSteps != null) {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                append(" / ")
                append(totalSteps)
            }
        }
    }

    Text(
        text = progressText,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@OnboardingComponentPreview
@Composable
private fun OnboardingTopBarPreview() {
    OnboardingComponentPreviewContainer {
        OnboardingTopBar(
            progress = "1 / 3",
            onBack = {},
        )
    }
}
