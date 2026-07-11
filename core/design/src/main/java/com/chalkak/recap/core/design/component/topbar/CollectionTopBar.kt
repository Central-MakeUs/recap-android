package com.chalkak.recap.core.design.component.topbar

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900

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
                        style = MaterialTheme.typography.titleLarge,
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
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.collection_view_label),
            style = MaterialTheme.typography.labelMedium,
            color = RecapGray500,
        )
        Row(
            modifier = Modifier
                .background(
                    color = RecapGray100,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = toggleContentDescription,
                    onClick = { onViewModeChange(nextViewMode) },
                )
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CollectionViewModeIcon(
                selected = viewMode == CollectionTypeViewMode.Grid,
                iconResId = R.drawable.ic_grid_24,
            )
            CollectionViewModeIcon(
                selected = viewMode == CollectionTypeViewMode.List,
                iconResId = R.drawable.ic_list_24,
            )
        }
    }
}

@Composable
private fun CollectionViewModeIcon(
    selected: Boolean,
    iconResId: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.background
                } else {
                    RecapGray100
                },
                shape = RoundedCornerShape(6.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = if (selected) RecapGray700 else RecapGray500,
            modifier = Modifier.size(16.dp),
        )
    }
}

private val CollectionTopBarHeight = 56.dp

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
