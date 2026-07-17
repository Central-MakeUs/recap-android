package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.icon.RecapHazeFolderIcon
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingIllustrationSignal
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private val OnboardingBlue = Color(0xFF5C74FF)
private val OnboardingGray700 = Color(0xFF222B3C)
private val OnboardingGray300 = Color(0xFF99A0B0)
private val KakaoYellow = Color(0xFFFEE500)
private const val LandingBackgroundIconAlpha = 0.45f
private const val LandingTransitionMillis = 500
private val LandingIllustrationSize = 190.dp
private val LandingBubbleIllustrationGap = 24.dp
private val LandingBubbleLoginGap = 17.dp

@Composable
fun OnboardingLandingScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    showLoginImmediately: Boolean = false,
    isLoading: Boolean = false,
    showDebugEmailLogin: Boolean = false,
    illustrationSignalFlow: Flow<OnboardingIllustrationSignal> = emptyFlow(),
) {
    var showLogin by rememberSaveable { mutableStateOf(showLoginImmediately) }
    LaunchedEffect(showLoginImmediately) {
        if (showLoginImmediately) {
            showLogin = true
        } else {
            delay(900.milliseconds)
            showLogin = true
        }
    }

    var rootTopY by remember { mutableFloatStateOf(0f) }
    var illustrationBottomY by remember { mutableFloatStateOf(0f) }
    var loginLabelTopY by remember { mutableFloatStateOf(0f) }
    var bubbleHeight by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onGloballyPositioned { coordinates ->
                rootTopY = coordinates.positionInRoot().y
            },
    ) {
        val density = LocalDensity.current
        val contentWidth = minOf(maxWidth, 375.dp)
        val topSpace by animateDpAsState(
            targetValue = if (showLogin) 120.dp else 60.dp,
            animationSpec = tween(durationMillis = LandingTransitionMillis),
            label = "onboarding_landing_top_space",
        )
        val loginProgress by animateFloatAsState(
            targetValue = if (showLogin) 1f else 0f,
            animationSpec = tween(durationMillis = LandingTransitionMillis),
            label = "onboarding_landing_login_progress",
        )

        val bubbleIllustrationGapPx = with(density) { LandingBubbleIllustrationGap.toPx() }
        val bubbleLoginGapPx = with(density) { LandingBubbleLoginGap.toPx() }
        val fallbackIllustrationBottomY = with(density) {
            rootTopY + (maxHeight / 2 + LandingIllustrationSize / 2).toPx()
        }
        val fallbackLoginLabelTopY = with(density) {
            rootTopY + (maxHeight - 72.dp - 125.dp).toPx()
        }
        val resolvedIllustrationBottomY =
            if (illustrationBottomY > 0f) illustrationBottomY else fallbackIllustrationBottomY
        val resolvedLoginLabelTopY =
            if (loginLabelTopY > 0f) loginLabelTopY else fallbackLoginLabelTopY

        // 일러스트 아래(top 앵커) ↔ 로그인 라벨 위(bottom 앵커)를 loginProgress로 동시에 lerp.
        // 로그인 쪽은 bottom 고정이라 말풍선 높이 애니메이션과 위치가 서로 기다리지 않는다.
        val illustrationBubbleTopY =
            resolvedIllustrationBottomY + bubbleIllustrationGapPx - rootTopY
        val resolvedBubbleHeight = if (bubbleHeight > 0f) {
            bubbleHeight
        } else {
            with(density) { 48.dp.toPx() }
        }
        val loginBubbleTopY =
            resolvedLoginLabelTopY - bubbleLoginGapPx - resolvedBubbleHeight - rootTopY
        val bubbleTopY = lerp(illustrationBubbleTopY, loginBubbleTopY, loginProgress)

        LandingBackgroundIcons(
            screenHeight = maxHeight,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(contentWidth)
                .padding(top = topSpace),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandHeadline()
        }

        LandingIllustration(
            loginProgress = loginProgress,
            modifier = Modifier
                .align(Alignment.Center)
                .onGloballyPositioned { coordinates ->
                    illustrationBottomY =
                        coordinates.positionInRoot().y + coordinates.size.height
                },
        )

        SocialLoginSection(
            onKakaoClick = { onAction(OnboardingAction.LoginWithKakao) },
            onEmailClick = { onAction(OnboardingAction.LoginWithEmail) },
            isLoading = isLoading,
            showDebugEmailLogin = showDebugEmailLogin,
            interactive = showLogin,
            onLoginLabelPositioned = { topInRoot ->
                loginLabelTopY = topInRoot
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(contentWidth)
                .padding(bottom = 72.dp)
                .graphicsLayer { alpha = loginProgress },
        )

        RecapSpeechBubble(
            text = if (showLogin) {
                stringResource(R.string.onboarding_landing_start_chip)
            } else {
                stringResource(R.string.onboarding_landing_speech_bubble)
            },
            arrowDirection = if (showLogin) {
                RecapSpeechBubbleArrowDirection.Down
            } else {
                RecapSpeechBubbleArrowDirection.Up
            },
            animationDurationMillis = LandingTransitionMillis,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onGloballyPositioned { coordinates ->
                    bubbleHeight = coordinates.size.height.toFloat()
                }
                .offset { IntOffset(0, bubbleTopY.roundToInt()) },
        )
    }
}

