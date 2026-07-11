package com.chalkak.recap.core.design.component.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.text.style.TextAlign
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
fun OrganizeNotificationPermissionBottomSheet(
    onDismissRequest: () -> Unit,
    onAllowNotificationClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = OrganizeNotificationPermissionBottomSheetTokens.ContainerCornerRadius,
            topEnd = OrganizeNotificationPermissionBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            OrganizeNotificationPermissionBottomSheetDragHandle()
        },
    ) {
        OrganizeNotificationPermissionBottomSheetContent(
            onAllowNotificationClick = onAllowNotificationClick,
            onLaterClick = onLaterClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = OrganizeNotificationPermissionBottomSheetTokens.HorizontalPadding,
                    top = OrganizeNotificationPermissionBottomSheetTokens.ContentTopPadding,
                    end = OrganizeNotificationPermissionBottomSheetTokens.HorizontalPadding,
                    bottom = OrganizeNotificationPermissionBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun OrganizeNotificationPermissionBottomSheetContent(
    onAllowNotificationClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            OrganizeNotificationPermissionBottomSheetTokens.SectionSpacing
        ),
    ) {
        Surface(
            modifier = Modifier.size(OrganizeNotificationPermissionBottomSheetTokens.IconContainerSize),
            shape = RoundedCornerShape(OrganizeNotificationPermissionBottomSheetTokens.IconContainerRadius),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = stringResource(
                    R.string.organize_notification_permission_icon_content_description
                ),
                modifier = Modifier
                    .padding(OrganizeNotificationPermissionBottomSheetTokens.IconPadding)
                    .size(OrganizeNotificationPermissionBottomSheetTokens.IconSize),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                OrganizeNotificationPermissionBottomSheetTokens.TextSpacing
            ),
        ) {
            Text(
                text = stringResource(R.string.organize_notification_permission_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.organize_notification_permission_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(
            modifier = Modifier.height(
                OrganizeNotificationPermissionBottomSheetTokens.ActionTopSpacing
            )
        )

        RecapButton(
            text = stringResource(R.string.organize_notification_permission_allow_button),
            onClick = onAllowNotificationClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Medium,
            shadowElevation = OrganizeNotificationPermissionBottomSheetTokens.PrimaryButtonElevation,
        )

        RecapButton(
            text = stringResource(R.string.organize_notification_permission_later_button),
            onClick = onLaterClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Compact,
            colors = RecapButtonDefaults.textColors(),
        )

        Text(
            text = stringResource(R.string.organize_notification_permission_notice),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OrganizeNotificationPermissionBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(
                vertical = OrganizeNotificationPermissionBottomSheetTokens.DragHandleVerticalPadding
            )
            .size(
                width = OrganizeNotificationPermissionBottomSheetTokens.DragHandleWidth,
                height = OrganizeNotificationPermissionBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(OrganizeNotificationPermissionBottomSheetTokens.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

private object OrganizeNotificationPermissionBottomSheetTokens {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 48.dp
    val DragHandleHeight = 6.dp
    val DragHandleVerticalPadding = 8.dp
    val HorizontalPadding = 24.dp
    val ContentTopPadding = 4.dp
    val BottomPadding = 48.dp
    val SectionSpacing = 12.dp
    val TextSpacing = 10.dp
    val IconContainerSize = 88.dp
    val IconContainerRadius = 24.dp
    val IconPadding = 24.dp
    val IconSize = 32.dp
    val ActionTopSpacing = 4.dp
    val PrimaryButtonElevation = 12.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Organize Notification Permission Bottom Sheet", showBackground = true, widthDp = 360)
@Composable
private fun OrganizeNotificationPermissionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = OrganizeNotificationPermissionBottomSheetTokens.ContainerCornerRadius,
                    topEnd = OrganizeNotificationPermissionBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OrganizeNotificationPermissionBottomSheetDragHandle()
                    OrganizeNotificationPermissionBottomSheetContent(
                        onAllowNotificationClick = {},
                        onLaterClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = OrganizeNotificationPermissionBottomSheetTokens.HorizontalPadding,
                                top = OrganizeNotificationPermissionBottomSheetTokens.ContentTopPadding,
                                end = OrganizeNotificationPermissionBottomSheetTokens.HorizontalPadding,
                                bottom = OrganizeNotificationPermissionBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
