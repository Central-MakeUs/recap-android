package com.chalkak.recap.core.design.component.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.Black
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapSheetHandle
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.core.design.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyUsageLimitBottomSheet(
    resetDateText: String,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = MonthlyUsageLimitBottomSheetTokens.Shape,
        containerColor = White,
        dragHandle = { MonthlyUsageLimitDragHandle() },
    ) {
        MonthlyUsageLimitBottomSheetContent(
            resetDateText = resetDateText,
            onConfirmClick = onConfirmClick,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        )
    }
}

@Composable
fun MonthlyUsageLimitBottomSheetContent(
    resetDateText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(
            start = MonthlyUsageLimitBottomSheetTokens.HorizontalPadding,
            end = MonthlyUsageLimitBottomSheetTokens.HorizontalPadding,
            top = MonthlyUsageLimitBottomSheetTokens.ContentTopPadding,
            bottom = MonthlyUsageLimitBottomSheetTokens.BottomPadding,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(MonthlyUsageLimitBottomSheetTokens.IconContainerSize),
            shape = RoundedCornerShape(MonthlyUsageLimitBottomSheetTokens.IconContainerRadius),
            color = RecapBlue50,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_error_circle_24),
                    contentDescription = null,
                    modifier = Modifier.size(MonthlyUsageLimitBottomSheetTokens.IconSize),
                    tint = RecapBlue300,
                )
            }
        }
        Spacer(Modifier.height(MonthlyUsageLimitBottomSheetTokens.IconToTitleSpacing))
        Text(
            text = stringResource(R.string.monthly_usage_limit_title),
            modifier = Modifier.fillMaxWidth(),
            style = RecapHeading2,
            color = Black,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MonthlyUsageLimitBottomSheetTokens.TitleToDescriptionSpacing))
        Text(
            text = stringResource(R.string.monthly_usage_limit_reset_description, resetDateText),
            modifier = Modifier.fillMaxWidth(),
            style = RecapBody1,
            color = RecapGray500,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MonthlyUsageLimitBottomSheetTokens.DescriptionToButtonSpacing))
        RecapButton(
            text = stringResource(R.string.monthly_usage_limit_confirm_button),
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 15.dp),
        )
    }
}

@Composable
private fun MonthlyUsageLimitDragHandle() {
    Surface(
        modifier = Modifier
            .padding(vertical = 13.dp)
            .size(width = 43.dp, height = 5.dp),
        shape = RoundedCornerShape(5.dp),
        color = RecapSheetHandle,
        content = {},
    )
}

private object MonthlyUsageLimitBottomSheetTokens {
    val Shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val HorizontalPadding = 16.dp
    val ContentTopPadding = 16.dp
    val BottomPadding = 20.dp
    val IconContainerSize = 40.dp
    val IconContainerRadius = 10.dp
    val IconSize = 20.dp
    val IconToTitleSpacing = 18.dp
    val TitleToDescriptionSpacing = 12.dp
    val DescriptionToButtonSpacing = 35.dp
}

@Preview(name = "Monthly Usage Limit", showBackground = true, widthDp = 375)
@Preview(
    name = "Monthly Usage Limit Large Font",
    showBackground = true,
    widthDp = 320,
    fontScale = 1.5f
)
@Composable
private fun MonthlyUsageLimitBottomSheetPreview() {
    RECAPTheme(dynamicColor = false) {
        Surface(
            shape = MonthlyUsageLimitBottomSheetTokens.Shape,
            color = White,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MonthlyUsageLimitDragHandle()
                MonthlyUsageLimitBottomSheetContent(
                    resetDateText = stringResource(R.string.monthly_usage_limit_preview_reset_date),
                    onConfirmClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
