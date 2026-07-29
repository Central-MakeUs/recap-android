package com.chalkak.recap.core.design.component.bottomsheet

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapSheetHandle
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDataTransferConsentBottomSheet(
    onDismissRequest: () -> Unit,
    onAgreeClick: () -> Unit,
    onCancelClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = AiDataTransferConsentBottomSheetTokens.ContainerCornerRadius,
            topEnd = AiDataTransferConsentBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            AiDataTransferConsentBottomSheetDragHandle()
        },
    ) {
        AiDataTransferConsentBottomSheetContent(
            onAgreeClick = onAgreeClick,
            onCancelClick = onCancelClick,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AiDataTransferConsentBottomSheetTokens.HorizontalPadding,
                    top = AiDataTransferConsentBottomSheetTokens.ContentTopPadding,
                    end = AiDataTransferConsentBottomSheetTokens.HorizontalPadding,
                    bottom = AiDataTransferConsentBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun AiDataTransferConsentBottomSheetContent(
    onAgreeClick: () -> Unit,
    onCancelClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.size(
                AiDataTransferConsentBottomSheetTokens.IconContainerSize
            ),
            shape = RoundedCornerShape(
                AiDataTransferConsentBottomSheetTokens.IconContainerRadius
            ),
            color = RecapBlue50,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_upload_arrow_16),
                    contentDescription = stringResource(
                        R.string.organize_ai_data_transfer_consent_icon_content_description
                    ),
                    modifier = Modifier.size(
                        AiDataTransferConsentBottomSheetTokens.IconSize
                    ),
                    tint = Color.Unspecified,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                AiDataTransferConsentBottomSheetTokens.IconToTitleSpacing
            )
        )

        Text(
            text = stringResource(R.string.organize_ai_data_transfer_consent_title),
            color = RecapGray900,
            style = RecapHeading2,
        )

        Spacer(
            modifier = Modifier.height(
                AiDataTransferConsentBottomSheetTokens.TitleToDescriptionSpacing
            )
        )

        Text(
            text = stringResource(R.string.organize_ai_data_transfer_consent_description),
            color = RecapGray700,
            style = RecapBody2,
        )

        Spacer(
            modifier = Modifier.height(
                AiDataTransferConsentBottomSheetTokens.DescriptionToPrivacySpacing
            )
        )

        Text(
            text = stringResource(R.string.organize_ai_data_transfer_consent_privacy_policy),
            modifier = Modifier.clickable(
                role = Role.Button,
                onClick = onPrivacyPolicyClick,
            ),
            color = RecapGray500,
            style = RecapBody2,
            textDecoration = TextDecoration.Underline,
        )

        Spacer(
            modifier = Modifier.height(
                AiDataTransferConsentBottomSheetTokens.PrivacyToBulletSpacing
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(
                AiDataTransferConsentBottomSheetTokens.BulletItemSpacing
            ),
        ) {
            AiDataTransferConsentBulletItem(
                textResId = R.string.organize_ai_data_transfer_consent_bullet_1,
            )
            AiDataTransferConsentBulletItem(
                textResId = R.string.organize_ai_data_transfer_consent_bullet_2,
            )
            AiDataTransferConsentBulletItem(
                textResId = R.string.organize_ai_data_transfer_consent_bullet_3,
            )
        }

        Spacer(
            modifier = Modifier.height(
                AiDataTransferConsentBottomSheetTokens.BulletToPrimarySpacing
            )
        )

        RecapButton(
            text = stringResource(R.string.organize_ai_data_transfer_consent_agree_button),
            onClick = onAgreeClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 15.dp),
        )

        Spacer(
            modifier = Modifier.height(
                AiDataTransferConsentBottomSheetTokens.PrimaryToSecondarySpacing
            )
        )

        RecapButton(
            text = stringResource(R.string.organize_ai_data_transfer_consent_cancel_button),
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            colors = RecapButtonDefaults.textColors(),
            contentPadding = PaddingValues(vertical = 15.dp),
        )
    }
}

@Composable
private fun AiDataTransferConsentBulletItem(
    @StringRes textResId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            AiDataTransferConsentBottomSheetTokens.BulletMarkerSpacing
        ),
    ) {
        Text(
            text = stringResource(R.string.settings_privacy_guide_bullet_marker),
            color = RecapGray500,
            style = RecapBody2,
        )
        Text(
            text = stringResource(textResId),
            modifier = Modifier.weight(1f),
            color = RecapGray500,
            style = RecapBody2,
        )
    }
}

@Composable
private fun AiDataTransferConsentBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(
                vertical = AiDataTransferConsentBottomSheetTokens.DragHandleVerticalPadding
            )
            .size(
                width = AiDataTransferConsentBottomSheetTokens.DragHandleWidth,
                height = AiDataTransferConsentBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(AiDataTransferConsentBottomSheetTokens.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

private object AiDataTransferConsentBottomSheetTokens {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 43.dp
    val DragHandleHeight = 5.dp
    val DragHandleVerticalPadding = 13.dp
    val HorizontalPadding = 24.dp
    val ContentTopPadding = 16.dp
    val BottomPadding = 48.dp
    val IconContainerSize = 40.dp
    val IconContainerRadius = 10.dp
    val IconSize = 16.dp
    val IconToTitleSpacing = 12.dp
    val TitleToDescriptionSpacing = 13.dp
    val DescriptionToPrivacySpacing = 12.dp
    val PrivacyToBulletSpacing = 32.dp
    val BulletItemSpacing = 8.dp
    val BulletMarkerSpacing = 8.dp
    val BulletToPrimarySpacing = 30.dp
    val PrimaryToSecondarySpacing = 12.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "AI Data Transfer Consent Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun AiDataTransferConsentBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = AiDataTransferConsentBottomSheetTokens.ContainerCornerRadius,
                    topEnd = AiDataTransferConsentBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AiDataTransferConsentBottomSheetDragHandle()
                    AiDataTransferConsentBottomSheetContent(
                        onAgreeClick = {},
                        onCancelClick = {},
                        onPrivacyPolicyClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = AiDataTransferConsentBottomSheetTokens.HorizontalPadding,
                                top = AiDataTransferConsentBottomSheetTokens.ContentTopPadding,
                                end = AiDataTransferConsentBottomSheetTokens.HorizontalPadding,
                                bottom = AiDataTransferConsentBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
