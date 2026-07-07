package com.chalkak.recap.core.design.component.bottomsheet

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsOff
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonColors
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapSheetHandle

data class RecapActionBottomSheetIconStyle(
    val containerSize: Dp = RecapActionBottomSheetDefaults.IconContainerSize,
    val containerShape: Shape = RoundedCornerShape(
        RecapActionBottomSheetDefaults.IconContainerRadius
    ),
    val containerColor: Color,
    val contentColor: Color,
    val iconPadding: Dp = RecapActionBottomSheetDefaults.IconPadding,
    val iconSize: Dp = RecapActionBottomSheetDefaults.IconSize,
)

enum class RecapActionBottomSheetNoticeAlignment {
    Start,
    Center,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapActionBottomSheet(
    icon: ImageVector,
    iconContentDescription: String?,
    iconStyle: RecapActionBottomSheetIconStyle,
    title: String,
    description: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    onDismissRequest: () -> Unit,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    topNotice: String? = null,
    topNoticeAlignment: RecapActionBottomSheetNoticeAlignment =
        RecapActionBottomSheetNoticeAlignment.Start,
    bottomNotice: String? = null,
    bottomNoticeAlignment: RecapActionBottomSheetNoticeAlignment =
        RecapActionBottomSheetNoticeAlignment.Start,
    textSpacing: Dp = RecapActionBottomSheetDefaults.TextSpacing,
    bottomPadding: Dp = RecapActionBottomSheetDefaults.BottomPadding,
    primaryButtonColors: RecapButtonColors = RecapButtonDefaults.primaryColors(),
    primaryButtonElevation: Dp = RecapActionBottomSheetDefaults.PrimaryButtonElevation,
    secondaryButtonColors: RecapButtonColors = RecapButtonDefaults.textColors(),
    secondaryButtonSize: RecapButtonSize = RecapButtonSize.Compact,
    secondaryButtonBorder: BorderStroke? = null,
    primaryButtonLeadingIcon: (@Composable () -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = RecapActionBottomSheetDefaults.ContainerCornerRadius,
            topEnd = RecapActionBottomSheetDefaults.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            RecapActionBottomSheetDragHandle()
        },
    ) {
        RecapActionBottomSheetContent(
            icon = icon,
            iconContentDescription = iconContentDescription,
            iconStyle = iconStyle,
            title = title,
            description = description,
            topNotice = topNotice,
            topNoticeAlignment = topNoticeAlignment,
            bottomNotice = bottomNotice,
            bottomNoticeAlignment = bottomNoticeAlignment,
            primaryButtonText = primaryButtonText,
            secondaryButtonText = secondaryButtonText,
            onPrimaryClick = onPrimaryClick,
            onSecondaryClick = onSecondaryClick,
            textSpacing = textSpacing,
            primaryButtonColors = primaryButtonColors,
            primaryButtonElevation = primaryButtonElevation,
            secondaryButtonColors = secondaryButtonColors,
            secondaryButtonSize = secondaryButtonSize,
            secondaryButtonBorder = secondaryButtonBorder,
            primaryButtonLeadingIcon = primaryButtonLeadingIcon,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = RecapActionBottomSheetDefaults.HorizontalPadding,
                    top = RecapActionBottomSheetDefaults.ContentTopPadding,
                    end = RecapActionBottomSheetDefaults.HorizontalPadding,
                    bottom = bottomPadding,
                ),
        )
    }
}

