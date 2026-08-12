package com.chalkak.recap.feature.organize.content

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chalkak.recap.core.data.notification.areAppNotificationsEnabled
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapCategoryOther500
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.feature.organize.R as OrganizeR

@Composable
fun OrganizeProgressContent(
    progress: Float,
    modifier: Modifier = Modifier,
    notificationsEnabled: Boolean? = null,
) {
    val resolvedNotificationsEnabled = notificationsEnabled
        ?: rememberAreAppNotificationsEnabled()
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = OrganizeProgressTokens.ProgressAnimationDurationMillis,
            easing = EaseInOut,
        ),
        label = "organize_progress",
    )
    val descriptionRes = if (resolvedNotificationsEnabled) {
        R.string.organize_progress_description
    } else {
        R.string.organize_progress_description_without_notification
    }
    val illustrationSize = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp() / 2f
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.organize_progress_title),
            style = RecapHeading2,
            color = RecapGray900,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.TitleToDescriptionSpacing))
        Text(
            text = stringResource(descriptionRes),
            style = RecapBody1,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.DescriptionToIllustrationSpacing))
        OrganizeProgressAnalyzingLottie(
            modifier = Modifier.size(illustrationSize),
        )
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.IllustrationToProgressSpacing))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(OrganizeProgressTokens.ProgressHeight),
            color = RecapOnboardingBlue,
            trackColor = RecapCategoryOther500, // 색상 토큰 정규화 필요
            strokeCap = StrokeCap.Round,
            gapSize = -OrganizeProgressTokens.ProgressHeight,
            drawStopIndicator = {},
        )
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.ProgressToBubbleSpacing))
        RecapSpeechBubble(
            text = stringResource(R.string.organize_progress_speech_bubble),
            arrowDirection = RecapSpeechBubbleArrowDirection.Up,
        )
    }
}

@Composable
private fun OrganizeProgressAnalyzingLottie(
    modifier: Modifier = Modifier,
) {
    val illustrationDescription = stringResource(
        R.string.organize_progress_illustration_content_description,
    )
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(OrganizeR.raw.analyzing),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.semantics { contentDescription = illustrationDescription },
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun rememberAreAppNotificationsEnabled(): Boolean {
    val context = LocalContext.current
    var notificationsEnabled by remember {
        mutableStateOf(context.areAppNotificationsEnabled())
    }
    LifecycleResumeEffect(Unit) {
        notificationsEnabled = context.areAppNotificationsEnabled()
        onPauseOrDispose { }
    }
    return notificationsEnabled
}

private object OrganizeProgressTokens {
    val ProgressHeight = 6.dp
    val TitleToDescriptionSpacing = 11.dp
    val DescriptionToIllustrationSpacing = 22.dp
    val IllustrationToProgressSpacing = 29.dp
    val ProgressToBubbleSpacing = 27.dp
    const val ProgressAnimationDurationMillis = 500
}

@Preview(
    name = "Organize Progress · Notification On",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeProgressContentNotificationOnPreview() {
    RECAPTheme {
        OrganizeProgressContent(
            progress = 0.65f,
            notificationsEnabled = true,
        )
    }
}

@Preview(
    name = "Organize Progress · Notification Off",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeProgressContentNotificationOffPreview() {
    RECAPTheme {
        OrganizeProgressContent(
            progress = 0.65f,
            notificationsEnabled = false,
        )
    }
}
