package com.chalkak.recap.feature.screenshot

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

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
            style = RecapHeading3,
            modifier = Modifier.padding(bottom = ScreenshotTypePickerBottomSheetTokens.TypePickerTitleSpacing),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(
                ScreenshotTypePickerBottomSheetTokens.TypePickerVerticalSpacing,
            ),
        ) {
            TypePickerOptions.chunked(ScreenshotTypePickerBottomSheetTokens.TypePickerColumns)
                .forEach { rowTypes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            ScreenshotTypePickerBottomSheetTokens.TypePickerHorizontalSpacing,
                        ),
                    ) {
                        rowTypes.forEach { type ->
                            ScreenshotTypeChip(
                                type = type,
                                selected = type == selectedType,
                                onClick = { onTypeSelected(type) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
        }
        Spacer(modifier = Modifier.height(ScreenshotTypePickerBottomSheetTokens.TypePickerConfirmTopSpacing))
        RecapButton(
            text = stringResource(R.string.screenshot_type_picker_confirm),
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(),
            size = RecapButtonSize.Medium,
            colors = RecapBlue300,
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
    val containerColor = if (selected) categoryType.tintColor else White
    val borderColor = if (selected) categoryType.borderColor else RecapGray200
    val contentColor = if (selected) categoryType.contentColor else RecapGray900
    val shape = RoundedCornerShape(percent = ScreenshotTypePickerBottomSheetTokens.TypeChipCornerRadius)

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = ScreenshotTypePickerBottomSheetTokens.TypeChipMinHeight)
            .clip(shape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(
            width = ScreenshotTypePickerBottomSheetTokens.TypeChipBorderWidth,
            color = borderColor,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = ScreenshotTypePickerBottomSheetTokens.TypeChipMinHeight)
                .padding(
                    horizontal = ScreenshotTypePickerBottomSheetTokens.TypeChipHorizontalPadding,
                    vertical = ScreenshotTypePickerBottomSheetTokens.TypeChipVerticalPadding,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(categoryType.labelResId),
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
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
                        selectedType = ScreenshotContentType.SCHEDULE,
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

/** RecapCategoryType 표시 순서와 동일한 3x3 배치. */
private val TypePickerOptions = listOf(
    ScreenshotContentType.SHOPPING,
    ScreenshotContentType.PLACE,
    ScreenshotContentType.SCHEDULE,
    ScreenshotContentType.KNOWLEDGE,
    ScreenshotContentType.CONTENT,
    ScreenshotContentType.BENEFIT,
    ScreenshotContentType.RECORD,
    ScreenshotContentType.JOB,
    ScreenshotContentType.ETC,
)

private object ScreenshotTypePickerBottomSheetTokens {
    const val TypePickerColumns = 3
    val TypePickerTitleSpacing = 23.dp
    val TypePickerHorizontalSpacing = 8.dp
    val TypePickerVerticalSpacing = 20.dp
    val TypePickerConfirmTopSpacing = 61.dp
    val TypeChipHorizontalPadding = 4.dp
    val TypeChipVerticalPadding = 14.dp
    val TypeChipMinHeight = 48.dp
    val TypeChipBorderWidth = 1.dp
    val TypeChipCornerRadius = 50
}
