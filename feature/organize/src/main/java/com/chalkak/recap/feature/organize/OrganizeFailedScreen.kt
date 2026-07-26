package com.chalkak.recap.feature.organize

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@Composable
fun OrganizeFailedScreen(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = OrganizeFailedTokens.HorizontalPadding),
        ) {
            OrganizeFailedContent(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .offset(y = (-84).dp),
            )
            RecapButton(
                text = stringResource(R.string.organize_failed_close),
                onClick = onCloseClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = OrganizeFailedTokens.BottomPadding),
                contentPadding = PaddingValues(vertical = 15.dp),
            )
        }
    }
}

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
    val HorizontalPadding = 24.dp
    val BottomPadding = 24.dp
    val IconSize = 40.dp
    val IllustrationWidth = 111.dp
    val IllustrationHeight = 91.09.dp
    val IconToTitleSpacing = 10.dp
    val TitleToIllustrationSpacing = 56.dp
    val IllustrationToDescriptionSpacing = 22.dp
}

@Preview(name = "Organize Failed Screen", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OrganizeFailedScreenPreview() {
    RECAPTheme {
        OrganizeFailedScreen(onCloseClick = {})
    }
}