@Composable
fun RecapActionBottomSheetContent(
    icon: ImageVector,
    iconContentDescription: String?,
    iconStyle: RecapActionBottomSheetIconStyle,
    title: String,
    description: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    topNotice: String? = null,
    topNoticeAlignment: RecapActionBottomSheetNoticeAlignment =
        RecapActionBottomSheetNoticeAlignment.Start,
    bottomNotice: String? = null,
    bottomNoticeAlignment: RecapActionBottomSheetNoticeAlignment =
        RecapActionBottomSheetNoticeAlignment.Start,
    textSpacing: Dp = RecapActionBottomSheetDefaults.TextSpacing,
    primaryButtonColors: RecapButtonColors = RecapButtonDefaults.primaryColors(),
    primaryButtonElevation: Dp = RecapActionBottomSheetDefaults.PrimaryButtonElevation,
    secondaryButtonColors: RecapButtonColors = RecapButtonDefaults.textColors(),
    secondaryButtonSize: RecapButtonSize = RecapButtonSize.Compact,
    secondaryButtonBorder: BorderStroke? = null,
    primaryButtonLeadingIcon: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RecapActionBottomSheetDefaults.SectionSpacing),
    ) {
        Surface(
            modifier = Modifier.size(iconStyle.containerSize),
            shape = iconStyle.containerShape,
            color = iconStyle.containerColor,
            contentColor = iconStyle.contentColor,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier
                    .padding(iconStyle.iconPadding)
                    .size(iconStyle.iconSize),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(textSpacing)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (topNotice != null) {
            RecapActionBottomSheetNotice(
                text = topNotice,
                alignment = topNoticeAlignment,
            )
        }

        Spacer(modifier = Modifier.height(RecapActionBottomSheetDefaults.ActionTopSpacing))

        RecapButton(
            text = primaryButtonText,
            onClick = onPrimaryClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Medium,
            colors = primaryButtonColors,
            shadowElevation = primaryButtonElevation,
            leadingIcon = primaryButtonLeadingIcon,
        )

        RecapButton(
            text = secondaryButtonText,
            onClick = onSecondaryClick,
            modifier = Modifier.fillMaxWidth(),
            size = secondaryButtonSize,
            colors = secondaryButtonColors,
            border = secondaryButtonBorder,
        )

        if (bottomNotice != null) {
            RecapActionBottomSheetNotice(
                text = bottomNotice,
                alignment = bottomNoticeAlignment,
            )
        }
    }
}

@Composable
private fun RecapActionBottomSheetNotice(
    text: String,
    alignment: RecapActionBottomSheetNoticeAlignment,
) {
    Text(
        text = text,
        modifier = if (alignment == RecapActionBottomSheetNoticeAlignment.Center) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
        },
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelMedium,
        textAlign = when (alignment) {
            RecapActionBottomSheetNoticeAlignment.Start -> TextAlign.Start
            RecapActionBottomSheetNoticeAlignment.Center -> TextAlign.Center
        },
    )
}

