package com.chalkak.recap.core.design.component.topbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading1

enum class CollectionTypeViewMode {
    Grid,
    List,
}

@Composable
fun CollectionTopBar(
    modifier: Modifier = Modifier,
    viewMode: CollectionTypeViewMode? = null,
    onViewModeChange: ((CollectionTypeViewMode) -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CollectionTopBarHeight)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_storage_24),
                        contentDescription = null,
                        tint = RecapBlue500,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.collection_title),
                        style = RecapHeading1,
                        fontWeight = FontWeight.Bold,
                        color = RecapGray900,
                    )
                }
                if (viewMode != null && onViewModeChange != null) {
                    CollectionViewModeToggle(
                        viewMode = viewMode,
                        onViewModeChange = onViewModeChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionViewModeToggle(
    viewMode: CollectionTypeViewMode,
    onViewModeChange: (CollectionTypeViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemSpan = CollectionViewModeToggleItemSize + CollectionViewModeToggleItemSpacing
    val selectionFraction = remember {
        Animatable(viewMode.ordinal.toFloat())
    }

    LaunchedEffect(viewMode) {
        selectionFraction.animateTo(
            targetValue = viewMode.ordinal.toFloat(),
            animationSpec = tween(CollectionViewModeHighlightDurationMillis),
        )
    }

    val highlightShape = RoundedCornerShape(CollectionViewModeToggleHighlightCornerRadius)
    val highlightOffset = itemSpan * selectionFraction.value
    val gridSelectedStrength = (1f - selectionFraction.value).coerceIn(0f, 1f)
    val listSelectedStrength = selectionFraction.value.coerceIn(0f, 1f)
    val nextViewMode = when (viewMode) {
        CollectionTypeViewMode.Grid -> CollectionTypeViewMode.List
        CollectionTypeViewMode.List -> CollectionTypeViewMode.Grid
    }
    val toggleContentDescription = stringResource(
        when (nextViewMode) {
            CollectionTypeViewMode.Grid -> R.string.collection_view_grid_content_description
            CollectionTypeViewMode.List -> R.string.collection_view_list_content_description
        },
    )
    val toggleInteractionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            text = stringResource(R.string.collection_view_label),
            style = RecapCaption1,
            color = RecapGray500,
        )
        Box(
            modifier = Modifier
                .background(
                    color = RecapGray50,
                    shape = RoundedCornerShape(CollectionViewModeToggleContainerCornerRadius),
                )
                .clickable(
                    interactionSource = toggleInteractionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = toggleContentDescription,
                    onClick = { onViewModeChange(nextViewMode) },
                ),
        ) {
            Box(
                modifier = Modifier
                    .offset(
                        x = CollectionViewModeToggleContentPaddingHorizontal + highlightOffset,
                        y = CollectionViewModeToggleContentPaddingVertical,
                    )
                    .size(CollectionViewModeToggleItemSize)
                    .clip(highlightShape)
                    .background(MaterialTheme.colorScheme.background),
            )
            Row(
                modifier = Modifier.padding(
                    horizontal = CollectionViewModeToggleContentPaddingHorizontal,
                    vertical = CollectionViewModeToggleContentPaddingVertical,
                ),
                horizontalArrangement = Arrangement.spacedBy(CollectionViewModeToggleItemSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CollectionViewModeToggleIcon(
                    iconResId = R.drawable.ic_grid_24,
                    selectedStrength = gridSelectedStrength,
                )
                CollectionViewModeToggleIcon(
                    iconResId = R.drawable.ic_list_24,
                    selectedStrength = listSelectedStrength,
                )
            }
        }
    }
}

@Composable
private fun CollectionViewModeToggleIcon(
    iconResId: Int,
    selectedStrength: Float,
    modifier: Modifier = Modifier,
) {
    val iconTint = lerp(
        RecapGray200,
        RecapGray500,
        selectedStrength.coerceIn(0f, 1f),
    )

    Box(
        modifier = modifier.size(CollectionViewModeToggleItemSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
    }
}

private const val CollectionViewModeHighlightDurationMillis = 250

private val CollectionTopBarHeight = 64.dp
private val CollectionViewModeToggleItemSize = 24.dp
private val CollectionViewModeToggleItemSpacing = 6.dp
private val CollectionViewModeToggleContentPaddingHorizontal = 5.dp
private val CollectionViewModeToggleContentPaddingVertical = 3.5.dp
private val CollectionViewModeToggleContainerCornerRadius = 5.dp
private val CollectionViewModeToggleHighlightCornerRadius = 2.dp

@Preview(name = "Collection Top Bar", showBackground = true, widthDp = 360)
@Composable
private fun CollectionTopBarPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionTopBar()
    }
}

@Preview(name = "Collection Top Bar Grid", showBackground = true, widthDp = 360)
@Composable
private fun CollectionTopBarGridPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionTopBar(
            viewMode = CollectionTypeViewMode.Grid,
            onViewModeChange = {},
        )
    }
}

@Preview(name = "Collection Top Bar List", showBackground = true, widthDp = 360)
@Composable
private fun CollectionTopBarListPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionTopBar(
            viewMode = CollectionTypeViewMode.List,
            onViewModeChange = {},
        )
    }
}

@Preview(name = "Collection View Mode Toggle Grid", showBackground = true)
@Composable
private fun CollectionViewModeToggleGridPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionViewModeTogglePreviewContent(initialViewMode = CollectionTypeViewMode.Grid)
    }
}

@Preview(name = "Collection View Mode Toggle List", showBackground = true)
@Composable
private fun CollectionViewModeToggleListPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionViewModeTogglePreviewContent(initialViewMode = CollectionTypeViewMode.List)
    }
}

@Preview(name = "Collection View Mode Toggle Interactive", showBackground = true)
@Composable
private fun CollectionViewModeToggleInteractivePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionViewModeTogglePreviewContent()
    }
}

@Composable
private fun CollectionViewModeTogglePreviewContent(
    initialViewMode: CollectionTypeViewMode = CollectionTypeViewMode.Grid,
) {
    var viewMode by remember { mutableStateOf(initialViewMode) }
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        CollectionViewModeToggle(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
        )
    }
}
