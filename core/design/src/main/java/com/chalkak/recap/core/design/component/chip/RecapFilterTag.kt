package com.chalkak.recap.core.design.component.chip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray700

@Immutable
data class RecapFilterTagOption(
    val id: String,
    val label: String,
)

object RecapFilterTagDefaults {
    val Shape = RoundedCornerShape(size = 16.dp)
    val ItemHeight = 32.dp
    val HorizontalPadding = 10.dp
    val VerticalPadding = 5.dp
    val LabelIconSpacing = 10.dp
    val IconSize = 16.dp
    val ExpandedElevation = 2.dp
    const val AnimationDurationMillis = 180
}

@Composable
fun RecapFilterTag(
    options: List<RecapFilterTagOption>,
    selectedOptionId: String,
    onOptionSelected: (RecapFilterTagOption) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    require(options.isNotEmpty()) { "RecapFilterTag requires at least one option." }

    var internalExpanded by remember { mutableStateOf(false) }
    val isExpanded = expanded ?: internalExpanded
    val setExpanded: (Boolean) -> Unit = { next ->
        if (expanded == null) {
            internalExpanded = next
        }
        onExpandedChange?.invoke(next)
    }

    val selectedOption = options.firstOrNull { it.id == selectedOptionId } ?: options.first()
    val unselectedOptions = options.filter { it.id != selectedOption.id }
    val menuVisibleState = remember { MutableTransitionState(false) }
    menuVisibleState.targetState = isExpanded && unselectedOptions.isNotEmpty()
    val isMenuShowing = menuVisibleState.currentState ||
        menuVisibleState.targetState ||
        !menuVisibleState.isIdle

    val caretRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = RecapFilterTagDefaults.AnimationDurationMillis),
        label = "RecapFilterTagCaretRotation",
    )
    val expandedElevation by animateDpAsState(
        targetValue = if (menuVisibleState.targetState) {
            RecapFilterTagDefaults.ExpandedElevation
        } else {
            0.dp
        },
        animationSpec = tween(durationMillis = RecapFilterTagDefaults.AnimationDurationMillis),
        label = "RecapFilterTagExpandedElevation",
    )
    val toggleDescription = if (isExpanded) {
        stringResource(R.string.recap_filter_tag_collapse_content_description)
    } else {
        stringResource(R.string.recap_filter_tag_expand_content_description)
    }

    Box(modifier = modifier) {
        // Keeps layout size collapsed so siblings are not pushed down.
        RecapFilterTagSurface(
            selectedOption = selectedOption,
            caretRotation = caretRotation,
            toggleDescription = toggleDescription,
            onSelectedClick = { setExpanded(!isExpanded) },
        )

        if (isMenuShowing) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { setExpanded(false) },
                properties = PopupProperties(focusable = true),
            ) {
                RecapFilterTagSurface(
                    selectedOption = selectedOption,
                    caretRotation = caretRotation,
                    toggleDescription = toggleDescription,
                    onSelectedClick = { setExpanded(false) },
                    unselectedOptions = unselectedOptions,
                    onOptionSelected = { option ->
                        onOptionSelected(option)
                        setExpanded(false)
                    },
                    optionsVisibleState = menuVisibleState,
                    shadowElevation = expandedElevation,
                )
            }
        }
    }
}

@Composable
private fun RecapFilterTagSurface(
    selectedOption: RecapFilterTagOption,
    caretRotation: Float,
    toggleDescription: String,
    onSelectedClick: () -> Unit,
    modifier: Modifier = Modifier,
    unselectedOptions: List<RecapFilterTagOption> = emptyList(),
    onOptionSelected: (RecapFilterTagOption) -> Unit = {},
    optionsVisibleState: MutableTransitionState<Boolean>? = null,
    shadowElevation: Dp = 0.dp,
) {
    Surface(
        modifier = modifier,
        shape = RecapFilterTagDefaults.Shape,
        color = RecapGray50,
        shadowElevation = shadowElevation,
    ) {
        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
            RecapFilterTagItem(
                label = selectedOption.label,
                labelColor = RecapGray700,
                onClick = onSelectedClick,
                modifier = Modifier.semantics {
                    contentDescription = toggleDescription
                    stateDescription = selectedOption.label
                },
                trailing = {
                    Icon(
                        painter = painterResource(R.drawable.ic_dropdown_16),
                        contentDescription = null,
                        modifier = Modifier
                            .size(RecapFilterTagDefaults.IconSize)
                            .graphicsLayer { rotationZ = caretRotation },
                        tint = RecapGray300,
                    )
                },
            )

            if (optionsVisibleState != null) {
                AnimatedVisibility(
                    visibleState = optionsVisibleState,
                    enter = expandVertically(
                        animationSpec = tween(
                            durationMillis = RecapFilterTagDefaults.AnimationDurationMillis,
                        ),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = RecapFilterTagDefaults.AnimationDurationMillis,
                        ),
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(
                            durationMillis = RecapFilterTagDefaults.AnimationDurationMillis,
                        ),
                        shrinkTowards = Alignment.Top,
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = RecapFilterTagDefaults.AnimationDurationMillis,
                        ),
                    ),
                ) {
                    Column {
                        unselectedOptions.forEach { option ->
                            RecapFilterTagItem(
                                label = option.label,
                                labelColor = RecapGray300,
                                onClick = { onOptionSelected(option) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecapFilterTagItem(
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val labelStyle = MaterialTheme.typography.labelMedium.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )

    Row(
        modifier = modifier
            .height(RecapFilterTagDefaults.ItemHeight)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = RecapFilterTagDefaults.HorizontalPadding,
                vertical = RecapFilterTagDefaults.VerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            RecapFilterTagDefaults.LabelIconSpacing,
        ),
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing?.invoke()
    }
}

@Preview(name = "RecapFilterTag collapsed", showBackground = true, backgroundColor = 0xFF4A4A4A)
@Composable
private fun RecapFilterTagCollapsedPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapFilterTagPreviewContent(expanded = false)
    }
}

@Preview(name = "RecapFilterTag expanded", showBackground = true, backgroundColor = 0xFF4A4A4A)
@Composable
private fun RecapFilterTagExpandedPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapFilterTagPreviewContent(expanded = true)
    }
}

@Composable
private fun RecapFilterTagPreviewContent(
    expanded: Boolean,
) {
    var selectedOptionId by remember { mutableStateOf("latest") }
    var isExpanded by remember { mutableStateOf(expanded) }
    val options = listOf(
        RecapFilterTagOption(
            id = "latest",
            label = stringResource(R.string.collection_sort_latest),
        ),
        RecapFilterTagOption(
            id = "favorite",
            label = stringResource(R.string.collection_sort_oldest),
        ),
    )

    Column(modifier = Modifier.padding(24.dp)) {
        RecapFilterTag(
            options = options,
            selectedOptionId = selectedOptionId,
            onOptionSelected = { selectedOptionId = it.id },
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
        )
        Text(
            text = "Below content stays put",
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray300,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
