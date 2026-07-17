package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@Composable
fun OrganizedScreenshotSummaryCard(
    organizedCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(OrganizedScreenshotSummaryCardTokens.ContainerCornerRadius),
        color = RecapGray50,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = OrganizedScreenshotSummaryCardTokens.ContainerHorizontalPadding,
                vertical = OrganizedScreenshotSummaryCardTokens.ContainerVerticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(
                OrganizedScreenshotSummaryCardTokens.DescriptionTopSpacing,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        OrganizedScreenshotSummaryCardTokens.CountTopSpacing,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.organized_screenshot_summary_card_title),
                        style = RecapHeading2,
                        color = RecapGray900,
                    )
                    Text(
                        text = stringResource(
                            R.string.organized_screenshot_summary_card_count,
                            organizedCount,
                        ),
                        style = RecapHeading1,
                        color = RecapBlue300,
                    )
                }
                Image(
                    painter = painterResource(R.drawable.illust_organized_screenshot_summary),
                    contentDescription = stringResource(
                        R.string.organized_screenshot_summary_card_illustration_content_description,
                    ),
                    modifier = Modifier.size(
                        width = OrganizedScreenshotSummaryCardTokens.IllustrationWidth,
                        height = OrganizedScreenshotSummaryCardTokens.IllustrationHeight,
                    ),
                    contentScale = ContentScale.Fit,
                )
            }
            Text(
                text = stringResource(R.string.organized_screenshot_summary_card_description),
                style = RecapBody2,
                color = RecapGray500,
            )
        }
    }
}

private object OrganizedScreenshotSummaryCardTokens {
    val ContainerCornerRadius = 20.dp
    val ContainerHorizontalPadding = 31.dp
    val ContainerVerticalPadding = 20.dp
    val CountTopSpacing = 7.dp
    val DescriptionTopSpacing = 11.dp
    val IllustrationWidth = 61.dp
    val IllustrationHeight = 52.6.dp
}

@Preview(name = "Organized Screenshot Summary Card", showBackground = true, widthDp = 360)
@Composable
private fun OrganizedScreenshotSummaryCardPreview() {
    RECAPTheme(dynamicColor = false) {
        OrganizedScreenshotSummaryCard(
            organizedCount = OrganizedScreenshotSummaryCardPreviewCount,
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val OrganizedScreenshotSummaryCardPreviewCount = 128
