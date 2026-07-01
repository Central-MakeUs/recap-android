package com.chalkak.recap.core.design.component

import android.content.res.Configuration
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
import androidx.compose.material.icons.outlined.Image
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
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

data class PhotoAccessPermissionBottomSheetText(
    val iconContentDescription: String,
    val title: String,
    val description: String,
    val notice: String,
    val primaryButton: String,
    val laterButton: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoAccessPermissionBottomSheet(
    onDismissRequest: () -> Unit,
    onPrimaryButtonClick: () -> Unit,
    onLaterClick: () -> Unit,
    text: PhotoAccessPermissionBottomSheetText,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = PhotoAccessPermissionBottomSheetTokens.ContainerCornerRadius,
            topEnd = PhotoAccessPermissionBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            PhotoAccessPermissionBottomSheetDragHandle()
        },
    ) {
        PhotoAccessPermissionBottomSheetContent(
            onPrimaryButtonClick = onPrimaryButtonClick,
            onLaterClick = onLaterClick,
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = PhotoAccessPermissionBottomSheetTokens.HorizontalPadding,
                    end = PhotoAccessPermissionBottomSheetTokens.HorizontalPadding,
                    bottom = PhotoAccessPermissionBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun PhotoAccessPermissionBottomSheetContent(
    onPrimaryButtonClick: () -> Unit,
    onLaterClick: () -> Unit,
    text: PhotoAccessPermissionBottomSheetText,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            PhotoAccessPermissionBottomSheetTokens.SectionSpacing
        ),
    ) {
        Surface(
            modifier = Modifier.size(PhotoAccessPermissionBottomSheetTokens.IconContainerSize),
            shape = RoundedCornerShape(PhotoAccessPermissionBottomSheetTokens.IconContainerRadius),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = text.iconContentDescription,
                modifier = Modifier
                    .padding(PhotoAccessPermissionBottomSheetTokens.IconPadding)
                    .size(PhotoAccessPermissionBottomSheetTokens.IconSize),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(
                PhotoAccessPermissionBottomSheetTokens.TextSpacing
            ),
        ) {
            Text(
                text = text.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = text.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Text(
            text = text.notice,
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelMedium,
        )

        Spacer(modifier = Modifier.height(PhotoAccessPermissionBottomSheetTokens.ActionTopSpacing))

        RecapButton(
            text = text.primaryButton,
            onClick = onPrimaryButtonClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Medium,
            shadowElevation = PhotoAccessPermissionBottomSheetTokens.PrimaryButtonElevation,
        )

        RecapButton(
            text = text.laterButton,
            onClick = onLaterClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Compact,
            colors = RecapButtonDefaults.textColors(),
        )
    }
}

@Composable
private fun PhotoAccessPermissionBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(vertical = PhotoAccessPermissionBottomSheetTokens.DragHandleVerticalPadding)
            .size(
                width = PhotoAccessPermissionBottomSheetTokens.DragHandleWidth,
                height = PhotoAccessPermissionBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(PhotoAccessPermissionBottomSheetTokens.DragHandleHeight),
        color = MaterialTheme.colorScheme.outlineVariant,
        content = {},
    )
}

private object PhotoAccessPermissionBottomSheetTokens {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 48.dp
    val DragHandleHeight = 6.dp
    val DragHandleVerticalPadding = 8.dp
    val HorizontalPadding = 24.dp
    val BottomPadding = 48.dp
    val SectionSpacing = 14.dp
    val TextSpacing = 6.dp
    val IconContainerSize = 72.dp
    val IconContainerRadius = 22.dp
    val IconPadding = 16.dp
    val IconSize = 24.dp
    val ActionTopSpacing = 4.dp
    val PrimaryButtonElevation = 20.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Photo Access Permission Bottom Sheet", showBackground = true, widthDp = 360)
@Preview(
    name = "Photo Access Permission Bottom Sheet - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PhotoAccessPermissionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = PhotoAccessPermissionBottomSheetTokens.ContainerCornerRadius,
                    topEnd = PhotoAccessPermissionBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PhotoAccessPermissionBottomSheetDragHandle()
                    PhotoAccessPermissionBottomSheetContent(
                        onPrimaryButtonClick = {},
                        onLaterClick = {},
                        text = PhotoAccessPermissionBottomSheetText(
                            iconContentDescription = stringResource(
                                R.string.photo_access_permission_icon_content_description
                            ),
                            title = stringResource(R.string.photo_access_permission_title),
                            description = stringResource(
                                R.string.photo_access_permission_description
                            ),
                            notice = stringResource(R.string.photo_access_permission_notice),
                            primaryButton = stringResource(
                                R.string.photo_access_permission_request_permission
                            ),
                            laterButton = stringResource(
                                R.string.photo_access_permission_later_button
                            ),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = PhotoAccessPermissionBottomSheetTokens.HorizontalPadding,
                                end = PhotoAccessPermissionBottomSheetTokens.HorizontalPadding,
                                bottom = PhotoAccessPermissionBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
