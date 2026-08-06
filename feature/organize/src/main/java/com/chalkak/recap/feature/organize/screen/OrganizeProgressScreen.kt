package com.chalkak.recap.feature.organize.screen

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.chalkak.recap.core.data.notification.areAppNotificationsEnabled
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapCategoryOther500
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@Composable
fun OrganizeProgressScreen(
    progress: Float,
    onCancelClick: () -> Unit,
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
                .padding(horizontal = OrganizeProgressTokens.HorizontalPadding),
        ) {
            OrganizeProgressContent(
                progress = progress,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .offset(y = (-20).dp),
            )
            RecapButton(
                text = stringResource(R.string.organize_progress_cancel),
                onClick = onCancelClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = OrganizeProgressTokens.BottomPadding),
                colors = RecapButtonDefaults.secondaryColors(),
                contentPadding = PaddingValues(vertical = 15.dp),
            )
        }
    }
}

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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        RecapSpeechBubble(
            text = stringResource(R.string.organize_progress_speech_bubble),
            arrowDirection = RecapSpeechBubbleArrowDirection.Down,
        )
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.BubbleToIllustrationSpacing))
        Image(
            painter = painterResource(R.drawable.recap_organize_ongoing),
            contentDescription = stringResource(
                R.string.organize_progress_illustration_content_description,
            ),
            modifier = Modifier.size(
                width = OrganizeProgressTokens.IllustrationWidth,
                height = OrganizeProgressTokens.IllustrationHeight,
            ),
            contentScale = ContentScale.Fit,
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
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.ProgressToTitleSpacing))
        Text(
            text = stringResource(R.string.organize_progress_title),
            style = RecapHeading2,
            color = Black,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(OrganizeProgressTokens.TitleToDescriptionSpacing))
        Text(
            text = stringResource(descriptionRes),
            style = RecapBody1,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
    }
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
    val HorizontalPadding = 24.dp
    val BottomPadding = 24.dp
    val IllustrationWidth = 173.dp
    val IllustrationHeight = 161.dp
    val ProgressHeight = 6.dp
    val BubbleToIllustrationSpacing = 8.dp
    val IllustrationToProgressSpacing = 32.dp
    val ProgressToTitleSpacing = 29.dp
    val TitleToDescriptionSpacing = 6.dp
    const val ProgressAnimationDurationMillis = 500
}

@Preview(
    name = "Organize Progress · Notification On",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeProgressScreenNotificationOnPreview() {
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
private fun OrganizeProgressScreenNotificationOffPreview() {
    RECAPTheme {
        OrganizeProgressContent(
            progress = 0.65f,
            notificationsEnabled = false,
        )
    }
}

