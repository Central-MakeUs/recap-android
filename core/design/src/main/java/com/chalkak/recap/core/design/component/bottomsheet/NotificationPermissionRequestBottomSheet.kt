package com.chalkak.recap.core.design.component.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapSheetHandle
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionRequestBottomSheet(
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
            topStart = NotificationPermissionRequestBottomSheetTokens.ContainerCornerRadius,
            topEnd = NotificationPermissionRequestBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            NotificationPermissionRequestBottomSheetDragHandle()
        },
    ) {
        NotificationPermissionRequestBottomSheetContent(
            onAllowNotificationClick = onAllowNotificationClick,
            onLaterClick = onLaterClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = NotificationPermissionRequestBottomSheetTokens.HorizontalPadding,
                    top = NotificationPermissionRequestBottomSheetTokens.ContentTopPadding,
                    end = NotificationPermissionRequestBottomSheetTokens.HorizontalPadding,
                    bottom = NotificationPermissionRequestBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun NotificationPermissionRequestBottomSheetContent(
    onAllowNotificationClick: () -> Unit,
    onLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(
                NotificationPermissionRequestBottomSheetTokens.IconContainerSize
            ),
            shape = RoundedCornerShape(
                NotificationPermissionRequestBottomSheetTokens.IconContainerRadius
            ),
            color = RecapBlue50,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification_bell_36),
                    contentDescription = stringResource(
                        R.string.organize_notification_permission_icon_content_description
                    ),
                    modifier = Modifier.size(
                        width = NotificationPermissionRequestBottomSheetTokens.IconWidth,
                        height = NotificationPermissionRequestBottomSheetTokens.IconHeight,
                    ),
                    tint = Color.Unspecified,
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                NotificationPermissionRequestBottomSheetTokens.IconToTitleSpacing
            )
        )

        Text(
            text = stringResource(R.string.organize_notification_permission_title),
            color = Black,
            style = RecapHeading2,
            textAlign = TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.height(
                NotificationPermissionRequestBottomSheetTokens.TitleToDescriptionSpacing
            )
        )

        Text(
            text = stringResource(R.string.organize_notification_permission_description),
            color = RecapGray500,
            style = RecapBody1,
            textAlign = TextAlign.Center,
        )

        Spacer(
            modifier = Modifier.height(
                NotificationPermissionRequestBottomSheetTokens.DescriptionToPrimarySpacing
            )
        )

        RecapButton(
            text = stringResource(R.string.organize_notification_permission_allow_button),
            onClick = onAllowNotificationClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 15.dp)
        )

        Spacer(
            modifier = Modifier.height(
                NotificationPermissionRequestBottomSheetTokens.PrimaryToSecondarySpacing
            )
        )

        RecapButton(
            text = stringResource(R.string.organize_notification_permission_later_button),
            onClick = onLaterClick,
            modifier = Modifier.fillMaxWidth(),
            colors = RecapButtonDefaults.textColors(),
            contentPadding = PaddingValues(vertical = 15.dp)
        )
    }
}

@Composable
private fun NotificationPermissionRequestBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(
                vertical = NotificationPermissionRequestBottomSheetTokens.DragHandleVerticalPadding
            )
            .size(
                width = NotificationPermissionRequestBottomSheetTokens.DragHandleWidth,
                height = NotificationPermissionRequestBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(NotificationPermissionRequestBottomSheetTokens.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

private object NotificationPermissionRequestBottomSheetTokens {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 43.dp
    val DragHandleHeight = 5.dp
    val DragHandleVerticalPadding = 13.dp
    val HorizontalPadding = 24.dp
    val ContentTopPadding = 16.dp
    val BottomPadding = 48.dp
    val IconContainerSize = 40.dp
    val IconContainerRadius = 10.dp
    val IconWidth = 16.dp
    val IconHeight = 19.dp
    val IconToTitleSpacing = 18.dp
    val TitleToDescriptionSpacing = 12.dp
    val DescriptionToPrimarySpacing = 35.dp
    val PrimaryToSecondarySpacing = 12.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Notification Permission Request Bottom Sheet",
    showBackground = true,
    widthDp = 360
)
@Composable
private fun NotificationPermissionRequestBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = NotificationPermissionRequestBottomSheetTokens.ContainerCornerRadius,
                    topEnd = NotificationPermissionRequestBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NotificationPermissionRequestBottomSheetDragHandle()
                    NotificationPermissionRequestBottomSheetContent(
                        onAllowNotificationClick = {},
                        onLaterClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = NotificationPermissionRequestBottomSheetTokens.HorizontalPadding,
                                top = NotificationPermissionRequestBottomSheetTokens.ContentTopPadding,
                                end = NotificationPermissionRequestBottomSheetTokens.HorizontalPadding,
                                bottom = NotificationPermissionRequestBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
