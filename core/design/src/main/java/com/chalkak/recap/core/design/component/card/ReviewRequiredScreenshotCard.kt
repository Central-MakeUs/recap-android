package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun ReviewRequiredScreenshotCard(
    reviewRequiredCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ReviewRequiredScreenshotCardTokens.ContainerCornerRadius),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = androidx.compose.foundation.BorderStroke(
            width = ReviewRequiredScreenshotCardTokens.ContainerBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(ReviewRequiredScreenshotCardTokens.ContainerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                ReviewRequiredScreenshotCardTokens.ContentSpacing
            ),
        ) {
            Surface(
                modifier = Modifier
                    .size(ReviewRequiredScreenshotCardTokens.IconContainerSize)
                    .clip(
                        RoundedCornerShape(
                            ReviewRequiredScreenshotCardTokens.IconContainerCornerRadius
                        )
                    ),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(ReviewRequiredScreenshotCardTokens.IconPadding)
                        .size(ReviewRequiredScreenshotCardTokens.IconSize),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    ReviewRequiredScreenshotCardTokens.TextSpacing
                ),
            ) {
                Text(
                    text = stringResource(
                        R.string.review_required_screenshot_card_title,
                        reviewRequiredCount,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = ReviewRequiredScreenshotCardTokens.TitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.review_required_screenshot_card_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = ReviewRequiredScreenshotCardTokens.DescriptionMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(
                    R.string.review_required_screenshot_card_action_content_description
                ),
                modifier = Modifier.size(ReviewRequiredScreenshotCardTokens.ChevronSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private object ReviewRequiredScreenshotCardTokens {
    val ContainerCornerRadius = 12.dp
    val ContainerBorderWidth = 1.dp
    val ContainerPadding = 16.dp
    val ContentSpacing = 16.dp
    val IconContainerSize = 32.dp
    val IconContainerCornerRadius = 6.dp
    val IconPadding = 5.dp
    val IconSize = 14.dp
    val TextSpacing = 2.dp
    val ChevronSize = 32.dp
    const val TitleMaxLines = 1
    const val DescriptionMaxLines = 1
}

@Preview(name = "Review Required Screenshot Card", showBackground = true, widthDp = 360)
@Composable
private fun ReviewRequiredScreenshotCardPreview() {
    RECAPTheme(dynamicColor = false) {
        ReviewRequiredScreenshotCard(
            reviewRequiredCount = ReviewRequiredScreenshotCardPreviewCount,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val ReviewRequiredScreenshotCardPreviewCount = 3
