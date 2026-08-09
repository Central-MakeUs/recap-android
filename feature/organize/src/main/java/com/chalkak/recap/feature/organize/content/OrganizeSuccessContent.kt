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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
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
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.Spread
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.core.models.Shape
import io.github.vinceglb.confettikit.core.models.Size
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import com.chalkak.recap.feature.organize.R as OrganizeR

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
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var checkIconCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var confettiOrigin by remember { mutableStateOf<Offset?>(null) }

    fun resolveConfettiOrigin() {
        if (confettiOrigin != null) {
            return
        }
        val root = rootCoordinates ?: return
        val icon = checkIconCoordinates ?: return
        if (!root.isAttached || !icon.isAttached) {
            return
        }
        confettiOrigin = root.localPositionOf(
            sourceCoordinates = icon,
            relativeToSource = Offset(
                x = icon.size.width / 2f,
                y = icon.size.height / 2f,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rootCoordinates = coordinates
                resolveConfettiOrigin()
            },
    ) {
        confettiOrigin?.let { origin ->
            OrganizeSuccessConfetti(
                origin = origin,
                playDelayMillis = OrganizeSuccessTokens.ConfettiPlayDelayMillis,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .offset(y = OrganizeSuccessTokens.ContentOffsetY),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            OrganizeSuccessCheckLottie(
                playDelayMillis = checkLottiePlayDelayMillis,
                modifier = Modifier
                    .size(OrganizeSuccessTokens.IconSize)
                    .onGloballyPositioned { iconCoordinates ->
                        checkIconCoordinates = iconCoordinates
                        resolveConfettiOrigin()
                    },
            )
            Spacer(modifier = Modifier.height(OrganizeSuccessTokens.IconToTitleSpacing))
            Text(
                text = stringResource(R.string.organize_success_title, successCount),
                style = RecapHeading2,
                color = Black,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(OrganizeSuccessTokens.TitleToIllustrationSpacing))
            OrganizeSuccessCharacterImage(
                modifier = Modifier.size(
                    width = OrganizeSuccessTokens.IllustrationWidth,
                    height = OrganizeSuccessTokens.IllustrationHeight,
                ),
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
}

@Composable
private fun OrganizeSuccessConfetti(
    origin: Offset,
    playDelayMillis: Long,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val parties = remember(origin, playDelayMillis, density) {
        val originOffsetX = with(density) {
            OrganizeSuccessTokens.ConfettiOriginOffsetX.toPx()
        }
        val originOffsetY = with(density) {
            OrganizeSuccessTokens.ConfettiOriginOffsetY.toPx()
        }
        listOf(
            Party(
                speed = 0f,
                maxSpeed = OrganizeSuccessTokens.ConfettiMaxSpeed,
                damping = OrganizeSuccessTokens.ConfettiDamping,
                spread = Spread.ROUND,
                colors = OrganizeSuccessTokens.ConfettiColors,
                shapes = listOf(Shape.Square, Shape.Circle),
                size = listOf(Size.SMALL, Size.MEDIUM),
                timeToLive = OrganizeSuccessTokens.ConfettiTimeToLiveMillis,
                fadeOutEnabled = true,
                position = Position.Absolute(
                    x = origin.x + originOffsetX,
                    y = origin.y + originOffsetY,
                ),
                delay = playDelayMillis.toInt().coerceAtLeast(0),
                emitter = Emitter(duration = OrganizeSuccessTokens.ConfettiBurstDuration)
                    .max(OrganizeSuccessTokens.ConfettiParticleCount),
            ),
        )
    }
    ConfettiKit(
        modifier = modifier,
        parties = parties,
    )
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
        delay(playDelayMillis.milliseconds)
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

@Composable
private fun OrganizeSuccessCharacterImage(
    modifier: Modifier = Modifier,
) {
    val illustrationDescription = stringResource(
        R.string.organize_success_illustration_content_description,
    )
    Image(
        painter = painterResource(R.drawable.recap_organize_success),
        contentDescription = illustrationDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

private object OrganizeSuccessTokens {
    val IconSize = 45.dp
    val IllustrationWidth = 195.dp
    val IllustrationHeight = 120.dp
    val IconToTitleSpacing = 10.dp
    val TitleToIllustrationSpacing = 44.dp
    val IllustrationToDescriptionSpacing = 27.dp
    val ContentOffsetY = (-44).dp
    const val CheckLottiePlayDelayMillis = 200L
    const val ConfettiPlayDelayMillis = 420L
    const val BackgroundGradientHeightFraction = 0.54f
    const val BackgroundGradientAlpha = 0.4f
    const val BackgroundGradientFadeDurationMillis = 750
    val BackgroundGradientColor = Color(0xFF8FA4FF)
    const val ConfettiMaxSpeed = 36f
    const val ConfettiDamping = 0.9f
    const val ConfettiTimeToLiveMillis = 2_000L
    const val ConfettiParticleCount = 80
    val ConfettiOriginOffsetX = 0.dp
    val ConfettiOriginOffsetY = 0.dp
    val ConfettiBurstDuration = 120.milliseconds
    val ConfettiColors = listOf(
        RecapBlue50.toArgb(),
        RecapBlue300.toArgb(),
        RecapBlue500.toArgb(),
    )
}

@Preview(name = "Organize Success Content", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OrganizeSuccessContentPreview() {
    RECAPTheme {
        OrganizeSuccessContent(successCount = 5)
    }
}
