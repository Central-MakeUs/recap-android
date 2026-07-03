package com.chalkak.recap.feature.mypage

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RecapGray300

@Composable
fun MyPageServiceInfoScreen(
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onNoticeClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onOpenSourceLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MyPageDetailScreenScaffold(
        titleResId = R.string.my_page_service_info_title,
        onBackClick = onBackClick,
        bottomContent = {
            Text(
                text = stringResource(R.string.my_page_service_info_copyright),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = RecapGray300,
            )
        },
        modifier = modifier,
    ) {
        MyPageServiceSummaryCard()
        MyPageServiceMenuGroup(
            items = listOf(
                MyPageServiceMenuItemData(
                    titleResId = R.string.my_page_service_info_contact_title,
                    descriptionResId = R.string.my_page_service_info_contact_description,
                    onClick = onContactClick,
                ),
                MyPageServiceMenuItemData(
                    titleResId = R.string.my_page_service_info_notice_title,
                    descriptionResId = R.string.my_page_service_info_notice_description,
                    onClick = onNoticeClick,
                ),
                MyPageServiceMenuItemData(
                    titleResId = R.string.my_page_service_info_terms_title,
                    descriptionResId = R.string.my_page_service_info_terms_description,
                    onClick = onTermsClick,
                ),
                MyPageServiceMenuItemData(
                    titleResId = R.string.my_page_service_info_privacy_title,
                    descriptionResId = R.string.my_page_service_info_privacy_description,
                    onClick = onPrivacyPolicyClick,
                ),
                MyPageServiceMenuItemData(
                    titleResId = R.string.my_page_service_info_open_source_title,
                    descriptionResId = R.string.my_page_service_info_open_source_description,
                    onClick = onOpenSourceLicenseClick,
                ),
            ),
        )
    }
}
