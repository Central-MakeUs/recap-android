package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun OrganizedCaptureCard(
    organizedCaptureCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(OrganizedCaptureCardTokens.ContainerCornerRadius),
        color = OrganizedCaptureCardTokens.ContainerColor,
        contentColor = OrganizedCaptureCardTokens.ContentColor,
        border = BorderStroke(
            width = OrganizedCaptureCardTokens.ContainerBorderWidth,
            color = OrganizedCaptureCardTokens.ContainerBorderColor,
        ),
    ) {
        Row(
            modifier = Modifier.padding(OrganizedCaptureCardTokens.ContainerPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                OrganizedCaptureCardTokens.ContentSpacing
            ),
        ) {
            Surface(
                modifier = Modifier.size(OrganizedCaptureCardTokens.IconContainerSize),
                shape = CircleShape,
                color = OrganizedCaptureCardTokens.ContentColor,
                contentColor = OrganizedCaptureCardTokens.IconColor,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(OrganizedCaptureCardTokens.IconPadding)
                        .size(OrganizedCaptureCardTokens.IconSize),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(OrganizedCaptureCardTokens.TextSpacing),
            ) {
                Text(
                    text = stringResource(
                        R.string.organized_capture_card_title,
                        organizedCaptureCount,
                    ),
                    color = OrganizedCaptureCardTokens.TitleColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = OrganizedCaptureCardTokens.TitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.organized_capture_card_description),
                    color = OrganizedCaptureCardTokens.DescriptionColor,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = OrganizedCaptureCardTokens.DescriptionMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private object OrganizedCaptureCardTokens {
    val ContainerCornerRadius = 12.dp
    val ContainerBorderWidth = 1.dp
    val ContainerPadding = 16.dp
    val ContentSpacing = 16.dp
    val IconContainerSize = 32.dp
    val IconPadding = 5.dp
    val IconSize = 14.dp
    val TextSpacing = 2.dp
    val ContainerColor = Color(0xFFEFF8F0)
    val ContainerBorderColor = Color(0xFFCBEBD3)
    val ContentColor = Color(0xFF2F7D4A)
    val TitleColor = Color(0xFF20643A)
    val DescriptionColor = Color(0xFF4F8D62)
    val IconColor = Color.White
    const val TitleMaxLines = 1
    const val DescriptionMaxLines = 1
}

@Preview(name = "Organized Capture Card", showBackground = true, widthDp = 360)
@Composable
private fun OrganizedCaptureCardPreview() {
    RECAPTheme(dynamicColor = false) {
        OrganizedCaptureCard(
            organizedCaptureCount = OrganizedCaptureCardPreviewCount,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val OrganizedCaptureCardPreviewCount = 3
