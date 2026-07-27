package com.chalkak.recap.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.White

@Composable
fun RecapLottieSplashScreen(
    skipAnimation: Boolean,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val compositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.recap_splash))
    val composition = compositionResult.value
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = !skipAnimation && composition != null,
    )
    var completionReported by remember { mutableStateOf(false) }

    LaunchedEffect(compositionResult.isFailure) {
        if (compositionResult.isFailure && !completionReported) {
            completionReported = true
            onSplashFinished()
        }
    }

    LaunchedEffect(progress, composition, skipAnimation) {
        if (skipAnimation || composition == null || completionReported) {
            return@LaunchedEffect
        }
        if (progress >= 1f) {
            completionReported = true
            onSplashFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawRect(White) }
            .clearAndSetSemantics { },
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = {
                    if (skipAnimation) {
                        1f
                    } else {
                        progress
                    }
                },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecapLottieSplashScreenPreview() {
    RECAPTheme {
        RecapLottieSplashScreen(
            skipAnimation = true,
            onSplashFinished = {},
        )
    }
}
