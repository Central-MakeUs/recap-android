package com.chalkak.recap.core.design.component.chip

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.theme.PretendardFontFamily
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray900

@Immutable
data class RecapCategoryChipColors(
    val container: Color,
    val border: Color,
    val content: Color,
    val icon: Color = border,
)

object RecapCategoryChipDefaults {
    val BorderWidth = 1.dp
    val RoundHorizontalPadding = 25.5.dp
    val RoundVerticalPadding = 14.dp
    val IconContainerSize = 20.dp
    val IconSize = 12.dp
    val IconCornerRadius = 6.dp
    val IconTextSpacing = 8.dp
    val TextChipFontSize = 10.sp

    fun colors(type: RecapCategoryType): RecapCategoryChipColors {
        return RecapCategoryChipColors(
            container = type.tintColor,
            border = type.borderColor,
            content = type.contentColor,
            icon = type.borderColor,
        )
    }

    fun textChipStyle(fontSize: TextUnit = TextChipFontSize): TextStyle {
        val sizeSp = fontSize.value
        return TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            lineHeight = (sizeSp * 1.4f).sp,
            letterSpacing = (sizeSp * -0.02f).sp,
        )
    }
}

/** 기존 카테고리 round chip. [RecapCategoryRoundChip]의 별칭. */
@Composable
fun RecapCategoryChip(
    type: RecapCategoryType,
    modifier: Modifier = Modifier,
    colors: RecapCategoryChipColors = RecapCategoryChipDefaults.colors(type),
) {
    RecapCategoryRoundChip(
        type = type,
        modifier = modifier,
        colors = colors,
    )
}

/** 기존 카테고리 round chip. [RecapCategoryRoundChip]의 별칭. */
@Composable
fun RecapCategoryChip(
    label: String,
    colors: RecapCategoryChipColors,
    modifier: Modifier = Modifier,
) {
    RecapCategoryRoundChip(
        label = label,
        colors = colors,
        modifier = modifier,
    )
}

@Composable
fun RecapCategoryRoundChip(
    type: RecapCategoryType,
    modifier: Modifier = Modifier,
    colors: RecapCategoryChipColors = RecapCategoryChipDefaults.colors(type),
) {
    RecapCategoryRoundChip(
        label = stringResource(type.labelResId),
        colors = colors,
        modifier = modifier,
    )
}

@Composable
fun RecapCategoryRoundChip(
    label: String,
    colors: RecapCategoryChipColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = colors.container,
        border = BorderStroke(
            width = RecapCategoryChipDefaults.BorderWidth,
            color = colors.border,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = RecapCategoryChipDefaults.RoundHorizontalPadding,
                vertical = RecapCategoryChipDefaults.RoundVerticalPadding,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = colors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun RecapCategoryTextChip(
    type: RecapCategoryType,
    modifier: Modifier = Modifier,
    colors: RecapCategoryChipColors = RecapCategoryChipDefaults.colors(type),
    textSize: TextUnit = RecapCategoryChipDefaults.TextChipFontSize,
) {
    RecapCategoryTextChip(
        label = stringResource(type.labelResId),
        colors = colors,
        modifier = modifier,
        textSize = textSize,
    )
}

@Composable
fun RecapCategoryTextChip(
    label: String,
    colors: RecapCategoryChipColors,
    modifier: Modifier = Modifier,
    textSize: TextUnit = RecapCategoryChipDefaults.TextChipFontSize,
) {
    Text(
        text = label,
        modifier = modifier,
        style = RecapCategoryChipDefaults.textChipStyle(textSize),
        color = colors.content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun RecapCategoryTextChipWithIcon(
    type: RecapCategoryType,
    modifier: Modifier = Modifier,
    colors: RecapCategoryChipColors = RecapCategoryChipDefaults.colors(type),
) {
    RecapCategoryTextChipWithIcon(
        label = stringResource(type.labelResId),
        iconResId = type.iconResId,
        colors = colors,
        modifier = modifier,
    )
}

@Composable
fun RecapCategoryTextChipWithIcon(
    label: String,
    @DrawableRes iconResId: Int,
    colors: RecapCategoryChipColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            RecapCategoryChipDefaults.IconTextSpacing,
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(RecapCategoryChipDefaults.IconContainerSize)
                .clip(RoundedCornerShape(RecapCategoryChipDefaults.IconCornerRadius)),
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = colors.icon,
                modifier = Modifier.size(RecapCategoryChipDefaults.IconSize),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RecapGray900,
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
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        RecapCategoryChipVariantPreviewSection(title = "Round") {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryRoundChip(type = type)
            }
        }
        RecapCategoryChipVariantPreviewSection(title = "Text") {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryTextChip(type = type)
            }
        }
        RecapCategoryChipVariantPreviewSection(title = "TextWithIcon") {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryTextChipWithIcon(type = type)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecapCategoryChipVariantPreviewSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = RecapGray900,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}
