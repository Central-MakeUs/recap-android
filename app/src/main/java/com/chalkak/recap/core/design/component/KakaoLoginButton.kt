package com.chalkak.recap.core.design.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun KakaoLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        color = KakaoYellow,
        contentColor = KakaoLogoColor,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            val textStyle = MaterialTheme.typography.titleMedium
            val textMeasurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val fullText = stringResource(R.string.onboarding_kakao_login_full)
            val shortText = stringResource(R.string.onboarding_kakao_login_short)
            val fullTextWidth = with(density) {
                textMeasurer.measure(
                    text = fullText,
                    style = textStyle,
                ).size.width.toDp()
            }
            val logoEnd = KakaoLogoStartPadding + KakaoLogoSize
            val fullTextStart = (maxWidth - fullTextWidth) / 2
            val loginText = if (fullTextStart < logoEnd) {
                shortText
            } else {
                fullText
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kakao_96px),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = KakaoLogoStartPadding)
                        .size(KakaoLogoSize),
                )
                Text(
                    text = loginText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = logoEnd),
                    color = KakaoTextColor,
                    style = textStyle,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private val KakaoYellow = Color(0xFFFEE500)
private val KakaoLogoColor = Color(0xFF000000)
private val KakaoTextColor = Color(0xD9000000)
private val KakaoLogoStartPadding = 28.dp
private val KakaoLogoSize = 24.dp

@Preview(name = "Kakao Login Button", showBackground = true, widthDp = 360)
@Composable
private fun KakaoLoginButtonPreview1() {
    RECAPTheme(dynamicColor = false) {
        KakaoLoginButton(
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Kakao Login Button", showBackground = true, widthDp = 240)
@Composable
private fun KakaoLoginButtonPreview2() {
    RECAPTheme(dynamicColor = false) {
        KakaoLoginButton(
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}


@Preview(name = "Kakao Login Button", showBackground = true, widthDp = 480)
@Composable
private fun KakaoLoginButtonPreview3() {
    RECAPTheme(dynamicColor = false) {
        KakaoLoginButton(
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
