package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.input.RecapInputField
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.capture.ReportReason

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotReportBottomSheet(
    selectedReason: ReportReason?,
    detail: String,
    onDismissRequest: () -> Unit,
    onReasonSelected: (ReportReason) -> Unit,
    onDetailChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    onSubmitClick: () -> Unit,
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
        ScreenshotReportBottomSheetContent(
            selectedReason = selectedReason,
            detail = detail,
            onReasonSelected = onReasonSelected,
            onDetailChange = onDetailChange,
            onCloseClick = onCloseClick,
            onSubmitClick = onSubmitClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = ScreenshotTokens.BottomSheetContentTopPadding,
                    bottom = ScreenshotTokens.BottomSheetBottomPadding,
                ),
        )
    }
}

@Composable
fun ScreenshotReportBottomSheetContent(
    selectedReason: ReportReason?,
    detail: String,
    onReasonSelected: (ReportReason) -> Unit,
    onDetailChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scrollState = rememberScrollState()
    val otherBringIntoViewRequester = remember { BringIntoViewRequester() }
    val otherFieldInteractionSource = remember { MutableInteractionSource() }
    val isOtherFieldFocused by otherFieldInteractionSource.collectIsFocusedAsState()
    val imeBottomPx = WindowInsets.ime.getBottom(LocalDensity.current)
    val showOtherField = selectedReason == ReportReason.OTHER

    LaunchedEffect(isOtherFieldFocused, imeBottomPx, showOtherField) {
        if (showOtherField && isOtherFieldFocused) {
            otherBringIntoViewRequester.bringIntoView()
        }
    }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .verticalScroll(scrollState),
        ) {
            Text(
                text = stringResource(R.string.screenshot_report_title),
                modifier = Modifier.padding(
                    horizontal = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                ),
                style = RecapHeading3,
                color = RecapGray900,
            )
            Text(
                text = stringResource(R.string.screenshot_report_description),
                modifier = Modifier.padding(
                    start = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                    top = ScreenshotReportBottomSheetTokens.TitleToDescriptionSpacing,
                    end = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                ),
                style = RecapBody2,
                color = RecapGray700,
            )
            Spacer(
                modifier = Modifier.height(
                    ScreenshotReportBottomSheetTokens.DescriptionToOptionsSpacing,
                ),
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(
                    ScreenshotReportBottomSheetTokens.OptionSpacing,
                ),
            ) {
                ReportReasonEntries.forEach { entry ->
                    if (entry.reason == ReportReason.OTHER) {
                        Column(
                            modifier = Modifier.bringIntoViewRequester(otherBringIntoViewRequester),
                        ) {
                            ScreenshotReportReasonRow(
                                label = stringResource(entry.labelResId),
                                selected = selectedReason == entry.reason,
                                enabled = enabled,
                                onClick = { onReasonSelected(entry.reason) },
                            )
                            AnimatedVisibility(
                                visible = showOtherField,
                                enter = expandVertically(
                                    animationSpec = tween(
                                        durationMillis = ScreenshotReportBottomSheetTokens.FieldAnimationDurationMillis,
                                        easing = FastOutSlowInEasing,
                                    ),
                                ) + fadeIn(
                                    animationSpec = tween(
                                        durationMillis = ScreenshotReportBottomSheetTokens.FieldAnimationDurationMillis,
                                        easing = FastOutSlowInEasing,
                                    ),
                                ),
                                exit = shrinkVertically(
                                    animationSpec = tween(
                                        durationMillis = ScreenshotReportBottomSheetTokens.FieldAnimationDurationMillis,
                                        easing = FastOutSlowInEasing,
                                    ),
                                ) + fadeOut(
                                    animationSpec = tween(
                                        durationMillis = ScreenshotReportBottomSheetTokens.FieldAnimationDurationMillis,
                                        easing = FastOutSlowInEasing,
                                    ),
                                ),
                            ) {
                                RecapInputField(
                                    value = detail,
                                    onValueChange = onDetailChange,
                                    placeholder = stringResource(R.string.screenshot_report_detail_placeholder),
                                    enabled = enabled,
                                    singleLine = true,
                                    interactionSource = otherFieldInteractionSource,
                                    modifier = Modifier.padding(
                                        start = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                                        top = ScreenshotReportBottomSheetTokens.OtherFieldTopSpacing,
                                        end = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                                    ),
                                )
                            }
                        }
                    } else {
                        ScreenshotReportReasonRow(
                            label = stringResource(entry.labelResId),
                            selected = selectedReason == entry.reason,
                            enabled = enabled,
                            onClick = { onReasonSelected(entry.reason) },
                        )
                    }
                }
            }
        }
        AnimatedContent(
            targetState = selectedReason != null,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = ScreenshotReportBottomSheetTokens.ButtonFadeDurationMillis,
                        delayMillis = ScreenshotReportBottomSheetTokens.ButtonFadeDurationMillis,
                    ),
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = ScreenshotReportBottomSheetTokens.ButtonFadeDurationMillis,
                    ),
                )
            },
            label = "screenshot_report_button",
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                    top = ScreenshotReportBottomSheetTokens.OptionsToButtonSpacing,
                    end = ScreenshotReportBottomSheetTokens.ContentHorizontalPadding,
                ),
        ) { hasSelection ->
            if (hasSelection) {
                RecapButton(
                    text = stringResource(R.string.screenshot_report_submit),
                    onClick = onSubmitClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    size = RecapButtonSize.Large,
                    colors = RecapButtonDefaults.primaryColors(),
                )
            } else {
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
    }
}

