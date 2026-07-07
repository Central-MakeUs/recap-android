package com.chalkak.recap.core.design.component.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonColors
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapSheetHandle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalConfirmationBottomSheet(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onCancelClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = WithdrawalConfirmationBottomSheetTokens.ContainerCornerRadius,
            topEnd = WithdrawalConfirmationBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            WithdrawalConfirmationBottomSheetDragHandle()
        },
    ) {
        WithdrawalConfirmationBottomSheetContent(
            checked = checked,
            onCheckedChange = onCheckedChange,
            onCancelClick = onCancelClick,
            onWithdrawClick = onWithdrawClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = WithdrawalConfirmationBottomSheetTokens.HorizontalPadding,
                    top = WithdrawalConfirmationBottomSheetTokens.ContentTopPadding,
                    end = WithdrawalConfirmationBottomSheetTokens.HorizontalPadding,
                    bottom = WithdrawalConfirmationBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun WithdrawalConfirmationBottomSheetContent(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onCancelClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            WithdrawalConfirmationBottomSheetTokens.SectionSpacing
        ),
    ) {
        Surface(
            modifier = Modifier.size(WithdrawalConfirmationBottomSheetTokens.IconContainerSize),
            shape = RoundedCornerShape(WithdrawalConfirmationBottomSheetTokens.IconContainerRadius),
            color = WithdrawalConfirmationBottomSheetTokens.IconContainerColor,
            contentColor = WithdrawalConfirmationBottomSheetTokens.DestructiveColor,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = stringResource(
                    R.string.withdrawal_confirmation_icon_content_description
                ),
                modifier = Modifier
                    .padding(WithdrawalConfirmationBottomSheetTokens.IconPadding)
                    .size(WithdrawalConfirmationBottomSheetTokens.IconSize),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(
                WithdrawalConfirmationBottomSheetTokens.TextSpacing
            ),
        ) {
            Text(
                text = stringResource(R.string.withdrawal_confirmation_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.withdrawal_confirmation_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WithdrawalConfirmationBottomSheetTokens.CheckRowRadius),
            color = RecapGray100,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = { onCheckedChange(!checked) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WithdrawalConfirmationBottomSheetTokens.CheckRowHorizontalPadding,
                        vertical = WithdrawalConfirmationBottomSheetTokens.CheckRowVerticalPadding,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    WithdrawalConfirmationBottomSheetTokens.CheckTextSpacing
                ),
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.size(WithdrawalConfirmationBottomSheetTokens.CheckboxSize),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                Text(
                    text = stringResource(R.string.withdrawal_confirmation_check_label),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(WithdrawalConfirmationBottomSheetTokens.ActionTopSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                WithdrawalConfirmationBottomSheetTokens.ButtonSpacing
            ),
        ) {
            RecapButton(
                text = stringResource(R.string.withdrawal_confirmation_cancel_button),
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                size = RecapButtonSize.Medium,
                colors = RecapButtonDefaults.outlinedColors(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            )
            RecapButton(
                text = stringResource(R.string.withdrawal_confirmation_withdraw_button),
                onClick = onWithdrawClick,
                modifier = Modifier.weight(1f),
                enabled = checked,
                size = RecapButtonSize.Medium,
                colors = WithdrawalConfirmationBottomSheetDefaults.destructiveButtonColors(),
            )
        }
    }
}

@Composable
private fun WithdrawalConfirmationBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(vertical = WithdrawalConfirmationBottomSheetTokens.DragHandleVerticalPadding)
            .size(
                width = WithdrawalConfirmationBottomSheetTokens.DragHandleWidth,
                height = WithdrawalConfirmationBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(WithdrawalConfirmationBottomSheetTokens.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

private object WithdrawalConfirmationBottomSheetDefaults {
    @Composable
    fun destructiveButtonColors(): RecapButtonColors = RecapButtonColors(
        containerColor = WithdrawalConfirmationBottomSheetTokens.DestructiveColor,
        contentColor = Color.White,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )
}

private object WithdrawalConfirmationBottomSheetTokens {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 48.dp
    val DragHandleHeight = 6.dp
    val DragHandleVerticalPadding = 8.dp
    val HorizontalPadding = 24.dp
    val ContentTopPadding = 4.dp
    val BottomPadding = 40.dp
    val SectionSpacing = 12.dp
    val TextSpacing = 10.dp
    val IconContainerSize = 56.dp
    val IconContainerRadius = 28.dp
    val IconPadding = 14.dp
    val IconSize = 24.dp
    val CheckRowRadius = 10.dp
    val CheckRowHorizontalPadding = 12.dp
    val CheckRowVerticalPadding = 10.dp
    val CheckTextSpacing = 8.dp
    val CheckboxSize = 24.dp
    val ActionTopSpacing = 4.dp
    val ButtonSpacing = 12.dp
    val DestructiveColor = Color(0xFFD9443F)
    val IconContainerColor = Color(0xFFFCE6E6)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Withdrawal Confirmation Bottom Sheet", showBackground = true, widthDp = 360)
@Composable
private fun WithdrawalConfirmationBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = WithdrawalConfirmationBottomSheetTokens.ContainerCornerRadius,
                    topEnd = WithdrawalConfirmationBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WithdrawalConfirmationBottomSheetDragHandle()
                    WithdrawalConfirmationBottomSheetContent(
                        checked = true,
                        onCheckedChange = {},
                        onCancelClick = {},
                        onWithdrawClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = WithdrawalConfirmationBottomSheetTokens.HorizontalPadding,
                                top = WithdrawalConfirmationBottomSheetTokens.ContentTopPadding,
                                end = WithdrawalConfirmationBottomSheetTokens.HorizontalPadding,
                                bottom = WithdrawalConfirmationBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
