package com.chalkak.recap.core.design.component.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.theme.RECAPTheme

@Immutable
data class RecapCategoryChipColors(
    val border: Color,
    val content: Color,
)

object RecapCategoryChipDefaults {
    val BorderWidth = 1.dp
    val HorizontalPadding = 10.dp
    val VerticalPadding = 4.dp

    fun colors(type: RecapCategoryType): RecapCategoryChipColors {
        return RecapCategoryChipColors(
            border = type.borderColor,
            content = type.contentColor,
        )
    }
}

@Composable
fun RecapCategoryChip(
    type: RecapCategoryType,
    modifier: Modifier = Modifier,
    colors: RecapCategoryChipColors = RecapCategoryChipDefaults.colors(type),
) {
    RecapCategoryChip(
        label = stringResource(type.labelResId),
        colors = colors,
        modifier = modifier,
    )
}

@Composable
fun RecapCategoryChip(
    label: String,
    colors: RecapCategoryChipColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = Color.Transparent,
        border = BorderStroke(
            width = RecapCategoryChipDefaults.BorderWidth,
            color = colors.border,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = RecapCategoryChipDefaults.HorizontalPadding,
                vertical = RecapCategoryChipDefaults.VerticalPadding,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = colors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Recap Category Chips", showBackground = true)
@Composable
private fun RecapCategoryChipsPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapCategoryChipsPreviewContent()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecapCategoryChipsPreviewContent() {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryChip(type = type)
            }
        }
    }
}
