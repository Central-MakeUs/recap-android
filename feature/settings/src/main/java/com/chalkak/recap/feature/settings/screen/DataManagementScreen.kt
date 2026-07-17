package com.chalkak.recap.feature.settings.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.OrganizedScreenshotSummaryCard
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.feature.settings.DataManagementAction
import com.chalkak.recap.feature.settings.DataManagementUiState

@Composable
fun DataManagementScreen(
    uiState: DataManagementUiState,
    onAction: (DataManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_data_management_title),
                onBackClick = { onAction(DataManagementAction.NavigateBack) },
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = RecapBackground,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DataManagementTokens.HorizontalPadding)
                    .padding(
                        top = DataManagementTokens.ContentTopPadding,
                        bottom = DataManagementTokens.ContentBottomPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(DataManagementTokens.SectionSpacing),
            ) {
                OrganizedScreenshotSummaryCard(organizedCount = uiState.organizedCount)
                RecapButton(
                    text = stringResource(R.string.settings_data_management_delete_button),
                    onClick = { onAction(DataManagementAction.DeleteDataClick) },
                    modifier = Modifier.fillMaxWidth(),
                    size = RecapButtonSize.Large,
                    colors = RecapButtonDefaults.colors(
                        containerColor = RecapGray50,
                        contentColor = RecapError,
                        disabledContainerColor = RecapGray50,
                        disabledContentColor = RecapError.copy(alpha = 0.38f),
                    ),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        DataManagementTokens.BulletItemSpacing,
                    ),
                ) {
                    DataManagementBulletItem(
                        textResId = R.string.settings_data_management_note_account_kept,
                    )
                    DataManagementBulletItem(
                        textResId = R.string.settings_data_management_note_irreversible,
                    )
                }
            }
        }
    }

    if (uiState.showDeleteConfirmDialog) {
        RecapPopup(
            title = stringResource(R.string.settings_data_management_delete_confirm_title),
            description = stringResource(
                R.string.settings_data_management_delete_confirm_description,
            ),
            confirmButtonText = stringResource(
                R.string.settings_data_management_delete_confirm_button,
            ),
            cancelButtonText = stringResource(
                R.string.settings_data_management_delete_confirm_cancel_button,
            ),
            onConfirmClick = { onAction(DataManagementAction.ConfirmDeleteData) },
            onCancelClick = { onAction(DataManagementAction.DismissDeleteConfirmDialog) },
            onDismissRequest = { onAction(DataManagementAction.DismissDeleteConfirmDialog) },
            confirmButtonColor = RecapError,
        )
    }
}

@Composable
private fun DataManagementBulletItem(
    @StringRes textResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DataManagementTokens.BulletMarkerSpacing),
    ) {
        Text(
            text = stringResource(R.string.settings_privacy_guide_bullet_marker),
            style = RecapCaption1,
            color = RecapGray300,
        )
        Text(
            text = stringResource(textResId),
            modifier = Modifier.weight(1f),
            style = RecapCaption1,
            color = RecapGray300,
        )
    }
}

private object DataManagementTokens {
    val HorizontalPadding = 16.dp
    val ContentTopPadding = 16.dp
    val ContentBottomPadding = 32.dp
    val SectionSpacing = 19.dp
    val BulletItemSpacing = 8.dp
    val BulletMarkerSpacing = 7.dp
}

@Preview(name = "Data Management", showBackground = true, widthDp = 360)
@Composable
private fun DataManagementScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            uiState = DataManagementUiState(
                organizedCount = DataManagementScreenPreviewCount,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Data Management Delete Confirm", showBackground = true, widthDp = 360)
@Composable
private fun DataManagementDeleteConfirmPreview() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            uiState = DataManagementUiState(
                organizedCount = DataManagementScreenPreviewCount,
                showDeleteConfirmDialog = true,
            ),
            onAction = {},
        )
    }
}

private const val DataManagementScreenPreviewCount = 128
