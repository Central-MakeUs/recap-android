package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.progress.RecapStepProgressIndicator
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubble
import com.chalkak.recap.core.design.component.speechbubble.RecapSpeechBubbleArrowDirection
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val GuideStepImages = listOf(
    R.drawable.onboarding_add_to_favorite_guide_1,
    R.drawable.onboarding_add_to_favorite_guide_2,
    R.drawable.onboarding_add_to_favorite_guide_3,
    R.drawable.onboarding_add_to_favorite_guide_4,
)

private val GuideStepDescriptions = listOf(
    R.string.onboarding_add_to_favorite_guide_step_1,
    R.string.onboarding_add_to_favorite_guide_step_2,
    R.string.onboarding_add_to_favorite_guide_step_3,
    R.string.onboarding_add_to_favorite_guide_step_4,
)

private val GuideStepIcons = listOf("❶", "❷", "❸", "❹")

private data class GuideTouchHighlightSpec(
    val xFraction: Float,
    val yFraction: Float,
    val bubbleTextResId: Int,
)

private val GuideTouchHighlights: List<GuideTouchHighlightSpec?> = listOf(
    GuideTouchHighlightSpec(0.835f, 0.895f, R.string.onboarding_add_to_favorite_guide_touch_1),
    GuideTouchHighlightSpec(0.853f, 0.093f, R.string.onboarding_add_to_favorite_guide_touch_2),
    GuideTouchHighlightSpec(0.146f, 0.500f, R.string.onboarding_add_to_favorite_guide_touch_3),
    null,
)

private val GuideContentHorizontalPadding = 28.dp
private val GuideImageCornerRadius = 20.dp
private val GuideTouchHoleSize = 89.dp
private const val GuideOverlayAlpha = 0.3f
/** 말풍선 높이의 이 비율만큼 원과 overlap 시킨다. */
private const val GuideBubbleOverlapFraction = 0.3f
/** 하단 타겟은 원 아래 공간이 없어 말풍선을 위로 올린다. */
private const val GuideBubbleBelowMaxYFraction = 0.85f
private const val GuidePagerPageWidthFraction = 0.75f
/** 말풍선이 가이드 이미지 밖으로 나갈 수 있는 최대 비율(화면–이미지 좌우 여백 대비). */
private const val GuideBubbleHorizontalOverflowFraction = 0.5f
private const val GuideImageAspectRatio = 3f / 4f

@Composable
fun OnboardingAddToFavoriteGuideScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
) {
    val pageCount = GuideStepImages.size
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, pageCount - 1),
        pageCount = { pageCount },
    )
    val progress = pagerState.currentPage + pagerState.currentPageOffsetFraction

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            RecapTopBar(
                title = stringResource(R.string.onboarding_add_to_favorite_guide_title),
                onBackClick = onBackClick,
                backButtonContentDescription = stringResource(R.string.settings_back_content_description),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                AddToFavoriteGuideCarousel(
                    pagerState = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                )
                AddToFavoriteGuideStepDescription(
                    pagerState = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GuideContentHorizontalPadding)
                        .padding(top = 18.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                RecapStepProgressIndicator(
                    progress = progress,
                    stepCount = pageCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GuideContentHorizontalPadding),
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AddToFavoriteGuideCarousel(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val pageCount = pagerState.pageCount
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    fun animateToPage(page: Int) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(page.coerceIn(0, pageCount - 1))
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val sidePadding = maxWidth * ((1f - GuidePagerPageWidthFraction) / 2f)
        val bubbleHorizontalOverflow = sidePadding * GuideBubbleHorizontalOverflowFraction
        val pageSpacing = maxWidth * (1f - GuidePagerPageWidthFraction)
        val pageWidth = maxWidth * GuidePagerPageWidthFraction
        val pagerHeight = pageWidth / GuideImageAspectRatio

        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerHeight),
                contentPadding = PaddingValues(horizontal = sidePadding),
                pageSpacing = pageSpacing,
            ) { page ->
                // 페이지 Box는 clip하지 않아 SpeechBubble이 이미지 라운드 밖으로 나갈 수 있다.
                // 라운드 clip은 Image / 딤 오버레이에만 적용한다.
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(GuideStepImages[page]),
                        contentDescription = stringResource(
                            R.string.onboarding_add_to_favorite_guide_step_image_content_description,
                            page + 1,
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(GuideImageCornerRadius))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop,
                    )
                    GuideTouchHighlights.getOrNull(page)?.let { highlight ->
                        GuideTouchHighlight(
                            xFraction = highlight.xFraction,
                            yFraction = highlight.yFraction,
                            bubbleText = stringResource(highlight.bubbleTextResId),
                            horizontalOverflow = bubbleHorizontalOverflow,
                        )
                    }
                }
            }
            GuideChevronButton(
                iconResId = R.drawable.ic_chevron_left_24,
                contentDescription = stringResource(
                    R.string.onboarding_add_to_favorite_guide_previous_content_description
                ),
                visible = currentPage > 0,
                onClick = { animateToPage(currentPage - 1) },
                modifier = Modifier.align(Alignment.CenterStart),
            )
            GuideChevronButton(
                iconResId = R.drawable.ic_chevron_right_24,
                contentDescription = stringResource(
                    R.string.onboarding_add_to_favorite_guide_next_content_description
                ),
                visible = currentPage < pageCount - 1,
                onClick = { animateToPage(currentPage + 1) },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun AddToFavoriteGuideStepDescription(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val currentPage = pagerState.currentPage
    val descriptionAlpha =
        (1f - abs(pagerState.currentPageOffsetFraction) * 2f).coerceIn(0f, 1f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        GuideStepDescriptions.forEachIndexed { index, descriptionResId ->
            val alpha = if (index == currentPage) descriptionAlpha else 0f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { this.alpha = alpha },
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = GuideStepIcons[index],
                    style = RecapBody1,
                    color = RecapGray700,
                )
                Text(
                    text = stringResource(descriptionResId),
                    modifier = Modifier.padding(start = 8.dp),
                    style = RecapBody1,
                    color = RecapGray700,
                )
            }
        }
    }
}

