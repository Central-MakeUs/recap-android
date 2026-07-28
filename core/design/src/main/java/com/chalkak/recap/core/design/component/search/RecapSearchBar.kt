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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1

@Composable
fun RecapSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.recap_search_bar_placeholder_collection),
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    imeAction: ImeAction = ImeAction.Search,
    onSearch: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val isNavigationEntry = onClick != null
    val isEditable = enabled && !isNavigationEntry
    val submitSearch: () -> Unit = {
        onSearch?.invoke()
        keyboardController?.hide()
    }
    val surfaceModifier = if (isNavigationEntry && enabled) {
        modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
    } else {
        modifier.fillMaxWidth()
    }

    LaunchedEffect(autoFocus, isEditable) {
        if (!autoFocus || !isEditable) return@LaunchedEffect
        // Wait one frame so focus sticks after enter navigation transitions.
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Surface(
        modifier = surfaceModifier,
        shape = RecapSearchBarTokens.Shape,
        color = RecapGray50,
        contentColor = RecapGray900,
    ) {
        Row(
            modifier = Modifier
                .height(RecapSearchBarTokens.Height)
                .padding(horizontal = RecapSearchBarTokens.HorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RecapSearchBarTokens.IconTextSpacing),
        ) {
            val searchIconModifier = if (onSearch != null && isEditable) {
                Modifier
                    .size(RecapSearchBarTokens.IconSize)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = submitSearch,
                    )
            } else {
                Modifier.size(RecapSearchBarTokens.IconSize)
            }
            Icon(
                painter = painterResource(R.drawable.ic_search_24),
                contentDescription = when {
                    isNavigationEntry -> stringResource(
                        R.string.recap_search_bar_search_content_description,
                    )
                    onSearch != null -> stringResource(
                        R.string.recap_search_bar_search_content_description,
                    )
                    else -> null
                },
                modifier = searchIconModifier,
                tint = RecapGray300,
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                enabled = isEditable,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = RecapGray900,
                ),
                cursorBrush = SolidColor(RecapBlue500),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onSearch = { if (onSearch != null) submitSearch() },
                    onDone = { if (onSearch != null) submitSearch() },
                    onGo = { if (onSearch != null) submitSearch() },
                    onSend = { if (onSearch != null) submitSearch() },
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = RecapCaption1,
                                color = RecapGray300,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (value.isNotEmpty() && !isNavigationEntry) {
                RecapSearchBarClearButton(
                    enabled = isEditable,
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
            onSearch = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "RecapSearchBar navigation entry", showBackground = true, widthDp = 360)
@Composable
private fun RecapSearchBarNavigationEntryPreview() {
    RECAPTheme {
        RecapSearchBar(
            value = "",
            onValueChange = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
