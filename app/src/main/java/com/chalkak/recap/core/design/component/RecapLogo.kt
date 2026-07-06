package com.chalkak.recap.core.design.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun RecapLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(R.drawable.ic_recap_logo),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(name = "Recap Logo", showBackground = true)
@Composable
private fun RecapLogoPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapLogo(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(RecapLogoAspectRatio),
        )
    }
}

const val RecapLogoAspectRatio: Float = 1106f / 338f
