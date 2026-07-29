package com.chalkak.recap.feature.settings.data

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.OrganizedScreenshotSummaryCard
import com.chalkak.recap.core.design.component.divider.RecapSectionDivider
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1

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
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                RecapSectionDivider()
                DataTransferConsentSection(
                    isConsented = uiState.isAiDataTransferConsented,
                    consentDate = uiState.aiDataTransferConsentDate,
                    onConsentActionClick = {
                        onAction(DataManagementAction.AiDataTransferConsentClick)
                    },
                )
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

    if (uiState.showWithdrawConsentDialog) {
        RecapPopup(
            title = stringResource(R.string.settings_data_management_withdraw_consent_title),
            description = stringResource(
                R.string.settings_data_management_withdraw_consent_description,
            ),
            confirmButtonText = stringResource(
                R.string.settings_data_management_withdraw_consent_confirm_button,
            ),
            cancelButtonText = stringResource(
                R.string.settings_data_management_withdraw_consent_cancel_button,
            ),
            onConfirmClick = { onAction(DataManagementAction.ConfirmWithdrawConsent) },
            onCancelClick = { onAction(DataManagementAction.DismissWithdrawConsentDialog) },
            onDismissRequest = { onAction(DataManagementAction.DismissWithdrawConsentDialog) },
            confirmButtonColor = RecapBlue300,
        )
    }
}

@Composable
private fun DataTransferConsentSection(
    isConsented: Boolean,
    consentDate: String,
    onConsentActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DataManagementTokens.HorizontalPadding)
            .padding(
                top = DataManagementTokens.ConsentSectionTopPadding,
                bottom = DataManagementTokens.ConsentSectionBottomPadding,
            ),
    ) {
        Text(
            text = stringResource(R.string.settings_data_management_transfer_section),
            style = RecapBody2,
            color = RecapGray500,
        )
        Spacer(modifier = Modifier.height(DataManagementTokens.ConsentSectionHeaderSpacing))
        AiDataTransferConsentRow(
            isConsented = isConsented,
            consentDate = consentDate,
            onActionClick = onConsentActionClick,
        )
    }
}

@Composable
private fun AiDataTransferConsentRow(
    isConsented: Boolean,
    consentDate: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusText = when {
        isConsented && consentDate.isNotBlank() -> {
            stringResource(
                R.string.settings_data_management_ai_consent_agreed_format,
                consentDate,
            )
        }
        isConsented -> stringResource(R.string.settings_data_management_ai_consent_agreed)
        else -> stringResource(R.string.settings_data_management_ai_consent_not_agreed)
    }
    val actionText = stringResource(
        if (isConsented) {
            R.string.settings_data_management_ai_consent_revoke
        } else {
            R.string.settings_data_management_ai_consent_agree
        },
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DataManagementTokens.ConsentRowSpacing),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DataManagementTokens.ConsentTextSpacing),
        ) {
            Text(
                text = stringResource(R.string.settings_data_management_ai_consent_title),
                style = RecapBody1,
                color = RecapGray900,
            )
            Text(
                text = statusText,
                style = RecapBody2,
                color = RecapGray300,
            )
        }
        Text(
            text = actionText,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onActionClick,
            ),
            style = RecapBody2,
            color = RecapGray300,
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
    val ConsentSectionTopPadding = 32.dp
    val ConsentSectionBottomPadding = 32.dp
    val ConsentSectionHeaderSpacing = 16.dp
    val ConsentRowSpacing = 12.dp
    val ConsentTextSpacing = 4.dp
}

@Preview(name = "Data Management", showBackground = true, widthDp = 360)
@Composable
private fun DataManagementScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            uiState = DataManagementUiState(
                organizedCount = DataManagementScreenPreviewCount,
                isAiDataTransferConsented = true,
                aiDataTransferConsentDate = stringResource(
                    R.string.settings_data_management_ai_consent_preview_date,
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Data Management Consent Revoked", showBackground = true, widthDp = 360)
@Composable
private fun DataManagementConsentRevokedPreview() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            uiState = DataManagementUiState(
                organizedCount = DataManagementScreenPreviewCount,
                isAiDataTransferConsented = false,
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
                isAiDataTransferConsented = true,
                aiDataTransferConsentDate = stringResource(
                    R.string.settings_data_management_ai_consent_preview_date,
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Data Management Withdraw Consent Confirm", showBackground = true, widthDp = 360)
@Composable
private fun DataManagementWithdrawConsentConfirmPreview() {
    RECAPTheme(dynamicColor = false) {
        DataManagementScreen(
            uiState = DataManagementUiState(
                organizedCount = DataManagementScreenPreviewCount,
                showWithdrawConsentDialog = true,
                isAiDataTransferConsented = true,
                aiDataTransferConsentDate = stringResource(
                    R.string.settings_data_management_ai_consent_preview_date,
                ),
            ),
            onAction = {},
        )
    }
}

private const val DataManagementScreenPreviewCount = 128
