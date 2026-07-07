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
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapSheetHandle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoutConfirmationBottomSheet(
    onDismissRequest: () -> Unit,
    onCancelClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = LogoutConfirmationBottomSheetTokens.ContainerCornerRadius,
            topEnd = LogoutConfirmationBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            LogoutConfirmationBottomSheetDragHandle()
        },
    ) {
        LogoutConfirmationBottomSheetContent(
            onCancelClick = onCancelClick,
            onLogoutClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = LogoutConfirmationBottomSheetTokens.HorizontalPadding,
                    top = LogoutConfirmationBottomSheetTokens.ContentTopPadding,
                    end = LogoutConfirmationBottomSheetTokens.HorizontalPadding,
                    bottom = LogoutConfirmationBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun LogoutConfirmationBottomSheetContent(
    onCancelClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            LogoutConfirmationBottomSheetTokens.SectionSpacing
        ),
    ) {
        Surface(
            modifier = Modifier.size(LogoutConfirmationBottomSheetTokens.IconContainerSize),
            shape = RoundedCornerShape(LogoutConfirmationBottomSheetTokens.IconContainerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                contentDescription = stringResource(
                    R.string.logout_confirmation_icon_content_description
                ),
                modifier = Modifier
                    .padding(LogoutConfirmationBottomSheetTokens.IconPadding)
                    .size(LogoutConfirmationBottomSheetTokens.IconSize),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(
                LogoutConfirmationBottomSheetTokens.TextSpacing
            ),
        ) {
            Text(
                text = stringResource(R.string.logout_confirmation_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.logout_confirmation_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(LogoutConfirmationBottomSheetTokens.ActionTopSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                LogoutConfirmationBottomSheetTokens.ButtonSpacing
            ),
        ) {
            RecapButton(
                text = stringResource(R.string.logout_confirmation_cancel_button),
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                size = RecapButtonSize.Medium,
                colors = RecapButtonDefaults.outlinedColors(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            )
            RecapButton(
                text = stringResource(R.string.logout_confirmation_logout_button),
                onClick = onLogoutClick,
                modifier = Modifier.weight(1f),
                size = RecapButtonSize.Medium,
                shadowElevation = LogoutConfirmationBottomSheetTokens.PrimaryButtonElevation,
            )
        }
    }
}

@Composable
private fun LogoutConfirmationBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(vertical = LogoutConfirmationBottomSheetTokens.DragHandleVerticalPadding)
            .size(
                width = LogoutConfirmationBottomSheetTokens.DragHandleWidth,
                height = LogoutConfirmationBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(LogoutConfirmationBottomSheetTokens.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

private object LogoutConfirmationBottomSheetTokens {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 48.dp
    val DragHandleHeight = 6.dp
    val DragHandleVerticalPadding = 8.dp
    val HorizontalPadding = 24.dp
    val ContentTopPadding = 4.dp
    val BottomPadding = 40.dp
    val SectionSpacing = 12.dp
    val TextSpacing = 12.dp
    val IconContainerSize = 72.dp
    val IconContainerRadius = 36.dp
    val IconPadding = 18.dp
    val IconSize = 28.dp
    val ActionTopSpacing = 4.dp
    val ButtonSpacing = 12.dp
    val PrimaryButtonElevation = 12.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Logout Confirmation Bottom Sheet", showBackground = true, widthDp = 360)
@Composable
private fun LogoutConfirmationBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = LogoutConfirmationBottomSheetTokens.ContainerCornerRadius,
                    topEnd = LogoutConfirmationBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoutConfirmationBottomSheetDragHandle()
                    LogoutConfirmationBottomSheetContent(
                        onCancelClick = {},
                        onLogoutClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = LogoutConfirmationBottomSheetTokens.HorizontalPadding,
                                top = LogoutConfirmationBottomSheetTokens.ContentTopPadding,
                                end = LogoutConfirmationBottomSheetTokens.HorizontalPadding,
                                bottom = LogoutConfirmationBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
