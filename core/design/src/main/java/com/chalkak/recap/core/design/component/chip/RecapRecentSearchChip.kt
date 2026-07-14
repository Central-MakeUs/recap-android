package com.chalkak.recap.core.design.component.chip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500

object RecapRecentSearchChipDefaults {
    val Shape = RoundedCornerShape(percent = 50)
    val HorizontalPadding = 12.dp
    val VerticalPadding = 8.dp
    val LabelIconSpacing = 10.dp
    val RemoveIconSize = 16.dp
}

@Composable
fun RecapRecentSearchChip(
    label: String,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelStyle = MaterialTheme.typography.labelLarge.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )

    Surface(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick,
        ),
        shape = RecapRecentSearchChipDefaults.Shape,
        color = RecapGray50,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RecapRecentSearchChipDefaults.HorizontalPadding,
                vertical = RecapRecentSearchChipDefaults.VerticalPadding,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                RecapRecentSearchChipDefaults.LabelIconSpacing,
            ),
        ) {
            Text(
                text = label,
                style = labelStyle,
                color = RecapGray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            RecapRecentSearchChipRemoveButton(onClick = onRemoveClick)
        }
    }
}

@Composable
private fun RecapRecentSearchChipRemoveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RecapRecentSearchChipDefaults.RemoveIconSize)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cancel_circle_16),
            contentDescription = stringResource(
                R.string.recap_recent_search_chip_remove_content_description,
            ),
            modifier = Modifier.size(RecapRecentSearchChipDefaults.RemoveIconSize),
            tint = RecapGray500,
        )
    }
}

@Preview(name = "RecapRecentSearchChip", showBackground = true)
@Composable
private fun RecapRecentSearchChipPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapRecentSearchChip(
            label = "검색어 01234",
            onClick = {},
            onRemoveClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
