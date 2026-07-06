package com.chalkak.recap.feature.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val OnboardingProgressInactiveSize = 8.dp
private val OnboardingProgressActiveWidth = 17.dp

@Composable
internal fun OnboardingTopBar(
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    stepCount: Int = 3,
) {
    OnboardingTopBar(
        progress = currentStepIndex.toFloat(),
        modifier = modifier,
        stepCount = stepCount,
    )
}

@Composable
internal fun OnboardingTopBar(
    progress: Float,
    modifier: Modifier = Modifier,
    stepCount: Int = 3,
) {
    val resolvedStepCount = stepCount.coerceAtLeast(1)
    val clampedProgress = progress.coerceIn(0f, (resolvedStepCount - 1).toFloat())
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val activeColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(resolvedStepCount) { index ->
            val selectedFraction = (1f - abs(clampedProgress - index)).coerceIn(0f, 1f)
            val width = OnboardingProgressInactiveSize +
                (OnboardingProgressActiveWidth - OnboardingProgressInactiveSize) * selectedFraction
            val color = lerp(inactiveColor, activeColor, selectedFraction)

            Box(
                modifier = Modifier
                    .width(width)
                    .height(OnboardingProgressInactiveSize)
                    .background(
                        color = color,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@OnboardingComponentPreview
@Composable
private fun OnboardingTopBarPreview() {
    OnboardingComponentPreviewContainer {
        OnboardingTopBar(progress = 0.5f)
    }
}
