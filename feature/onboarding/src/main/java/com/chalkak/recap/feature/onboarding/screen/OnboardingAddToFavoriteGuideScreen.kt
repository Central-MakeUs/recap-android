package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingAddToFavoriteGuideScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                backButtonContentDescription = stringResource(R.string.my_page_back_content_description),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
            ) {
                AddToFavoriteGuideCarousel(
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(R.string.onboarding_add_to_favorite_guide_body),
                    modifier = Modifier.padding(top = 18.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AddToFavoriteGuideCarousel(
    modifier: Modifier = Modifier,
) {
    val pageCount = 5
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    fun animateToPage(page: Int) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(page.coerceIn(0, pageCount - 1))
        }
    }

    Box(
        modifier = modifier
            .heightIn(min = 360.dp, max = 430.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            GuideImagePlaceholder(
                stepNumber = page + 1,
                contentDescription = stringResource(
                    R.string.onboarding_add_to_favorite_guide_step_image_content_description,
                    page + 1,
                ),
                modifier = Modifier.fillMaxSize(),
            )
        }
        GuideChevronButton(
            iconResId = R.drawable.ic_chevron_left_24,
            contentDescription = stringResource(
                R.string.onboarding_add_to_favorite_guide_previous_content_description
            ),
            enabled = currentPage > 0,
            onClick = { animateToPage(currentPage - 1) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp),
        )
        GuideChevronButton(
            iconResId = R.drawable.ic_chevron_right_24,
            contentDescription = stringResource(
                R.string.onboarding_add_to_favorite_guide_next_content_description
            ),
            enabled = currentPage < pageCount - 1,
            onClick = { animateToPage(currentPage + 1) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp),
        )
        Text(
            text = stringResource(
                R.string.onboarding_add_to_favorite_guide_page_indicator,
                currentPage + 1,
                pageCount,
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GuideImagePlaceholder(
    stepNumber: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stepNumber.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GuideChevronButton(
    iconResId: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp),
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            tint = if (enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
            },
        )
    }
}

@Preview(name = "Add To Favorite Guide", showSystemUi = true, widthDp = 360)
@Composable
private fun OnboardingAddToFavoriteGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        OnboardingAddToFavoriteGuideScreen(
            onBackClick = {},
        )
    }
}