@Composable
private fun ScreenshotReportReasonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showPressed by remember { mutableStateOf(false) }
    LaunchedEffect(enabled, isPressed) {
        if (enabled && isPressed) {
            showPressed = true
        } else {
            showPressed = false
        }
    }
    val pressAnimationSpec = tween<Float>(
        durationMillis = ScreenshotReportBottomSheetTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val colorAnimationSpec = tween<Color>(
        durationMillis = ScreenshotReportBottomSheetTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val scale by animateFloatAsState(
        targetValue = if (showPressed) {
            ScreenshotReportBottomSheetTokens.PressedScale
        } else {
            1f
        },
        animationSpec = pressAnimationSpec,
        label = "screenshot_report_reason_press_scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (showPressed) RecapGray50 else White,
        animationSpec = colorAnimationSpec,
        label = "screenshot_report_reason_container_color",
    )
    val rowShape = RoundedCornerShape(ScreenshotReportBottomSheetTokens.RowCornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenshotReportBottomSheetTokens.ClickAreaHorizontalPadding)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(rowShape)
            .background(containerColor)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(
                horizontal = ScreenshotReportBottomSheetTokens.ContentInsetHorizontal,
                vertical = ScreenshotReportBottomSheetTokens.OptionVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            ScreenshotReportBottomSheetTokens.RadioToLabelSpacing,
        ),
    ) {
        ScreenshotReportRadioIndicator(selected = selected)
        Text(
            text = label,
            style = RecapBody1,
            color = if (enabled) RecapGray700 else RecapGray700.copy(alpha = 0.38f),
        )
    }
}

@Composable
private fun ScreenshotReportRadioIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ScreenshotReportBottomSheetTokens.RadioOuterSize)
            .clip(CircleShape)
            .border(
                width = ScreenshotReportBottomSheetTokens.RadioBorderWidth,
                color = if (selected) RecapBlue300 else RecapGray200,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(ScreenshotReportBottomSheetTokens.RadioInnerSize)
                    .clip(CircleShape)
                    .background(RecapBlue300),
            )
        }
    }
}

private data class ReportReasonEntry(
    val reason: ReportReason,
    val labelResId: Int,
)

private val ReportReasonEntries = listOf(
    ReportReasonEntry(
        reason = ReportReason.INACCURATE_CONTENT,
        labelResId = R.string.screenshot_report_reason_inaccurate,
    ),
    ReportReasonEntry(
        reason = ReportReason.INAPPROPRIATE_CONTENT,
        labelResId = R.string.screenshot_report_reason_inappropriate,
    ),
    ReportReasonEntry(
        reason = ReportReason.SENSITIVE_INFO,
        labelResId = R.string.screenshot_report_reason_sensitive,
    ),
    ReportReasonEntry(
        reason = ReportReason.OTHER,
        labelResId = R.string.screenshot_report_reason_other,
    ),
)

@Preview(name = "Screenshot Report Bottom Sheet Idle", showBackground = false, widthDp = 360)
@Composable
private fun ScreenshotReportBottomSheetIdlePreview() {
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
                    ScreenshotReportBottomSheetContent(
                        selectedReason = null,
                        detail = "",
                        onReasonSelected = {},
                        onDetailChange = {},
                        onCloseClick = {},
                        onSubmitClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = ScreenshotTokens.BottomSheetContentTopPadding,
                                bottom = ScreenshotTokens.BottomSheetBottomPadding,
                            ),
                    )
                }
            }
        }
    }
}

@Preview(name = "Screenshot Report Bottom Sheet Other", showBackground = false, widthDp = 360)
@Composable
private fun ScreenshotReportBottomSheetOtherPreview() {
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
                    ScreenshotReportBottomSheetContent(
                        selectedReason = ReportReason.OTHER,
                        detail = "",
                        onReasonSelected = {},
                        onDetailChange = {},
                        onCloseClick = {},
                        onSubmitClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = ScreenshotTokens.BottomSheetContentTopPadding,
                                bottom = ScreenshotTokens.BottomSheetBottomPadding,
                            ),
                    )
                }
            }
        }
    }
}

private object ScreenshotReportBottomSheetTokens {
    val TitleToDescriptionSpacing = 10.dp
    val DescriptionToOptionsSpacing = 34.dp
    val OptionSpacing = 4.dp
    val OptionVerticalPadding = 8.dp
    val ContentHorizontalPadding = 22.dp
    val ClickAreaHorizontalPadding = 10.dp
    val ContentInsetHorizontal = ContentHorizontalPadding - ClickAreaHorizontalPadding
    val RowCornerRadius = 10.dp
    const val PressedScale = 0.9875f
    const val PressAnimationDurationMillis = 150
    const val ButtonFadeDurationMillis = 150
    const val FieldAnimationDurationMillis = 200
    val RadioToLabelSpacing = 12.dp
    val RadioOuterSize = 20.dp
    val RadioInnerSize = 10.dp
    val RadioBorderWidth = 2.dp
    val OtherFieldTopSpacing = 17.dp
    val OptionsToButtonSpacing = 21.dp
}
