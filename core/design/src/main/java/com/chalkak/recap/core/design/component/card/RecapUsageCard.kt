package com.chalkak.recap.core.design.component.card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.White
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RecapUsageCard(
    usedCount: Int,
    limitCount: Int,
    modifier: Modifier = Modifier,
) {
    val resolvedUsedCount = usedCount.coerceAtLeast(0)
    val resolvedLimitCount = limitCount.coerceAtLeast(0)
    val progress = usageProgress(resolvedUsedCount, resolvedLimitCount)
    val isPreview = LocalInspectionMode.current
    val animatedProgress = remember { Animatable(if (isPreview) progress else 0f) }

    LaunchedEffect(progress) {
        delay(150.milliseconds)
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = RecapUsageCardTokens.ProgressAnimationDurationMillis,
                easing = EaseInOutSine,
            ),
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(RecapUsageCardTokens.CornerRadius),
        color = White,
        border = BorderStroke(
            width = RecapUsageCardTokens.BorderWidth,
            color = RecapBlue300,
        ),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = RecapUsageCardTokens.HorizontalPadding,
                vertical = RecapUsageCardTokens.VerticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(RecapUsageCardTokens.SectionSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    RecapUsageCardTokens.TitleCountSpacing,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.usage_card_title),
                    modifier = Modifier.weight(1f),
                    style = RecapHeading2,
                    color = RecapGray900,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        RecapUsageCardTokens.CountSeparatorSpacing,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(
                            R.string.usage_card_sheet_count,
                            resolvedUsedCount,
                        ),
                        style = RecapHeading3,
                        color = RecapBlue300,
                    )
                    Text(
                        text = stringResource(R.string.usage_card_count_separator),
                        style = RecapHeading3,
                        color = RecapGray900,
                    )
                    Text(
                        text = stringResource(
                            R.string.usage_card_sheet_count,
                            resolvedLimitCount,
                        ),
                        style = RecapHeading3,
                        color = RecapGray900,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RecapUsageCardTokens.ProgressHeight),
                color = RecapOnboardingBlue,
                trackColor = RecapGray200,
                strokeCap = StrokeCap.Round,
                gapSize = -RecapUsageCardTokens.ProgressHeight,
                drawStopIndicator = {},
            )
            Text(
                text = stringResource(R.string.usage_card_reset_description),
                style = RecapBody2,
                color = RecapGray500,
            )
        }
    }
}

private fun usageProgress(
    usedCount: Int,
    limitCount: Int,
): Float {
    if (limitCount <= 0) return 0f
    return (usedCount.toFloat() / limitCount.toFloat()).coerceIn(0f, 1f)
}

private object RecapUsageCardTokens {
    val CornerRadius = 16.dp
    val BorderWidth = 1.dp
    val HorizontalPadding = 20.dp
    val VerticalPadding = 24.dp
    val SectionSpacing = 15.dp
    val TitleCountSpacing = 12.dp
    val CountSeparatorSpacing = 5.dp
    val ProgressHeight = 4.dp
    const val ProgressAnimationDurationMillis = 750
}

@Preview(name = "Usage Card", showBackground = true, widthDp = 360)
@Composable
private fun RecapUsageCardPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapUsageCard(
            usedCount = RecapUsageCardPreviewUsedCount,
            limitCount = RecapUsageCardPreviewLimitCount,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Usage Card Empty", showBackground = true, widthDp = 360)
@Composable
private fun RecapUsageCardEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapUsageCard(
            usedCount = 0,
            limitCount = RecapUsageCardPreviewLimitCount,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Usage Card Full", showBackground = true, widthDp = 360)
@Composable
private fun RecapUsageCardFullPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapUsageCard(
            usedCount = RecapUsageCardPreviewLimitCount,
            limitCount = RecapUsageCardPreviewLimitCount,
            modifier = Modifier.padding(16.dp),
        )
    }
}

private const val RecapUsageCardPreviewUsedCount = 12
private const val RecapUsageCardPreviewLimitCount = 20
