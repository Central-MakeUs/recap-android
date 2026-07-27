package com.chalkak.recap.core.design.component.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import kotlin.math.abs

@Composable
fun RecapStepProgressIndicator(
    currentStepIndex: Int,
    modifier: Modifier = Modifier,
    stepCount: Int = RecapStepProgressIndicatorDefaults.StepCount,
) {
    RecapStepProgressIndicator(
        progress = currentStepIndex.toFloat(),
        modifier = modifier,
        stepCount = stepCount,
    )
}

@Composable
fun RecapStepProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    stepCount: Int = RecapStepProgressIndicatorDefaults.StepCount,
) {
    val resolvedStepCount = stepCount.coerceAtLeast(1)
    val clampedProgress = progress.coerceIn(0f, (resolvedStepCount - 1).toFloat())
    val inactiveColor = RecapGray100
    val activeColor = RecapOnboardingBlue

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RecapStepProgressIndicatorDefaults.Height),
        horizontalArrangement = Arrangement.spacedBy(
            RecapStepProgressIndicatorDefaults.Spacing,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(resolvedStepCount) { index ->
            val selectedFraction = (1f - abs(clampedProgress - index)).coerceIn(0f, 1f)
            val width = RecapStepProgressIndicatorDefaults.InactiveSize +
                    (
                            RecapStepProgressIndicatorDefaults.ActiveWidth -
                                    RecapStepProgressIndicatorDefaults.InactiveSize
                            ) * selectedFraction
            val color = lerp(inactiveColor, activeColor, selectedFraction)

            Box(
                modifier = Modifier
                    .width(width)
                    .height(RecapStepProgressIndicatorDefaults.InactiveSize)
                    .background(
                        color = color,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

object RecapStepProgressIndicatorDefaults {
    val Height = 48.dp
    val Spacing = 5.dp
    val InactiveSize = 8.dp
    val ActiveWidth = 17.dp
    const val StepCount = 3
}

@Preview(name = "RecapStepProgressIndicator", showBackground = true, widthDp = 360)
@Composable
private fun RecapStepProgressIndicatorPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapStepProgressIndicator(progress = 0.5f)
    }
}

@Preview(name = "RecapStepProgressIndicator settled", showBackground = true, widthDp = 360)
@Composable
private fun RecapStepProgressIndicatorSettledPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapStepProgressIndicator(currentStepIndex = 1, stepCount = 4)
    }
}
