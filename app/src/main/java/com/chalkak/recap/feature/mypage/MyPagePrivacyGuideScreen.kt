package com.chalkak.recap.feature.mypage

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RecapBlue50

@Composable
fun MyPagePrivacyGuideScreen(
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MyPageDetailScreenScaffold(
        titleResId = R.string.my_page_privacy_guide_title,
        onBackClick = onBackClick,
        bottomContent = {
            MyPageDocumentButton(
                text = stringResource(R.string.my_page_privacy_guide_policy_button),
                onClick = onPrivacyPolicyClick,
            )
            MyPageDocumentButton(
                text = stringResource(R.string.my_page_privacy_guide_terms_button),
                onClick = onTermsClick,
            )
        },
        modifier = modifier,
    ) {
        MyPageGuideCard(
            icon = Icons.Outlined.CheckBox,
            titleResId = R.string.my_page_privacy_guide_selected_only_title,
            descriptionResId = R.string.my_page_privacy_guide_selected_only_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
        )
        MyPageGuideCard(
            icon = Icons.Outlined.ErrorOutline,
            titleResId = R.string.my_page_privacy_guide_no_auto_filter_title,
            descriptionResId = R.string.my_page_privacy_guide_no_auto_filter_description,
            iconTint = MyPageDetailTokens.WarningIconColor,
            iconContainerColor = MyPageDetailTokens.WarningContainerColor,
        )
        MyPageGuideCard(
            icon = Icons.Outlined.Cancel,
            titleResId = R.string.my_page_privacy_guide_user_exclusion_title,
            descriptionResId = R.string.my_page_privacy_guide_user_exclusion_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
        )
        MyPageGuideCard(
            icon = Icons.Outlined.CreditCard,
            titleResId = R.string.my_page_privacy_guide_result_data_title,
            descriptionResId = R.string.my_page_privacy_guide_result_data_description,
        )
    }
}
