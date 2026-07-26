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
fun OrganizeSuccessScreen(
    successCount: Int,
    onDoneClick: () -> Unit,
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
                .padding(horizontal = OrganizeSuccessTokens.HorizontalPadding),
        ) {
            OrganizeSuccessContent(
                successCount = successCount,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .offset(y = (-44).dp),
            )
            RecapButton(
                text = stringResource(R.string.organize_success_done),
                onClick = onDoneClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = OrganizeSuccessTokens.BottomPadding),
                contentPadding = PaddingValues(vertical = 15.dp),
            )
        }
    }
}

@Composable
fun OrganizeSuccessContent(
    successCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle_24),
            contentDescription = stringResource(
                R.string.organize_success_icon_content_description,
            ),
            modifier = Modifier.size(OrganizeSuccessTokens.IconSize),
            tint = RecapBlue300,
        )
        Spacer(modifier = Modifier.height(OrganizeSuccessTokens.IconToTitleSpacing))
        Text(
            text = stringResource(R.string.organize_success_title, successCount),
            style = RecapHeading2,
            color = Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(OrganizeSuccessTokens.TitleToIllustrationSpacing))
        Image(
            painter = painterResource(R.drawable.recap_organize_success),
            contentDescription = stringResource(
                R.string.organize_success_illustration_content_description,
            ),
            modifier = Modifier
                .size(
                    width = OrganizeSuccessTokens.IllustrationWidth,
                    height = OrganizeSuccessTokens.IllustrationHeight,
                )
                .offset(x = (-9).dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(OrganizeSuccessTokens.IllustrationToDescriptionSpacing))
        Text(
            text = stringResource(R.string.organize_success_description),
            style = RecapBody1,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
}

private object OrganizeSuccessTokens {
    val HorizontalPadding = 24.dp
    val BottomPadding = 24.dp
    val IconSize = 40.dp
    val IllustrationWidth = 195.09.dp
    val IllustrationHeight = 177.43.dp
    val IconToTitleSpacing = 10.dp
    val TitleToIllustrationSpacing = 28.dp
    val IllustrationToDescriptionSpacing = 21.dp
}

@Preview(name = "Organize Success Screen", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OrganizeSuccessScreenPreview() {
    RECAPTheme {
        OrganizeSuccessScreen(
            successCount = 5,
            onDoneClick = {},
        )
    }
}

