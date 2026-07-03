package com.chalkak.recap.feature.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R

@Composable
fun MyPageDataManagementScreen(
    onBackClick: () -> Unit,
    onAccountManagementClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MyPageDetailScreenScaffold(
        titleResId = R.string.my_page_data_management_title,
        onBackClick = onBackClick,
        bottomContent = {
            Text(
                text = stringResource(R.string.my_page_data_management_policy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier,
    ) {
        MyPageDataCard(
            labelResId = R.string.my_page_data_management_organized_label,
            titleResId = R.string.my_page_data_management_organized_title,
            descriptionResId = R.string.my_page_data_management_organized_description,
        )
        MyPageDataCard(
            labelResId = R.string.my_page_data_management_delete_label,
            titleResId = R.string.my_page_data_management_delete_title,
            descriptionResId = R.string.my_page_data_management_delete_description,
        )
        MyPageDataCard(
            labelResId = R.string.my_page_data_management_range_label,
            titleResId = R.string.my_page_data_management_range_title,
            descriptionResId = R.string.my_page_data_management_range_description,
        )
        MyPageDataCard(
            labelResId = R.string.my_page_data_management_all_data_label,
            titleResId = R.string.my_page_data_management_all_data_title,
            descriptionResId = R.string.my_page_data_management_all_data_description,
            action = {
                val buttonColor = MaterialTheme.colorScheme.primary
                OutlinedButton(
                    onClick = onAccountManagementClick,
                    modifier = Modifier.height(34.dp),
                    border = BorderStroke(1.dp, buttonColor),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = buttonColor,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 10.dp,
                        vertical = 0.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.my_page_data_management_account_button),
                        style = MaterialTheme.typography.bodySmall,
                        color = buttonColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = buttonColor,
                    )
                }
            },
        )
    }
}
