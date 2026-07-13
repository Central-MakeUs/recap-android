package com.chalkak.recap.feature.screenshot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.toLabelResId
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

// TODO : 다듬기
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotTypePickerBottomSheet(
    selectedType: ScreenshotContentType,
    onDismissRequest: () -> Unit,
    onTypeSelected: (ScreenshotContentType) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        ScreenshotTypePickerBottomSheetContent(
            selectedType = selectedType,
            onTypeSelected = onTypeSelected,
            onConfirmClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
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
fun ScreenshotTypePickerBottomSheetContent(
    selectedType: ScreenshotContentType,
    onTypeSelected: (ScreenshotContentType) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.screenshot_type_picker_title),
            color = RecapGray900,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = ScreenshotTokens.TypePickerTitleSpacing),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = ScreenshotTokens.TypePickerGridMaxHeight),
            horizontalArrangement = Arrangement.spacedBy(ScreenshotTokens.TypePickerGridSpacing),
            verticalArrangement = Arrangement.spacedBy(ScreenshotTokens.TypePickerGridSpacing),
            contentPadding = PaddingValues(bottom = ScreenshotTokens.TypePickerConfirmTopSpacing),
        ) {
            items(
                items = ScreenshotContentType.entries,
                key = { type -> type.name },
            ) { type ->
                ScreenshotTypeChip(
                    type = type,
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        RecapButton(
            text = stringResource(R.string.screenshot_type_picker_confirm),
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Medium,
            colors = RecapBlue300
        )
    }
}

@Composable
internal fun ScreenshotTypeChip(
    type: ScreenshotContentType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryType = type.toRecapCategoryType()
    val borderColor = if (selected) categoryType.borderColor else RecapGray200
    val contentColor = if (selected) categoryType.contentColor else RecapGray900

    val shape = RoundedCornerShape(percent = ScreenshotTokens.TypeChipCornerRadius)

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = ScreenshotTokens.TypeChipMinHeight)
            .clip(shape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = shape,
        color = White,
        border = BorderStroke(
            width = ScreenshotTokens.TypeChipBorderWidth,
            color = borderColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = ScreenshotTokens.TypeChipMinHeight)
                .padding(
                    horizontal = ScreenshotTokens.TypeChipHorizontalPadding,
                    vertical = ScreenshotTokens.TypeChipVerticalPadding,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(type.toLabelResId()),
                color = contentColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Screenshot Type Picker Bottom Sheet", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotTypePickerBottomSheetPreview() {
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
                    ScreenshotTypePickerBottomSheetContent(
                        selectedType = ScreenshotContentType.PLACE_RESTAURANT,
                        onTypeSelected = {},
                        onConfirmClick = {},
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
