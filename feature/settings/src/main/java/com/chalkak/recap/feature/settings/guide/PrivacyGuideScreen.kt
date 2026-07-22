package com.chalkak.recap.feature.settings.guide

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.divider.RecapSectionDivider
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography
import com.chalkak.recap.feature.settings.SettingsNavRow
import com.chalkak.recap.feature.settings.SettingsRowTokens

@Composable
fun PrivacyGuideScreen(
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_privacy_guide_title),
                onBackClick = onBackClick,
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = RecapBackground,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SettingsRowTokens.HorizontalPadding)
                        .padding(
                            top = PrivacyGuideTokens.ContentTopPadding,
                            bottom = PrivacyGuideTokens.ContentBottomPadding,
                        ),
                    verticalArrangement = Arrangement.spacedBy(PrivacyGuideTokens.SectionSpacing),
                ) {
                    PrivacyGuideSection(
                        titleResId = R.string.settings_privacy_guide_image_processing_title,
                        bulletResIds = listOf(
                            R.string.settings_privacy_guide_image_processing_bullet_1,
                            R.string.settings_privacy_guide_image_processing_bullet_2,
                        ),
                    )
                    PrivacyGuideSection(
                        titleResId = R.string.settings_privacy_guide_original_storage_title,
                        bulletResIds = listOf(
                            R.string.settings_privacy_guide_original_storage_bullet_1,
                            R.string.settings_privacy_guide_original_storage_bullet_2,
                        ),
                    )
                    PrivacyGuideSection(
                        titleResId = R.string.settings_privacy_guide_sensitive_info_title,
                        bulletResIds = listOf(
                            R.string.settings_privacy_guide_sensitive_info_bullet_1,
                            R.string.settings_privacy_guide_sensitive_info_bullet_2,
                        ),
                    )
                }
                RecapSectionDivider()
                Spacer(
                    modifier = Modifier.height(
                        PrivacyGuideTokens.DividerToRowSpacing -
                            SettingsRowTokens.ClickAreaVerticalPadding,
                    ),
                )
                SettingsNavRow(
                    titleResId = R.string.settings_service_info_privacy_title,
                    onClick = onPrivacyPolicyClick,
                )
                SettingsNavRow(
                    titleResId = R.string.settings_service_info_terms_title,
                    onClick = onTermsClick,
                )
                Spacer(modifier = Modifier.height(SettingsRowTokens.BottomSpacing))
            }
        }
    }
}

@Composable
private fun PrivacyGuideSection(
    @StringRes titleResId: Int,
    bulletResIds: List<Int>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PrivacyGuideTokens.BulletListSpacing),
    ) {
        Text(
            text = stringResource(titleResId),
            style = RecapTypography.RecapHeading3,
            color = RecapGray900,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(PrivacyGuideTokens.BulletItemSpacing),
        ) {
            bulletResIds.forEach { bulletResId ->
                PrivacyGuideBulletItem(textResId = bulletResId)
            }
        }
    }
}

@Composable
private fun PrivacyGuideBulletItem(
    @StringRes textResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PrivacyGuideTokens.BulletMarkerSpacing),
    ) {
        Text(
            text = stringResource(R.string.settings_privacy_guide_bullet_marker),
            style = RecapTypography.RecapBody2,
            color = RecapGray500,
        )
        Text(
            text = stringResource(textResId),
            modifier = Modifier.weight(1f),
            style = RecapTypography.RecapBody2,
            color = RecapGray500,
        )
    }
}

private object PrivacyGuideTokens {
    val ContentTopPadding = 16.dp
    val ContentBottomPadding = 32.dp
    val SectionSpacing = 32.dp
    val BulletListSpacing = 13.dp
    val BulletItemSpacing = 8.dp
    val BulletMarkerSpacing = 8.dp
    // divider 하단 ~ row 텍스트까지 32.dp (row 상단 click padding 보정)
    val DividerToRowSpacing = 32.dp
}

@Preview(name = "Privacy Guide", showBackground = true, widthDp = 360)
@Composable
private fun PrivacyGuideScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        PrivacyGuideScreen(
            onBackClick = {},
            onPrivacyPolicyClick = {},
            onTermsClick = {},
        )
    }
}
