package com.chalkak.recap.feature.settings.guide

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.card.ShareFavoriteGuideCard
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading4

@Composable
fun UsageGuideScreen(
    onBackClick: () -> Unit,
    onShareFavoriteGuideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_usage_guide_title),
                onBackClick = onBackClick,
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = RecapBackground,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = UsageGuideTokens.HorizontalPadding)
                    .padding(
                        top = UsageGuideTokens.ContentTopPadding,
                        bottom = UsageGuideTokens.ContentBottomPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(UsageGuideTokens.SectionSpacing),
            ) {
                ShareFavoriteGuideCard(onClick = onShareFavoriteGuideClick)
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        UsageGuideTokens.GuideListHeaderSpacing,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.settings_usage_guide_section_title),
                        style = RecapCaption1,
                        color = RecapGray300,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            UsageGuideTokens.GuideItemSpacing,
                        ),
                    ) {
                        UsageGuideItem(
                            iconResId = R.drawable.ic_checkbox_checked_16,
                            titleResId = R.string.settings_usage_guide_select_title,
                            descriptionResId = R.string.settings_usage_guide_select_description,
                        )
                        UsageGuideItem(
                            iconResId = R.drawable.ic_share_16,
                            titleResId = R.string.settings_usage_guide_share_title,
                            descriptionResId = R.string.settings_usage_guide_share_description,
                        )
                        UsageGuideItem(
                            iconResId = R.drawable.ic_no_auto_import_16,
                            titleResId = R.string.settings_usage_guide_no_auto_title,
                            descriptionResId = R.string.settings_usage_guide_no_auto_description,
                        )
                        UsageGuideItem(
                            iconResId = R.drawable.ic_shield_16,
                            titleResId = R.string.settings_usage_guide_permission_title,
                            descriptionResId = R.string.settings_usage_guide_permission_description,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageGuideItem(
    @DrawableRes iconResId: Int,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UsageGuideTokens.IconTextSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(UsageGuideTokens.IconSize),
            tint = RecapBlue300,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(UsageGuideTokens.ItemTextSpacing),
        ) {
            Text(
                text = stringResource(titleResId),
                style = RecapHeading4,
                color = RecapGray900,
            )
            Text(
                text = stringResource(descriptionResId),
                style = RecapBody2,
                color = RecapGray500,
            )
        }
    }
}

private object UsageGuideTokens {
    val HorizontalPadding = 16.dp
    val ContentTopPadding = 16.dp
    val ContentBottomPadding = 32.dp
    val SectionSpacing = 28.dp
    val GuideListHeaderSpacing = 16.dp
    val GuideItemSpacing = 40.dp
    val IconTextSpacing = 12.dp
    val ItemTextSpacing = 4.dp
    val IconSize = 16.dp
}

@Preview(name = "Usage Guide", showBackground = true, widthDp = 360)
@Composable
private fun UsageGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        UsageGuideScreen(
            onBackClick = {},
            onShareFavoriteGuideClick = {},
        )
    }
}
