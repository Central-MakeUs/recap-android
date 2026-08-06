package com.chalkak.recap.feature.organize.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@Composable
fun OrganizeFailedContent(
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
            modifier = Modifier.size(OrganizeFailedTokens.IconSize),
            tint = RecapBlue300,
        )
        Spacer(modifier = Modifier.height(OrganizeFailedTokens.IconToTitleSpacing))
        Text(
            text = stringResource(R.string.organize_failed_title),
            style = RecapHeading2,
            color = Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(OrganizeFailedTokens.TitleToIllustrationSpacing))
        Image(
            painter = painterResource(R.drawable.recap_organize_failed),
            contentDescription = stringResource(
                R.string.organize_failed_illustration_content_description,
            ),
            modifier = Modifier.size(
                width = OrganizeFailedTokens.IllustrationWidth,
                height = OrganizeFailedTokens.IllustrationHeight,
            ),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(OrganizeFailedTokens.IllustrationToDescriptionSpacing))
        Text(
            text = stringResource(R.string.organize_failed_description),
            style = RecapBody1,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
}

private object OrganizeFailedTokens {
    val IconSize = 40.dp
    val IllustrationWidth = 111.dp
    val IllustrationHeight = 91.09.dp
    val IconToTitleSpacing = 10.dp
    val TitleToIllustrationSpacing = 56.dp
    val IllustrationToDescriptionSpacing = 22.dp
}

@Preview(name = "Organize Failed Content", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OrganizeFailedContentPreview() {
    RECAPTheme {
        OrganizeFailedContent()
    }
}