@Composable
private fun RecapActionBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(vertical = RecapActionBottomSheetDefaults.DragHandleVerticalPadding)
            .size(
                width = RecapActionBottomSheetDefaults.DragHandleWidth,
                height = RecapActionBottomSheetDefaults.DragHandleHeight,
            ),
        shape = RoundedCornerShape(RecapActionBottomSheetDefaults.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

object RecapActionBottomSheetDefaults {
    val ContainerCornerRadius = 24.dp
    val DragHandleWidth = 48.dp
    val DragHandleHeight = 6.dp
    val DragHandleVerticalPadding = 8.dp
    val HorizontalPadding = 24.dp
    val ContentTopPadding = 4.dp
    val BottomPadding = 48.dp
    val SectionSpacing = 12.dp
    val TextSpacing = 6.dp
    val IconContainerSize = 72.dp
    val IconContainerRadius = 22.dp
    val IconPadding = 16.dp
    val IconSize = 24.dp
    val ActionTopSpacing = 4.dp
    val PrimaryButtonElevation = 12.dp

    @Composable
    fun primaryIconStyle(): RecapActionBottomSheetIconStyle =
        RecapActionBottomSheetIconStyle(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        )

    @Composable
    fun surfaceVariantIconStyle(
        containerSize: Dp = 64.dp,
        containerRadius: Dp = 32.dp,
    ): RecapActionBottomSheetIconStyle = RecapActionBottomSheetIconStyle(
        containerSize = containerSize,
        containerShape = RoundedCornerShape(containerRadius),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    @Composable
    fun errorIconStyle(): RecapActionBottomSheetIconStyle =
        RecapActionBottomSheetIconStyle(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        )

    fun warningIconStyle(): RecapActionBottomSheetIconStyle =
        RecapActionBottomSheetIconStyle(
            containerShape = RoundedCornerShape(36.dp),
            containerColor = Color(0xFFFFF4DF),
            contentColor = Color(0xFFD08A13),
            iconPadding = 18.dp,
            iconSize = 28.dp,
        )

    fun deleteIconStyle(): RecapActionBottomSheetIconStyle =
        RecapActionBottomSheetIconStyle(
            containerShape = RoundedCornerShape(36.dp),
            containerColor = Color(0xFFFCE6E6),
            contentColor = Color(0xFFD9443F),
            iconPadding = 18.dp,
            iconSize = 28.dp,
        )

    @Composable
    fun destructiveFilledColors(): RecapButtonColors = RecapButtonColors(
        containerColor = Color(0xFFD9443F),
        contentColor = Color.White,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    @Composable
    fun destructiveTextColors(): RecapButtonColors = RecapButtonColors(
        containerColor = Color.Transparent,
        contentColor = Color(0xFFD9443F),
        disabledContainerColor = Color.Transparent,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )
}

@Preview(
    name = "Photo Access",
    group = "Action Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun PhotoAccessActionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapActionBottomSheetPreviewShell {
            RecapActionBottomSheetContent(
                icon = Icons.Outlined.Image,
                iconContentDescription = stringResource(
                    R.string.photo_access_permission_icon_content_description
                ),
                iconStyle = RecapActionBottomSheetDefaults.primaryIconStyle(),
                title = stringResource(R.string.photo_access_permission_title),
                description = stringResource(R.string.photo_access_permission_description),
                topNotice = stringResource(R.string.photo_access_permission_notice),
                primaryButtonText = stringResource(
                    R.string.photo_access_permission_request_permission
                ),
                secondaryButtonText = stringResource(R.string.photo_access_permission_later_button),
                onPrimaryClick = {},
                onSecondaryClick = {},
                modifier = RecapActionBottomSheetDefaults.previewContentModifier(),
            )
        }
    }
}

@Preview(
    name = "Image Load Failure",
    group = "Action Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun ImageLoadFailureActionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapActionBottomSheetPreviewShell {
            RecapActionBottomSheetContent(
                icon = Icons.Outlined.BrokenImage,
                iconContentDescription = null,
                iconStyle = RecapActionBottomSheetDefaults.errorIconStyle(),
                title = stringResource(R.string.image_load_failure_title),
                description = stringResource(R.string.image_load_failure_description),
                primaryButtonText = stringResource(R.string.image_load_failure_retry_button),
                secondaryButtonText = stringResource(R.string.image_load_failure_home_button),
                onPrimaryClick = {},
                onSecondaryClick = {},
                primaryButtonLeadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                },
                modifier = RecapActionBottomSheetDefaults.previewContentModifier(),
            )
        }
    }
}

@Preview(
    name = "Notification Disabled",
    group = "Action Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun NotificationDisabledActionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapActionBottomSheetPreviewShell {
            RecapActionBottomSheetContent(
                icon = Icons.Outlined.NotificationsOff,
                iconContentDescription = stringResource(
                    R.string.notification_disabled_icon_content_description
                ),
                iconStyle = RecapActionBottomSheetDefaults.surfaceVariantIconStyle(),
                title = stringResource(R.string.notification_disabled_title),
                description = stringResource(R.string.notification_disabled_description),
                bottomNotice = stringResource(R.string.notification_disabled_notice),
                bottomNoticeAlignment = RecapActionBottomSheetNoticeAlignment.Center,
                primaryButtonText = stringResource(R.string.notification_disabled_settings_button),
                secondaryButtonText = stringResource(R.string.notification_disabled_later_button),
                onPrimaryClick = {},
                onSecondaryClick = {},
                modifier = RecapActionBottomSheetDefaults.previewContentModifier(
                    bottom = 40.dp,
                ),
            )
        }
    }
}

