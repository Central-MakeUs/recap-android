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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageLoadFailureBottomSheet(
    onDismissRequest: () -> Unit,
    onRetryClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = ImageLoadFailureBottomSheetTokens.ContainerCornerRadius,
            topEnd = ImageLoadFailureBottomSheetTokens.ContainerCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            ImageLoadFailureBottomSheetDragHandle()
        },
    ) {
        ImageLoadFailureBottomSheetContent(
            onRetryClick = onRetryClick,
            onGoHomeClick = onGoHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ImageLoadFailureBottomSheetTokens.HorizontalPadding,
                    end = ImageLoadFailureBottomSheetTokens.HorizontalPadding,
                    bottom = ImageLoadFailureBottomSheetTokens.BottomPadding,
                ),
        )
    }
}

@Composable
fun ImageLoadFailureBottomSheetContent(
    onRetryClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            ImageLoadFailureBottomSheetTokens.SectionSpacing
        ),
    ) {
        Surface(
            modifier = Modifier.size(ImageLoadFailureBottomSheetTokens.IconContainerSize),
            shape = RoundedCornerShape(ImageLoadFailureBottomSheetTokens.IconContainerRadius),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        ) {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = null,
                modifier = Modifier
                    .padding(ImageLoadFailureBottomSheetTokens.IconPadding)
                    .size(ImageLoadFailureBottomSheetTokens.IconSize),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(
                ImageLoadFailureBottomSheetTokens.TextSpacing
            ),
        ) {
            Text(
                text = stringResource(R.string.image_load_failure_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.image_load_failure_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(modifier = Modifier.height(ImageLoadFailureBottomSheetTokens.ActionTopSpacing))

        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(ImageLoadFailureBottomSheetTokens.PrimaryButtonHeight),
            shape = RoundedCornerShape(ImageLoadFailureBottomSheetTokens.ButtonCornerRadius),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = ImageLoadFailureBottomSheetTokens.PrimaryButtonElevation,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(ImageLoadFailureBottomSheetTokens.ButtonIconSize),
            )
            Text(
                text = stringResource(R.string.image_load_failure_retry_button),
                modifier = Modifier.padding(start = ImageLoadFailureBottomSheetTokens.ButtonIconSpacing),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        TextButton(
            onClick = onGoHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(ImageLoadFailureBottomSheetTokens.TextButtonHeight),
        ) {
            Text(
                text = stringResource(R.string.image_load_failure_home_button),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ImageLoadFailureBottomSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(vertical = ImageLoadFailureBottomSheetTokens.DragHandleVerticalPadding)
            .size(
                width = ImageLoadFailureBottomSheetTokens.DragHandleWidth,
                height = ImageLoadFailureBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(ImageLoadFailureBottomSheetTokens.DragHandleHeight),
        color = MaterialTheme.colorScheme.outlineVariant,
        content = {},
    )
}

private object ImageLoadFailureBottomSheetTokens {
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
    val PrimaryButtonHeight = 52.dp
    val TextButtonHeight = 42.dp
    val ButtonCornerRadius = 14.dp
    val PrimaryButtonElevation = 20.dp
    val ButtonIconSize = 22.dp
    val ButtonIconSpacing = 8.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Image Load Failure Bottom Sheet", showBackground = true, widthDp = 360)
@Preview(
    name = "Image Load Failure Bottom Sheet - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ImageLoadFailureBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = ImageLoadFailureBottomSheetTokens.ContainerCornerRadius,
                    topEnd = ImageLoadFailureBottomSheetTokens.ContainerCornerRadius,
                ),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ImageLoadFailureBottomSheetDragHandle()
                    ImageLoadFailureBottomSheetContent(
                        onRetryClick = {},
                        onGoHomeClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = ImageLoadFailureBottomSheetTokens.HorizontalPadding,
                                end = ImageLoadFailureBottomSheetTokens.HorizontalPadding,
                                bottom = ImageLoadFailureBottomSheetTokens.BottomPadding,
                            ),
                    )
                }
            }
        }
    }
}
