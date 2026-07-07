package com.chalkak.recap.feature.mypage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RecapBlue50

@Composable
fun MyPageUploadGuideScreen(
    onBackClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MyPageDetailScreenScaffold(
        titleResId = R.string.my_page_upload_guide_title,
        onBackClick = onBackClick,
        bottomContent = {
            Text(
                text = stringResource(R.string.my_page_upload_guide_settings_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RecapButton(
                text = stringResource(R.string.my_page_upload_guide_settings_button),
                onClick = onOpenSettingsClick,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 12.dp,
            )
        },
        modifier = modifier,
    ) {
        MyPageGuideCard(
            icon = Icons.Outlined.CheckBox,
            titleResId = R.string.my_page_upload_guide_select_title,
            descriptionResId = R.string.my_page_upload_guide_select_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
        )
        MyPageGuideCard(
            icon = Icons.Outlined.Share,
            titleResId = R.string.my_page_upload_guide_share_title,
            descriptionResId = R.string.my_page_upload_guide_share_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
        )
        MyPageGuideCard(
            icon = Icons.Outlined.CloudOff,
            titleResId = R.string.my_page_upload_guide_no_auto_title,
            descriptionResId = R.string.my_page_upload_guide_no_auto_description,
        )
        MyPageGuideCard(
            icon = Icons.Outlined.VisibilityOff,
            titleResId = R.string.my_page_upload_guide_exclude_sensitive_title,
            descriptionResId = R.string.my_page_upload_guide_exclude_sensitive_description,
        )
    }
}