@Preview(
    name = "Unsaved Changes",
    group = "Action Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun UnsavedChangesActionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapActionBottomSheetPreviewShell {
            RecapActionBottomSheetContent(
                icon = Icons.Outlined.ErrorOutline,
                iconContentDescription = stringResource(
                    R.string.unsaved_changes_icon_content_description
                ),
                iconStyle = RecapActionBottomSheetDefaults.warningIconStyle(),
                title = stringResource(R.string.unsaved_changes_title),
                description = stringResource(R.string.unsaved_changes_description),
                primaryButtonText = stringResource(R.string.unsaved_changes_keep_editing_button),
                secondaryButtonText = stringResource(
                    R.string.unsaved_changes_exit_without_saving_button
                ),
                onPrimaryClick = {},
                onSecondaryClick = {},
                textSpacing = 12.dp,
                secondaryButtonColors = RecapActionBottomSheetDefaults.destructiveTextColors(),
                modifier = RecapActionBottomSheetDefaults.previewContentModifier(),
            )
        }
    }
}

@Preview(
    name = "Deletion Confirmation",
    group = "Action Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun DeletionConfirmationActionBottomSheetPreview() {
    DeletionConfirmationActionBottomSheetPreviewContent(
        description = stringResource(R.string.deletion_confirmation_preview_description),
    )
}

@Preview(
    name = "Deletion Confirmation - Removed From All Lists",
    group = "Action Bottom Sheet",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun DeletionConfirmationRemovedFromAllListsActionBottomSheetPreview() {
    DeletionConfirmationActionBottomSheetPreviewContent(
        description = stringResource(
            R.string.deletion_confirmation_removed_from_all_lists_preview_description
        ),
    )
}

@Composable
private fun DeletionConfirmationActionBottomSheetPreviewContent(
    description: String,
) {
    RECAPTheme(dynamicColor = false) {
        RecapActionBottomSheetPreviewShell {
            RecapActionBottomSheetContent(
                icon = Icons.Outlined.Delete,
                iconContentDescription = stringResource(
                    R.string.deletion_confirmation_icon_content_description
                ),
                iconStyle = RecapActionBottomSheetDefaults.deleteIconStyle(),
                title = stringResource(R.string.deletion_confirmation_preview_title),
                description = description,
                primaryButtonText = stringResource(R.string.deletion_confirmation_delete_button),
                secondaryButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
                onPrimaryClick = {},
                onSecondaryClick = {},
                textSpacing = 12.dp,
                primaryButtonColors = RecapActionBottomSheetDefaults.destructiveFilledColors(),
                primaryButtonElevation = 0.dp,
                secondaryButtonColors = RecapButtonDefaults.outlinedColors(),
                secondaryButtonSize = RecapButtonSize.Medium,
                secondaryButtonBorder = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = RecapActionBottomSheetDefaults.previewContentModifier(
                    bottom = 40.dp,
                ),
            )
        }
    }
}

@Composable
private fun RecapActionBottomSheetPreviewShell(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = RecapActionBottomSheetDefaults.ContainerCornerRadius,
                topEnd = RecapActionBottomSheetDefaults.ContainerCornerRadius,
            ),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RecapActionBottomSheetDragHandle()
                content()
            }
        }
    }
}

private fun RecapActionBottomSheetDefaults.previewContentModifier(
    bottom: Dp = BottomPadding,
): Modifier = Modifier
    .fillMaxWidth()
    .padding(
        start = HorizontalPadding,
        top = ContentTopPadding,
        end = HorizontalPadding,
        bottom = bottom,
    )
