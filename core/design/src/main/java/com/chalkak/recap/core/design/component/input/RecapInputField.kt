package com.chalkak.recap.core.design.component.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapError

@Composable
fun RecapInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    maxLength: Int? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null,
) {
    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by resolvedInteractionSource.collectIsFocusedAsState()
    val showCounter = maxLength != null
    val showError = isError && !errorMessage.isNullOrBlank()
    val resolvedMinLines = if (singleLine) 1 else minLines.coerceAtLeast(1)
    val resolvedMaxLines = if (singleLine) 1 else maxLines.coerceAtLeast(resolvedMinLines)
    val borderColor = if (isFocused) RecapBlue300 else RecapGray200

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RecapFieldTokens.LabelSpacing),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RecapFieldTokens.Shape,
            color = Color.White,
            border = BorderStroke(
                width = RecapFieldTokens.BorderWidth,
                color = borderColor,
            ),
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    val nextValue = if (maxLength != null) {
                        newValue.take(maxLength)
                    } else {
                        newValue
                    }
                    onValueChange(nextValue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (singleLine) {
                            Modifier.defaultMinSize(minHeight = RecapFieldTokens.SingleLineMinHeight)
                        } else {
                            Modifier.heightIn(min = RecapFieldTokens.MultiLineMinHeight)
                        },
                    )
                    .padding(RecapFieldTokens.ContentPadding),
                enabled = enabled,
                singleLine = singleLine,
                minLines = resolvedMinLines,
                maxLines = resolvedMaxLines,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = RecapGray900,
                ),
                cursorBrush = SolidColor(RecapBlue500),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                interactionSource = resolvedInteractionSource,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = if (singleLine) {
                            Alignment.CenterStart
                        } else {
                            Alignment.TopStart
                        },
                    ) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RecapGray300,
                                maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }

        if (showError || showCounter) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                if (showError) {
                    RecapInputFieldErrorMessage(
                        message = errorMessage,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (showCounter) {
                    Text(
                        text = stringResource(
                            R.string.recap_input_field_character_counter,
                            value.length,
                            maxLength,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = RecapGray300,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecapInputFieldErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RecapInputFieldTokens.ErrorIconSpacing),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error_circle_16),
            contentDescription = null,
            modifier = Modifier.size(RecapInputFieldTokens.ErrorIconSize),
            tint = RecapError,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.labelMedium,
            color = RecapError,
        )
    }
}

private object RecapInputFieldTokens {
    val ErrorIconSize = 16.dp
    val ErrorIconSpacing = 4.dp
}

const val showRecapInputFieldBackground = true

@Preview(name = "RecapInputField filled", showBackground = showRecapInputFieldBackground, widthDp = 360)
@Composable
private fun RecapInputFieldFilledPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapInputField(
            value = stringResource(R.string.recap_input_field_preview_text),
            onValueChange = {},
            label = stringResource(R.string.recap_input_field_preview_label),
            placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "RecapInputField placeholder", showBackground = showRecapInputFieldBackground, widthDp = 360)
@Composable
private fun RecapInputFieldPlaceholderPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapInputField(
            value = "",
            onValueChange = {},
            label = stringResource(R.string.recap_input_field_preview_label),
            placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "RecapInputField focused", showBackground = showRecapInputFieldBackground, widthDp = 360)
@Composable
private fun RecapInputFieldFocusedPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapInputFieldFocusedPreviewContent(
            value = stringResource(R.string.recap_input_field_preview_text),
        )
    }
}

@Preview(name = "RecapInputField typing", showBackground = showRecapInputFieldBackground, widthDp = 360)
@Composable
private fun RecapInputFieldTypingPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapInputFieldFocusedPreviewContent(
            value = stringResource(R.string.recap_input_field_preview_typing_text),
        )
    }
}

@Preview(name = "RecapInputField multiline", showBackground = showRecapInputFieldBackground, widthDp = 360)
@Composable
private fun RecapInputFieldMultilinePreview() {
    RECAPTheme(dynamicColor = false) {
        RecapInputField(
            value = stringResource(R.string.recap_input_field_preview_multiline_text),
            onValueChange = {},
            label = stringResource(R.string.recap_input_field_preview_label),
            placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
            singleLine = false,
            minLines = 4,
            maxLength = 300,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "RecapInputField error", showBackground = showRecapInputFieldBackground, widthDp = 360)
@Composable
private fun RecapInputFieldErrorPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapInputField(
            value = "",
            onValueChange = {},
            label = stringResource(R.string.recap_input_field_preview_label),
            placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
            isError = true,
            errorMessage = stringResource(R.string.recap_input_field_preview_error_message),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun RecapInputFieldFocusedPreviewContent(
    value: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RecapFieldTokens.Shape,
        color = Color.Transparent,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(RecapFieldTokens.LabelSpacing),
        ) {
            Text(
                text = stringResource(R.string.recap_input_field_preview_label),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray900,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RecapFieldTokens.Shape,
                color = Color.White,
                border = BorderStroke(
                    width = RecapFieldTokens.BorderWidth,
                    color = RecapBlue300,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = RecapFieldTokens.SingleLineMinHeight)
                        .padding(RecapFieldTokens.ContentPadding),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RecapGray900,
                    )
                }
            }
        }
    }
}
