package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.core.design.component.image.RecapPinchZoomAsyncImage
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapImagePlaceholderBackground

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScreenshotFullscreenScreen(
    imageModel: Any?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    var imageLoadFailed by remember(imageModel) { mutableStateOf(false) }
    val showPlaceholder = imageModel == null || imageLoadFailed
    val imageShape = RoundedCornerShape(ScreenshotSharedImageCornerRadius)
    val sharedBoundsModifier = if (showPlaceholder) {
        Modifier
    } else {
        Modifier.screenshotSharedImageBounds(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
    // Hide destination Fit frame for the pre-match frame, then keep Crop until the
    // shared transition finishes so the resting Fit layout never flashes early.
    var hasCompletedSharedEntry by remember {
        mutableStateOf(sharedTransitionScope == null)
    }
    val isSharedTransitionActive = sharedTransitionScope?.isTransitionActive == true
    LaunchedEffect(isSharedTransitionActive) {
        if (!isSharedTransitionActive && sharedTransitionScope != null) {
            hasCompletedSharedEntry = true
        }
    }
    val imageAlpha =
        if (sharedTransitionScope != null &&
            !isSharedTransitionActive &&
            !hasCompletedSharedEntry
        ) {
            0f
        } else {
            1f
        }
    val imageContentScale =
        if (!hasCompletedSharedEntry || isSharedTransitionActive) {
            ContentScale.Crop
        } else {
            ContentScale.Fit
        }
    // Image dropShadow lives outside sharedBounds; keep it in parallel with morph.
    val edgeChromeTargetAlpha = when {
        sharedTransitionScope == null -> 1f
        !hasCompletedSharedEntry && !isSharedTransitionActive -> 0f
        isSharedTransitionActive && hasCompletedSharedEntry -> 0f
        else -> 1f
    }
    val edgeChromeProgress by animateFloatAsState(
        targetValue = edgeChromeTargetAlpha,
        animationSpec = tween(
            durationMillis = RecapNavigationMotion.SlideDurationMillis,
            easing = FastOutSlowInEasing,
        ),
        label = "screenshotFullscreenEdgeChrome",
    )
    val imageDropShadow = Shadow(
        radius = ScreenshotFullscreenTokens.ImageShadowBlurRadius,
        color = Black.copy(
            alpha = ScreenshotFullscreenTokens.ImageShadowAlpha * edgeChromeProgress,
        ),
    )
    val topGradientChromeModifier =
        Modifier.screenshotFullscreenTopGradientChrome(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                if (showPlaceholder) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = ScreenshotFullscreenTokens.ImageEdgePadding,
                                top = ScreenshotFullscreenTokens.TopBarHeight,
                                end = ScreenshotFullscreenTokens.ImageEdgePadding,
                                bottom = ScreenshotFullscreenTokens.ImageEdgePadding,
                            )
                            .dropShadow(shape = imageShape, shadow = imageDropShadow),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(imageShape)
                                .background(RecapImagePlaceholderBackground)
                                .border(
                                    width = ScreenshotFullscreenTokens.ImageBorderWidth,
                                    color = RecapGray100,
                                    shape = imageShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.recap_placeholder_1),
                                contentDescription = stringResource(
                                    R.string.screenshot_image_placeholder_content_description,
                                ),
                                modifier = Modifier.size(width = 24.dp, height = 21.dp),
                            )
                        }
                    }
                } else {
                    RecapPinchZoomAsyncImage(
                        model = imageModel,
                        contentDescription = stringResource(
                            R.string.screenshot_image_placeholder_content_description,
                        ),
                        modifier = Modifier.graphicsLayer { alpha = imageAlpha },
                        contentPadding = PaddingValues(
                            start = ScreenshotFullscreenTokens.ImageEdgePadding,
                            top = ScreenshotFullscreenTokens.TopBarHeight,
                            end = ScreenshotFullscreenTokens.ImageEdgePadding,
                            bottom = ScreenshotFullscreenTokens.ImageEdgePadding,
                        ),
                        shape = imageShape,
                        borderWidth = ScreenshotFullscreenTokens.ImageBorderWidth,
                        borderColor = RecapGray100,
                        dropShadow = imageDropShadow,
                        imageFrameModifier = sharedBoundsModifier,
                        contentScale = imageContentScale,
                        onError = { imageLoadFailed = true },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(ScreenshotFullscreenTokens.TopGradientHeightFraction)
                    .align(Alignment.TopCenter)
                    .then(topGradientChromeModifier)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Black.copy(
                                    alpha = ScreenshotFullscreenTokens.TopGradientStartAlpha,
                                ),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            ScreenshotFullscreenTopBar(onNavigateBack = onNavigateBack)
        }
    }
}

/**
 * Lift the top vignette above the shared-image overlay and fade it with the
 * destination enter/exit progress (otherwise it stays hidden under the morph
 * and only appears after the shared transition ends).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.screenshotFullscreenTopGradientChrome(
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    if (sharedTransitionScope == null || animatedVisibilityScope == null) return this
    val fadeSpec = tween<Float>(
        durationMillis = RecapNavigationMotion.SlideDurationMillis,
        easing = FastOutSlowInEasing,
    )
    return with(sharedTransitionScope) {
        with(animatedVisibilityScope) {
            this@screenshotFullscreenTopGradientChrome
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                .animateEnterExit(
                    enter = fadeIn(animationSpec = fadeSpec),
                    exit = fadeOut(animationSpec = fadeSpec),
                )
        }
    }
}

@Composable
private fun ScreenshotFullscreenTopBar(
    onNavigateBack: () -> Unit,
) {
    val closeInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(ScreenshotFullscreenTokens.TopBarHeight),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(ScreenshotFullscreenTokens.TopBarPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = stringResource(
                    R.string.screenshot_fullscreen_close_content_description,
                ),
                tint = RecapGray900,
                modifier = Modifier
                    .size(ScreenshotFullscreenTokens.TopBarIconSize)
                    .clickable(
                        interactionSource = closeInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onNavigateBack,
                    ),
            )
        }
    }
}

private object ScreenshotFullscreenTokens {
    val TopBarHeight = 60.dp
    val TopBarPadding = 16.dp
    val TopBarIconSize = 24.dp
    val ImageBorderWidth = 0.5.dp
    val ImageEdgePadding = 30.dp
    val ImageShadowBlurRadius = 16.dp
    const val ImageShadowAlpha = 0.13f
    const val TopGradientHeightFraction = 0.15f
    const val TopGradientStartAlpha = 0.4f
}

@Preview(name = "Screenshot Fullscreen", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScreenshotFullscreenScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotFullscreenScreen(
            imageModel = null,
            onNavigateBack = {},
        )
    }
}