@Composable
private fun LandingIllustration(
    loginProgress: Float,
    modifier: Modifier = Modifier,
) {
    RecapHazeFolderIcon(
        size = LandingIllustrationSize,
        contentDescription = stringResource(R.string.app_name),
        modifier = modifier.graphicsLayer {
            val shrink = 1f - (0.4f * loginProgress)
            scaleX = shrink
            scaleY = shrink
            alpha = 1f - loginProgress
        },
    )
}

/**
 * 랜딩 배경 장식 아이콘.
 * 우상단 → 좌중간 → 우중간 → 좌하단 순으로 배치하며, 콘텐츠 뒤에 깔린다.
 */
@Composable
private fun LandingBackgroundIcons(
    screenHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // 1. 우상단 — 문서
        Image(
            painter = painterResource(R.drawable.onboarding_background_1),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = screenHeight * 0.10f)
                .size(width = 58.dp, height = 67.dp)
                .graphicsLayer { alpha = LandingBackgroundIconAlpha },
        )
        // 2. 좌중간 — 카드 스택 (왼쪽 가장자리로 일부 잘림)
        Image(
            painter = painterResource(R.drawable.onboarding_background_2),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = screenHeight * 0.34f)
                .size(97.dp)
                .graphicsLayer { alpha = LandingBackgroundIconAlpha },
        )
        // 3. 우중간 — 돋보기 (오른쪽 가장자리로 일부 잘림)
        Image(
            painter = painterResource(R.drawable.onboarding_background_3),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = screenHeight * 0.52f)
                .size(75.dp)
                .graphicsLayer {
                    alpha = LandingBackgroundIconAlpha
                },
        )
        // 4. 좌하단 — 카메라
        Image(
            painter = painterResource(R.drawable.onboarding_background_4),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-screenHeight * 0.14f))
                .size(width = 57.dp, height = 50.dp)
                .graphicsLayer { alpha = LandingBackgroundIconAlpha },
        )
    }
}

@Composable
private fun BrandHeadline(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecapLogo(
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .width(154.dp)
                .aspectRatio(RecapLogoAspectRatio),
        )
        Text(
            text = buildAnnotatedString {
                pushStyle(SpanStyle(color = OnboardingBlue))
                append(stringResource(R.string.onboarding_splash_tagline_highlight))
                pop()
                append(" ")
                append(stringResource(R.string.onboarding_splash_tagline_rest))
            },
            style = MaterialTheme.typography.headlineMedium,
            color = OnboardingGray700,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SocialLoginSection(
    onKakaoClick: () -> Unit,
    onEmailClick: () -> Unit,
    isLoading: Boolean,
    showDebugEmailLogin: Boolean,
    interactive: Boolean,
    onLoginLabelPositioned: (topInRoot: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .onGloballyPositioned { coordinates ->
                        onLoginLabelPositioned(coordinates.positionInRoot().y)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DividerLine()
                Text(
                    text = stringResource(R.string.onboarding_simple_login_label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnboardingGray300,
                )
                DividerLine()
            }
            SocialLoginButton(
                onClick = onKakaoClick,
                enabled = interactive && !isLoading,
                containerColor = KakaoYellow,
                contentDescription = stringResource(R.string.onboarding_kakao_login_content_description),
                modifier = Modifier.padding(top = 58.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.kakao_96px),
                        contentDescription = null,
                        modifier = Modifier.size(29.dp),
                        tint = Color.Black,
                    )
                }
            }
        }
        if (showDebugEmailLogin) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.onboarding_email_login_button),
                style = MaterialTheme.typography.bodyLarge,
                color = OnboardingGray300,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(
                    enabled = interactive && !isLoading,
                    role = Role.Button,
                    onClick = onEmailClick,
                ),
            )
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .size(width = 59.dp, height = 1.dp)
            .background(Color(0xFFCED2DE)),
    )
}

@Composable
private fun SocialLoginButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(67.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        color = containerColor,
        contentColor = Color.Unspecified,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingLandingScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingLandingScreen(
            onAction = {},
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingLandingScreenLoginPreview() {
    OnboardingPreviewContainer {
        OnboardingLandingScreen(
            onAction = {},
            showLoginImmediately = true,
        )
    }
}
