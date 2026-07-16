package com.chalkak.recap.feature.screenshot

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapSheetHandle
import com.chalkak.recap.core.design.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotActionBottomSheet(
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = ScreenshotTokens.BottomSheetCornerRadius,
            topEnd = ScreenshotTokens.BottomSheetCornerRadius,
        ),
        containerColor = White,
        contentColor = RecapGray900,
        dragHandle = { ScreenshotSheetDragHandle() },
    ) {
        ScreenshotActionBottomSheetContent(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onCloseClick = onCloseClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenshotTokens.BottomSheetHorizontalPadding,
                    top = ScreenshotTokens.BottomSheetContentTopPadding,
                    end = ScreenshotTokens.BottomSheetHorizontalPadding,
                    bottom = ScreenshotTokens.BottomSheetBottomPadding,
                ),
        )
    }
}

@Composable
fun ScreenshotActionBottomSheetContent(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
    ) {
        RecapButton(
            text = stringResource(R.string.screenshot_action_edit),
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            size = RecapButtonSize.Large,
            colors = RecapButtonDefaults.neutralColors(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        RecapButton(
            text = stringResource(R.string.screenshot_action_delete),
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            size = RecapButtonSize.Large,
            colors = RecapButtonDefaults.colors(
                containerColor = RecapGray50,
                contentColor = RecapError,
                disabledContainerColor = RecapGray50.copy(alpha = 0.12f),
                disabledContentColor = RecapError.copy(alpha = 0.38f),
            ),
        )
        Spacer(modifier = Modifier.height(24.dp))
        RecapButton(
            text = stringResource(R.string.screenshot_action_close),
            onClick = onCloseClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            size = RecapButtonSize.Large,
            colors = RecapButtonDefaults.colors(
                containerColor = White,
                contentColor = RecapGray900,
                disabledContainerColor = White,
                disabledContentColor = RecapGray900.copy(alpha = 0.38f),
            ),
            border = BorderStroke(1.dp, RecapGray200),
        )
    }
}

@Composable
internal fun ScreenshotSheetDragHandle(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(
                top = ScreenshotActionBottomSheetTokens.DragHandleTopPadding,
                bottom = ScreenshotActionBottomSheetTokens.DragHandleBottomPadding,
            )
            .size(
                width = ScreenshotActionBottomSheetTokens.DragHandleWidth,
                height = ScreenshotActionBottomSheetTokens.DragHandleHeight,
            ),
        shape = RoundedCornerShape(ScreenshotActionBottomSheetTokens.DragHandleHeight),
        color = RecapSheetHandle,
        content = {},
    )
}

@Preview(name = "Screenshot Action Bottom Sheet", showBackground = false, widthDp = 360)
@Composable
private fun ScreenshotActionBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = ScreenshotTokens.BottomSheetCornerRadius,
                    topEnd = ScreenshotTokens.BottomSheetCornerRadius,
                ),
                color = White,
                contentColor = RecapGray900,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ScreenshotSheetDragHandle()
                    ScreenshotActionBottomSheetContent(
                        onEditClick = {},
                        onDeleteClick = {},
                        onCloseClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = ScreenshotTokens.BottomSheetHorizontalPadding,
                                top = ScreenshotTokens.BottomSheetContentTopPadding,
                                end = ScreenshotTokens.BottomSheetHorizontalPadding,
                                bottom = ScreenshotTokens.BottomSheetBottomPadding,
                            ),
                    )
                }
            }
        }
    }
}

private object ScreenshotActionBottomSheetTokens {
    val DragHandleWidth = 43.dp
    val DragHandleHeight = 5.dp
    val DragHandleTopPadding = 13.dp
    val DragHandleBottomPadding = 17.dp
}
