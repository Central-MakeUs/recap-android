package com.chalkak.recap.core.design.component.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White

@Composable
fun RecapSelectField(
    value: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true,
) {
    val actionInteractionSource = remember { MutableInteractionSource() }

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
            color = White,
            border = BorderStroke(
                width = RecapFieldTokens.BorderWidth,
                color = RecapGray200,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = RecapFieldTokens.SingleLineMinHeight)
                    .padding(RecapFieldTokens.ContentPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecapGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = actionLabel,
                    modifier = Modifier.clickable(
                        enabled = enabled,
                        interactionSource = actionInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onActionClick,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        RecapBlue500
                    } else {
                        RecapGray900.copy(alpha = 0.38f)
                    },
                )
            }
        }
    }
}

@Preview(name = "RecapSelectField", showBackground = true, widthDp = 360)
@Composable
private fun RecapSelectFieldPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapSelectField(
            value = stringResource(R.string.recap_select_field_preview_value),
            actionLabel = stringResource(R.string.recap_select_field_preview_action),
            onActionClick = {},
            label = stringResource(R.string.recap_select_field_preview_label),
            modifier = Modifier.padding(16.dp),
        )
    }
}
