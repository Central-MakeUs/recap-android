package com.chalkak.recap.feature.organize.screen

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
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@Composable
fun UnsupportedShareScreen(
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
                .padding(horizontal = UnsupportedShareTokens.HorizontalPadding),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .offset(y = -144.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_error_circle_24),
                    contentDescription = stringResource(
                        R.string.share_unsupported_icon_content_description,
                    ),
                    modifier = Modifier.size(UnsupportedShareTokens.IconSize),
                    tint = RecapBlue300,
                )
                Spacer(modifier = Modifier.height(UnsupportedShareTokens.IconToTitleSpacing))
                Text(
                    text = stringResource(R.string.share_unsupported_title),
                    style = RecapHeading2,
                    color = Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(UnsupportedShareTokens.TitleToDescriptionSpacing))
                Text(
                    text = stringResource(R.string.share_unsupported_description),
                    style = RecapBody2,
                    color = RecapGray500,
                    textAlign = TextAlign.Center,
                )
            }
            RecapButton(
                text = stringResource(R.string.share_unsupported_close),
                onClick = onCloseClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = UnsupportedShareTokens.BottomPadding),
                contentPadding = PaddingValues(vertical = 15.dp)
            )
        }
    }
}

private object UnsupportedShareTokens {
    val HorizontalPadding = 24.dp
    val BottomPadding = 24.dp
    val IconSize = 40.dp
    val IconToTitleSpacing = 10.dp
    val TitleToDescriptionSpacing = 28.dp
}

@Preview(name = "Unsupported Share Screen", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun UnsupportedShareScreenPreview() {
    RECAPTheme {
        UnsupportedShareScreen(onCloseClick = {})
    }
}
