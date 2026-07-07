package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingIllustrationSignal
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.OnboardingIllustration
import com.chalkak.recap.feature.onboarding.component.OnboardingIllustrationVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private val OnboardingBlue = Color(0xFF5C74FF)
private val OnboardingGray700 = Color(0xFF222B3C)
private val OnboardingGray500 = Color(0xFF4D586C)
private val OnboardingGray300 = Color(0xFF99A0B0)
private val KakaoYellow = Color(0xFFFEE500)
private val AppleBlack = Color(0xFF0B111D)

@Composable
fun OnboardingLandingScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
    showLoginImmediately: Boolean = false,
    illustrationSignalFlow: Flow<OnboardingIllustrationSignal> = emptyFlow(),
) {
    var showLogin by rememberSaveable { mutableStateOf(showLoginImmediately) }
    LaunchedEffect(showLoginImmediately) {
        if (showLoginImmediately) {
            showLogin = true
        } else {
            delay(900)
            showLogin = true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val contentWidth = minOf(maxWidth, 375.dp)
        val topSpace by animateDpAsState(
            targetValue = if (showLogin) 80.dp else 108.dp,
            animationSpec = tween(durationMillis = 500),
            label = "onboarding_landing_top_space",
        )
        val headlineIllustrationSpace by animateDpAsState(
            targetValue = if (showLogin) 24.dp else 55.dp,
            animationSpec = tween(durationMillis = 500),
            label = "onboarding_landing_headline_illustration_space",
        )
        val illustrationDescriptionSpace by animateDpAsState(
            targetValue = if (showLogin) 16.dp else 74.dp,
            animationSpec = tween(durationMillis = 500),
            label = "onboarding_landing_illustration_description_space",
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(contentWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(topSpace))
            BrandHeadline()
            Spacer(modifier = Modifier.height(headlineIllustrationSpace))
            OnboardingIllustration(
                signalFlow = illustrationSignalFlow,
                variant = OnboardingIllustrationVariant.Landing,
            )
            Spacer(modifier = Modifier.height(illustrationDescriptionSpace))
            Text(
                text = stringResource(R.string.onboarding_splash_description),
                style = MaterialTheme.typography.bodyLarge,
                color = OnboardingGray500,
                textAlign = TextAlign.Center,
            )
        }
        AnimatedVisibility(
            visible = showLogin,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp),
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(animationSpec = tween(300)) { it / 3 },
        ) {
            SocialLoginSection(
                onKakaoClick = { onAction(OnboardingAction.LoginWithKakao) },
                onAppleClick = { onAction(OnboardingAction.LoginWithApple) },
                modifier = Modifier.width(contentWidth),
            )
        }
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
    onAppleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
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
        Row(
            modifier = Modifier.padding(top = 58.dp),
            horizontalArrangement = Arrangement.spacedBy(27.dp),
        ) {
            SocialLoginButton(
                onClick = onKakaoClick,
                containerColor = KakaoYellow,
                contentDescription = stringResource(R.string.onboarding_kakao_login_content_description),
            ) {
                Icon(
                    painter = painterResource(R.drawable.kakao_96px),
                    contentDescription = null,
                    modifier = Modifier.size(29.dp),
                    tint = Color.Black,
                )
            }
            SocialLoginButton(
                onClick = onAppleClick,
                containerColor = AppleBlack,
                contentDescription = stringResource(R.string.onboarding_apple_login_content_description),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_apple_24),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White,
                )
            }
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
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
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
