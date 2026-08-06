package com.chalkak.recap.feature.organize.content

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.feature.organize.R as OrganizeR
import kotlinx.coroutines.delay

@Composable
fun OrganizeSuccessBackgroundGradient(
    modifier: Modifier = Modifier,
) {
    val gradientAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        gradientAlpha.animateTo(
            targetValue = OrganizeSuccessTokens.BackgroundGradientAlpha,
            animationSpec = tween(
                durationMillis = OrganizeSuccessTokens.BackgroundGradientFadeDurationMillis,
            ),
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(OrganizeSuccessTokens.BackgroundGradientHeightFraction)
            .background(
                brush = Brush.verticalGradient(
                    0f to OrganizeSuccessTokens.BackgroundGradientColor.copy(alpha = 0f),
                    1f to OrganizeSuccessTokens.BackgroundGradientColor.copy(
                        alpha = gradientAlpha.value,
                    ),
                ),
            ),
    )
}

@Composable
fun OrganizeSuccessContent(
    successCount: Int,
    modifier: Modifier = Modifier,
    checkLottiePlayDelayMillis: Long = OrganizeSuccessTokens.CheckLottiePlayDelayMillis,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OrganizeSuccessCheckLottie(
            playDelayMillis = checkLottiePlayDelayMillis,
            modifier = Modifier.size(OrganizeSuccessTokens.IconSize),
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

@Composable
private fun OrganizeSuccessCheckLottie(
    playDelayMillis: Long,
    modifier: Modifier = Modifier,
) {
    val iconDescription = stringResource(
        R.string.organize_success_icon_content_description,
    )
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(OrganizeR.raw.complete_check),
    )
    var isPlaying by remember(playDelayMillis) { mutableStateOf(playDelayMillis <= 0L) }
    LaunchedEffect(composition, playDelayMillis) {
        if (composition == null || playDelayMillis <= 0L) {
            return@LaunchedEffect
        }
        delay(playDelayMillis)
        isPlaying = true
    }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = isPlaying && composition != null,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.semantics { contentDescription = iconDescription },
        contentScale = ContentScale.Fit,
    )
}

private object OrganizeSuccessTokens {
    val IconSize = 45.dp
    val IllustrationWidth = 195.09.dp
    val IllustrationHeight = 177.43.dp
    val IconToTitleSpacing = 10.dp
    val TitleToIllustrationSpacing = 28.dp
    val IllustrationToDescriptionSpacing = 21.dp
    const val CheckLottiePlayDelayMillis = 200L
    const val BackgroundGradientHeightFraction = 0.54f
    const val BackgroundGradientAlpha = 0.4f
    const val BackgroundGradientFadeDurationMillis = 750
    val BackgroundGradientColor = Color(0xFF8FA4FF)
}

@Preview(name = "Organize Success Content", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OrganizeSuccessContentPreview() {
    RECAPTheme {
        OrganizeSuccessContent(successCount = 5)
    }
}
