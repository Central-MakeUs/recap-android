package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.progress.RecapStepProgressIndicator
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapOnboardingBlue
import com.chalkak.recap.core.design.theme.RecapTypography
import kotlinx.coroutines.launch
import kotlin.math.abs

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

private val GuideTouchPositions: List<Pair<Float, Float>?> = listOf(
    0.925f to 0.965f, // 더보기
    0.963f to 0.023f, // 편집
    0.186f to 0.500f, // RECAP 체크
    null,
)

private val GuideContentHorizontalPadding = 28.dp
private val GuideTouchIconSize = 55.dp
private const val GuideTouchIconAlpha = 0.2f
private const val GuidePagerPageWidthFraction = 0.75f
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Image(
                        painter = painterResource(GuideStepImages[page]),
                        contentDescription = stringResource(
                            R.string.onboarding_add_to_favorite_guide_step_image_content_description,
                            page + 1,
                        ),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    GuideTouchPositions.getOrNull(page)?.let { (xFraction, yFraction) ->
                        GuideTouchIcon(
                            modifier = Modifier.align(
                                BiasAlignment(
                                    horizontalBias = xFraction * 2f - 1f,
                                    verticalBias = yFraction * 2f - 1f,
                                ),
                            ),
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
                    style = RecapTypography.RecapBody1,
                    color = RecapGray700,
                )
                Text(
                    text = stringResource(descriptionResId),
                    modifier = Modifier.padding(start = 8.dp),
                    style = RecapTypography.RecapBody1,
                    color = RecapGray700,
                )
            }
        }
    }
}

@Composable
private fun GuideTouchIcon(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(GuideTouchIconSize)
            .background(
                color = RecapOnboardingBlue.copy(alpha = GuideTouchIconAlpha),
                shape = CircleShape,
            ),
    )
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
