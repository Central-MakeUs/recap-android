package com.chalkak.recap.feature.organize.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@Composable
fun OrganizePartialFailedContent(
    successCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error_circle_24),
            contentDescription = stringResource(
                R.string.organize_failed_icon_content_description,
            ),
            modifier = Modifier.size(OrganizePartialFailedTokens.IconSize),
            tint = RecapBlue300,
        )
        Spacer(modifier = Modifier.height(OrganizePartialFailedTokens.IconToTitleSpacing))
        Text(
            text = stringResource(R.string.organize_partial_failed_title),
            style = RecapHeading2,
            color = Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(OrganizePartialFailedTokens.TitleToChipSpacing))
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = RecapBlue50,
        ) {
            Text(
                text = stringResource(
                    R.string.organize_partial_failed_success_chip,
                    successCount,
                ),
                modifier = Modifier.padding(
                    horizontal = OrganizePartialFailedTokens.ChipHorizontalPadding,
                    vertical = OrganizePartialFailedTokens.ChipVerticalPadding,
                ),
                style = RecapCaption1,
                color = RecapOnboardingBlue,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(OrganizePartialFailedTokens.ChipToIllustrationSpacing))
        Image(
            painter = painterResource(R.drawable.recap_organize_failed),
            contentDescription = stringResource(
                R.string.organize_failed_illustration_content_description,
            ),
            modifier = Modifier
                .size(
                    width = OrganizePartialFailedTokens.IllustrationWidth,
                    height = OrganizePartialFailedTokens.IllustrationHeight,
                )
                .offset(x = (-5).dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(OrganizePartialFailedTokens.IllustrationToDescriptionSpacing))
        Text(
            text = stringResource(R.string.organize_partial_failed_description),
            style = RecapBody1,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
}

private object OrganizePartialFailedTokens {
    val IconSize = 40.dp
    val IllustrationWidth = 111.dp
    val IllustrationHeight = 91.09.dp
    val IconToTitleSpacing = 10.dp
    val TitleToChipSpacing = 18.dp
    val ChipToIllustrationSpacing = 56.dp
    val IllustrationToDescriptionSpacing = 24.dp
    val ChipHorizontalPadding = 34.5.dp
    val ChipVerticalPadding = 10.dp
}

@Preview(
    name = "Organize Partial Failed Content",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizePartialFailedContentPreview() {
    RECAPTheme {
        OrganizePartialFailedContent(successCount = 3)
    }
}