@Composable
private fun GuideTouchHighlight(
    xFraction: Float,
    yFraction: Float,
    bubbleText: String,
    horizontalOverflow: Dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val holeDiameterPx = with(density) { GuideTouchHoleSize.toPx() }
        val horizontalOverflowPx = with(density) { horizontalOverflow.toPx() }
        val placeBubbleBelow = yFraction <= GuideBubbleBelowMaxYFraction
        var bubbleSize by remember { mutableStateOf(IntSize.Zero) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(GuideImageCornerRadius)),
        ) {
            val holeRadius = holeDiameterPx / 2f
            val center = Offset(
                x = size.width * xFraction,
                y = size.height * yFraction,
            )
            val overlayPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(Offset.Zero, size))
                addOval(Rect(center = center, radius = holeRadius))
            }
            drawPath(
                path = overlayPath,
                color = Black.copy(alpha = GuideOverlayAlpha),
            )
        }

        RecapSpeechBubble(
            text = bubbleText,
            arrowDirection = RecapSpeechBubbleArrowDirection.None,
            floatingEnabled = false,
            modifier = Modifier
                .onSizeChanged { bubbleSize = it }
                .offset {
                    val holeCenterX = constraints.maxWidth * xFraction
                    val holeCenterY = constraints.maxHeight * yFraction
                    val holeRadius = holeDiameterPx / 2f
                    val overlapPx = bubbleSize.height * GuideBubbleOverlapFraction
                    val preferredX = holeCenterX - bubbleSize.width / 2f
                    val minX = -horizontalOverflowPx
                    val maxX = (
                        (constraints.maxWidth - bubbleSize.width).toFloat() + horizontalOverflowPx
                    ).coerceAtLeast(minX)
                    val x = preferredX.coerceIn(minX, maxX).roundToInt()
                    val y = if (placeBubbleBelow) {
                        (holeCenterY + holeRadius - overlapPx).roundToInt()
                    } else {
                        (holeCenterY - holeRadius - bubbleSize.height + overlapPx).roundToInt()
                    }
                    IntOffset(x = x, y = y)
                },
        )
    }
}

@Composable
private fun GuideChevronButton(
    iconResId: Int,
    contentDescription: String,
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "guideChevronAlpha",
    )

    IconButton(
        onClick = onClick,
        enabled = visible,
        modifier = modifier
            .size(48.dp)
            .graphicsLayer { this.alpha = alpha },
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Add To Favorite Guide Step 1", showSystemUi = true, widthDp = 360)
@Composable
private fun OnboardingAddToFavoriteGuideScreenStep1Preview() {
    AddToFavoriteGuidePreview(initialPage = 0)
}

@Preview(name = "Add To Favorite Guide Step 2", showSystemUi = true, widthDp = 360)
@Composable
private fun OnboardingAddToFavoriteGuideScreenStep2Preview() {
    AddToFavoriteGuidePreview(initialPage = 1)
}

@Preview(name = "Add To Favorite Guide Step 3", showSystemUi = true, widthDp = 360)
@Composable
private fun OnboardingAddToFavoriteGuideScreenStep3Preview() {
    AddToFavoriteGuidePreview(initialPage = 2)
}

@Preview(name = "Add To Favorite Guide Step 4", showSystemUi = true, widthDp = 360)
@Composable
private fun OnboardingAddToFavoriteGuideScreenStep4Preview() {
    AddToFavoriteGuidePreview(initialPage = 3)
}

@Composable
private fun AddToFavoriteGuidePreview(initialPage: Int) {
    RECAPTheme(dynamicColor = false) {
        OnboardingAddToFavoriteGuideScreen(
            onBackClick = {},
            initialPage = initialPage,
        )
    }
}
