package com.chalkak.recap.feature.onboarding.screen

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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.RecapLegalUrls
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapKakaoYellow
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingIllustrationSignal
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.OnboardingLayoutDefaults
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val LandingBackgroundIconAlpha = 0.45f
private val LandingHeadlineTopPadding = 120.dp
private val LandingBubbleKakaoGap = 90.dp
private val LandingBottomPadding = 30.dp
private val LandingLoginLegalGap = 28.dp
private val LandingLegalNoticeEstimatedHeight = 40.dp
private val LandingKakaoButtonSize = 67.dp
private val LandingKakaoButtonTopPadding = 58.dp

@Composable
fun OnboardingLandingScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    illustrationSignalFlow: Flow<OnboardingIllustrationSignal> = emptyFlow(),
) {
    var rootTopY by remember { mutableFloatStateOf(0f) }
    var kakaoButtonTopY by remember { mutableFloatStateOf(0f) }
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
        val bubbleKakaoGapPx = with(density) { LandingBubbleKakaoGap.toPx() }
        val fallbackKakaoButtonTopY = with(density) {
            rootTopY + (
                    maxHeight -
                            LandingBottomPadding -
                            LandingLegalNoticeEstimatedHeight -
                            LandingLoginLegalGap -
                            LandingKakaoButtonSize
                    ).toPx()
        }
        val resolvedKakaoButtonTopY =
            if (kakaoButtonTopY > 0f) kakaoButtonTopY else fallbackKakaoButtonTopY
        val resolvedBubbleHeight = if (bubbleHeight > 0f) {
            bubbleHeight
        } else {
            with(density) { 48.dp.toPx() }
        }
        val bubbleTopY =
            resolvedKakaoButtonTopY - bubbleKakaoGapPx - resolvedBubbleHeight - rootTopY

        LandingBackgroundIcons(
            screenHeight = maxHeight,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(contentWidth)
                .padding(top = LandingHeadlineTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandHeadline()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .width(contentWidth)
                .padding(bottom = LandingBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SocialLoginSection(
                onKakaoClick = { onAction(OnboardingAction.LoginWithKakao) },
                isLoading = isLoading,
                onKakaoButtonPositioned = { topInRoot ->
                    kakaoButtonTopY = topInRoot
                },
            )
            Spacer(modifier = Modifier.height(LandingLoginLegalGap))
            val uriHandler = LocalUriHandler.current
            LandingLegalNotice(
                onTermsClick = { uriHandler.openUri(RecapLegalUrls.TERMS_OF_SERVICE) },
                onPrivacyClick = { uriHandler.openUri(RecapLegalUrls.PRIVACY_POLICY) },
            )
        }

        RecapSpeechBubble(
            text = stringResource(R.string.onboarding_landing_start_chip),
            arrowDirection = RecapSpeechBubbleArrowDirection.Down,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onGloballyPositioned { coordinates ->
                    bubbleHeight = coordinates.size.height.toFloat()
                }
                .offset { IntOffset(0, bubbleTopY.roundToInt()) },
        )
    }
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
            colorFilter = ColorFilter.tint(RecapBlue50),
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
            colorFilter = ColorFilter.tint(RecapBlue50),
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
            colorFilter = ColorFilter.tint(RecapBlue50),
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
            colorFilter = ColorFilter.tint(RecapBlue50),
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
                pushStyle(SpanStyle(color = RecapOnboardingBlue))
                append(stringResource(R.string.onboarding_splash_tagline_highlight))
                pop()
                append(" ")
                append(stringResource(R.string.onboarding_splash_tagline_rest))
            },
            style = RecapHeading2,
            color = RecapGray700,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SocialLoginSection(
    onKakaoClick: () -> Unit,
    isLoading: Boolean,
    onKakaoButtonPositioned: (topInRoot: Float) -> Unit,
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
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DividerLine()
                Text(
                    text = stringResource(R.string.onboarding_simple_login_label),
                    style = RecapCaption1,
                    color = RecapGray500,
                )
                DividerLine()
            }
            SocialLoginButton(
                onClick = onKakaoClick,
                enabled = !isLoading,
                containerColor = RecapKakaoYellow,
                contentDescription = stringResource(R.string.onboarding_kakao_login_content_description),
                modifier = Modifier
                    .padding(top = LandingKakaoButtonTopPadding)
                    .onGloballyPositioned { coordinates ->
                        onKakaoButtonPositioned(coordinates.positionInRoot().y)
                    },
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Black,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.kakao_96px),
                        contentDescription = null,
                        modifier = Modifier.size(29.dp),
                        tint = Black,
                    )
                }
            }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .size(width = 68.dp, height = 1.dp)
            .background(RecapGray200),
    )
}

@Composable
private fun LandingLegalNotice(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_landing_terms),
                style = RecapCaption1,
                color = RecapGray500,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = onTermsClick,
                ),
            )
            VerticalDivider(
                modifier = Modifier.height(14.dp),
                thickness = 1.dp,
                color = RecapGray200,
            )
            Text(
                text = stringResource(R.string.onboarding_landing_privacy),
                style = RecapCaption1,
                color = RecapGray500,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = onPrivacyClick,
                ),
            )
        }
        Text(
            text = stringResource(R.string.onboarding_landing_legal_agreement_notice),
            style = RecapCaption2,
            color = RecapGray300,
            textAlign = TextAlign.Center,
        )
    }
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
            .size(LandingKakaoButtonSize)
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
    OnboardingPreviewContainer(
        contentPadding = OnboardingLayoutDefaults.LandingScreenPadding,
    ) {
        OnboardingLandingScreen(
            onAction = {},
        )
    }
}
