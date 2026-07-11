package com.chalkak.recap.core.design.component.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun RecapSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.recap_search_bar_placeholder_collection),
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Search,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RecapSearchBarTokens.Shape,
        color = RecapGray100,
        contentColor = RecapGray900,
    ) {
        Row(
            modifier = Modifier
                .height(RecapSearchBarTokens.Height)
                .padding(horizontal = RecapSearchBarTokens.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RecapSearchBarTokens.IconTextSpacing),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search_24),
                contentDescription = null,
                modifier = Modifier.size(RecapSearchBarTokens.IconSize),
                tint = RecapGray300,
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = RecapGray900,
                ),
                cursorBrush = SolidColor(RecapBlue500),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RecapGray300,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (value.isNotEmpty()) {
                RecapSearchBarClearButton(
                    enabled = enabled,
                    onClick = { onValueChange("") },
                )
            }
        }
    }
}

@Composable
private fun RecapSearchBarClearButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(RecapSearchBarTokens.ClearIconSize)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cancel_circle_16),
            contentDescription = stringResource(
                R.string.recap_search_bar_clear_content_description,
            ),
            modifier = Modifier.size(RecapSearchBarTokens.ClearIconSize),
            tint = RecapGray300,
        )
    }
}

private object RecapSearchBarTokens {
    val Height = 44.dp
    val HorizontalPadding = 16.dp
    val IconTextSpacing = 8.dp
    val IconSize = 24.dp
    val ClearIconSize = 16.dp
    val Shape = RoundedCornerShape(percent = 50)
}

@Preview(name = "RecapSearchBar empty", showBackground = true, widthDp = 360)
@Composable
private fun RecapSearchBarPreview() {
    RECAPTheme {
        RecapSearchBar(
            value = "",
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "RecapSearchBar with query", showBackground = true, widthDp = 360)
@Composable
private fun RecapSearchBarFilledPreview() {
    RECAPTheme {
        RecapSearchBar(
            value = "맛집",
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
