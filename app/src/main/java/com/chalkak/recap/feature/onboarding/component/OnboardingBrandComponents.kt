package com.chalkak.recap.feature.onboarding.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio

@Composable
internal fun BrandMark(
    modifier: Modifier = Modifier,
) {
    RecapLogo(
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier
            .width(111.dp)
            .aspectRatio(RecapLogoAspectRatio),
    )
}

@Composable
internal fun ScreenshotIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        IllustrationCard(
            label = stringResource(R.string.onboarding_screenshot_label),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .width(100.dp)
                .height(158.dp)
                .offset(x = (-54).dp, y = 4.dp)
                .graphicsLayer {
                    rotationZ = -11f
                },
        )
        IllustrationCard(
            label = stringResource(R.string.onboarding_saved_info_label),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .width(132.dp)
                .height(152.dp)
                .offset(x = 44.dp, y = (-4).dp)
                .graphicsLayer {
                    rotationZ = 9f
                },
        )
    }
}

@Composable
internal fun LandingCleanupIllustration(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScreenshotInputTile(
            modifier = Modifier
                .width(96.dp)
                .height(150.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(36.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        OrganizedInfoTile(
            modifier = Modifier
                .width(120.dp)
                .height(150.dp),
        )
    }
}

@Composable
internal fun FirstCleanupIllustration(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScreenshotStack(
            modifier = Modifier
                .width(190.dp)
                .height(132.dp),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        OrganizedInfoTile(
            modifier = Modifier
                .width(120.dp)
                .height(150.dp),
        )
    }
}

@Composable
private fun ScreenshotStack(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        ScreenshotStackTile(
            modifier = Modifier
                .offset(x = 84.dp)
                .width(82.dp)
                .height(118.dp)
                .graphicsLayer { rotationZ = 8f },
        )
        ScreenshotStackTile(
            modifier = Modifier
                .offset(x = 42.dp, y = 2.dp)
                .width(82.dp)
                .height(118.dp)
                .graphicsLayer { rotationZ = -1f },
        )
        ScreenshotStackTile(
            modifier = Modifier
                .offset(x = 0.dp, y = 4.dp)
                .width(82.dp)
                .height(118.dp)
                .graphicsLayer { rotationZ = -8f },
        )
    }
}

@Composable
private fun ScreenshotStackTile(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
    )
}

@Composable
private fun ScreenshotInputTile(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = stringResource(R.string.onboarding_screenshot_label),
            modifier = Modifier.padding(bottom = 12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun OrganizedInfoTile(
    modifier: Modifier = Modifier,
) {
    val placeholderColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(66.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(width = 46.dp, height = 18.dp),
        ) {
            drawRoundRect(
                color = placeholderColor,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun IllustrationCard(
    label: String,
    elevation: CardElevation,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = elevation,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
    }
}

@OnboardingComponentPreview
@Composable
private fun BrandMarkPreview() {
    OnboardingComponentPreviewContainer {
        BrandMark()
    }
}

@OnboardingComponentPreview
@Composable
private fun ScreenshotIllustrationPreview() {
    OnboardingComponentPreviewContainer {
        ScreenshotIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    }
}

@OnboardingComponentPreview
@Composable
private fun LandingCleanupIllustrationPreview() {
    OnboardingComponentPreviewContainer {
        LandingCleanupIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
        )
    }
}

@OnboardingComponentPreview
@Composable
private fun FirstCleanupIllustrationPreview() {
    OnboardingComponentPreviewContainer {
        FirstCleanupIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
        )
    }
}

@OnboardingComponentPreview
@Composable
private fun OrganizedInfoTilePreview() {
    OnboardingComponentPreviewContainer {
        OrganizedInfoTile(
            modifier = Modifier
                .width(120.dp)
                .height(150.dp),
        )
    }
}

@OnboardingComponentPreview
@Composable
private fun IllustrationCardPreview() {
    OnboardingComponentPreviewContainer {
        IllustrationCard(
            label = stringResource(R.string.onboarding_screenshot_label),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .width(132.dp)
                .height(152.dp),
        )
    }
}
