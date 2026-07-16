package com.chalkak.recap.core.design.component.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography
import com.chalkak.recap.core.design.theme.White

@Composable
fun RecapPopup(
    title: String,
    description: String,
    confirmButtonText: String,
    cancelButtonText: String,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonColor: Color = RecapError,
    confirmButtonContentColor: Color = White,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
    ),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        RecapPopupContent(
            title = title,
            description = description,
            confirmButtonText = confirmButtonText,
            cancelButtonText = cancelButtonText,
            onConfirmClick = onConfirmClick,
            onCancelClick = onCancelClick,
            modifier = modifier,
            confirmButtonColor = confirmButtonColor,
            confirmButtonContentColor = confirmButtonContentColor,
        )
    }
}

@Composable
fun RecapPopupContent(
    title: String,
    description: String,
    confirmButtonText: String,
    cancelButtonText: String,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    confirmButtonColor: Color = RecapError,
    confirmButtonContentColor: Color = White,
) {
    Surface(
        modifier = modifier
            .padding(horizontal = RecapPopupTokens.DialogHorizontalMargin)
            .widthIn(max = RecapPopupTokens.MaxWidth)
            .fillMaxWidth(),
        shape = RoundedCornerShape(RecapPopupTokens.ContainerCornerRadius),
        color = White,
        contentColor = RecapGray900,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = RecapPopupTokens.ContentVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RecapPopupTokens.SectionSpacing),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = RecapPopupTokens.ContentHorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RecapPopupTokens.TextSpacing),
            ) {
                Text(
                    text = title,
                    color = RecapGray900,
                    style = RecapTypography.RecapHeading2,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = description,
                    color = RecapGray500,
                    style = RecapTypography.RecapBody2,
                    textAlign = TextAlign.Center,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RecapPopupTokens.ButtonRowHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(RecapPopupTokens.ButtonSpacing),
            ) {
                RecapButton(
                    text = cancelButtonText,
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f),
                    size = RecapButtonSize.Medium,
                    colors = RecapButtonDefaults.neutralColors(),
                )
                RecapButton(
                    text = confirmButtonText,
                    onClick = onConfirmClick,
                    modifier = Modifier.weight(1f),
                    size = RecapButtonSize.Medium,
                    colors = confirmButtonColor,
                    contentColor = confirmButtonContentColor,
                )
            }
        }
    }
}

private object RecapPopupTokens {
    val DialogHorizontalMargin = 24.dp
    val MaxWidth = 320.dp
    val ContainerCornerRadius = 24.dp
    val ContentHorizontalPadding = 28.dp
    val ContentVerticalPadding = 24.dp
    val SectionSpacing = 24.dp
    val TextSpacing = 8.dp
    val ButtonRowHorizontalPadding = 21.dp
    val ButtonSpacing = 14.dp
}

@Preview(name = "RecapPopup Destructive", showBackground = true, widthDp = 360, heightDp = 240)
@Composable
private fun RecapPopupDestructivePreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RecapGray900.copy(alpha = 0.72f))
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            RecapPopupContent(
                title = stringResource(R.string.recap_popup_preview_title),
                description = stringResource(R.string.recap_popup_preview_description),
                confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
                cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
                onConfirmClick = {},
                onCancelClick = {},
                confirmButtonColor = RecapError,
            )
        }
    }
}

@Preview(name = "RecapPopup Primary", showBackground = true, widthDp = 360, heightDp = 240)
@Composable
private fun RecapPopupPrimaryPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RecapGray900.copy(alpha = 0.72f))
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            RecapPopupContent(
                title = stringResource(R.string.recap_popup_preview_title),
                description = stringResource(R.string.recap_popup_preview_description),
                confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
                cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
                onConfirmClick = {},
                onCancelClick = {},
                confirmButtonColor = RecapBlue300,
            )
        }
    }
}

@Preview(
    name = "RecapPopup Long Button Labels",
    showBackground = true,
    widthDp = 360,
    heightDp = 240,
)
@Composable
private fun RecapPopupLongButtonLabelsPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RecapGray900.copy(alpha = 0.72f))
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            RecapPopupContent(
                title = stringResource(R.string.screenshot_edit_discard_confirm_title),
                description = stringResource(R.string.screenshot_edit_discard_confirm_description),
                confirmButtonText = stringResource(R.string.screenshot_edit_discard_confirm_quit),
                cancelButtonText = stringResource(
                    R.string.screenshot_edit_discard_confirm_keep_editing,
                ),
                onConfirmClick = {},
                onCancelClick = {},
                confirmButtonColor = RecapError,
            )
        }
    }
}
