package com.chalkak.recap.feature.screenshot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.input.RecapInputField
import com.chalkak.recap.core.design.component.input.RecapSelectField
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun ScreenshotEditScreen(
    content: ScreenshotUiState.Content,
    onAction: (ScreenshotAction) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    onChangeType: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = content.editDraft
    val canDone = draft.isTitleValid() && !content.isSaving
    val fieldsEnabled = !content.isSaving
    val imageModel = resolveScreenshotImageModel(
        storedImagePath = content.card.imageRefs.storedImagePath,
        sourceImageUri = content.card.imageRefs.sourceImageUri,
        thumbnailPath = content.card.imageRefs.thumbnailPath,
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding(),
        ) {
            ScreenshotEditTopBar(
                canDone = canDone,
                cancelEnabled = !content.isSaving,
                onCancel = onCancel,
                onDone = onDone,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = ScreenshotTokens.HorizontalPadding,
                        vertical = ScreenshotTokens.ContentTopPadding,
                    ),
            ) {
                Text(
                    text = stringResource(
                        R.string.screenshot_organized_date_format,
                        formatOrganizedDate(content.card.createdAtMillis),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = RecapGray500,
                )
                Spacer(modifier = Modifier.height(7.dp))
                ScreenshotEditImagePreview(imageModel = imageModel)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.screenshot_edit_image_immutable_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = RecapBlue500,
                )
                Spacer(modifier = Modifier.height(21.dp))
                RecapSelectField(
                    value = stringResource(draft.contentType.toLabelResId()),
                    actionLabel = stringResource(R.string.screenshot_edit_type_change),
                    onActionClick = onChangeType,
                    label = stringResource(R.string.screenshot_edit_type_label),
                    enabled = fieldsEnabled,
                )
                Spacer(modifier = Modifier.height(30.dp))
                RecapInputField(
                    value = draft.title,
                    onValueChange = { onAction(ScreenshotAction.UpdateEditTitle(it)) },
                    label = stringResource(R.string.screenshot_edit_title_label),
                    enabled = fieldsEnabled,
                    maxLength = ScreenshotLimits.TitleMaxLength,
                    isError = content.titleError,
                    errorMessage = if (content.titleError) {
                        stringResource(R.string.screenshot_edit_title_error)
                    } else {
                        null
                    },
                )
                RecapInputField(
                    value = draft.summary,
                    onValueChange = { onAction(ScreenshotAction.UpdateEditSummary(it)) },
                    label = stringResource(R.string.screenshot_edit_summary_label),
                    enabled = fieldsEnabled,
                    maxLength = ScreenshotLimits.SummaryMaxLength,
                )
                Spacer(modifier = Modifier.height(21.dp))
                RecapInputField(
                    value = draft.body,
                    onValueChange = { onAction(ScreenshotAction.UpdateEditBody(it)) },
                    label = stringResource(R.string.screenshot_edit_body_label),
                    enabled = fieldsEnabled,
                    singleLine = false,
                    minLines = 4,
                )
                content.actionErrorMessageResId?.let { errorResId ->
                    Text(
                        text = stringResource(errorResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RecapError,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotEditTopBar(
    canDone: Boolean,
    cancelEnabled: Boolean,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScreenshotTokens.EditTopBarHeight)
            .padding(horizontal = ScreenshotTokens.HorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.screenshot_edit_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = RecapGray900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScreenshotTextAction(
                text = stringResource(R.string.screenshot_edit_cancel),
                enabled = cancelEnabled,
                onClick = onCancel,
                color = RecapGray900,
                fontWeight = FontWeight.Medium,
            )
            ScreenshotTextAction(
                text = stringResource(R.string.screenshot_edit_done),
                enabled = canDone,
                onClick = onDone,
                color = RecapBlue500,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ScreenshotTextAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    color: Color,
    fontWeight: FontWeight,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .defaultMinSize(
                minWidth = ScreenshotTokens.TextActionMinSize,
                minHeight = ScreenshotTokens.TextActionMinSize,
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                color
            } else {
                RecapGray900.copy(alpha = 0.38f)
            },
            fontWeight = fontWeight,
        )
    }
}

@Composable
private fun ScreenshotEditImagePreview(
    imageModel: Any?,
) {
    var imageLoadFailed by remember(imageModel) { mutableStateOf(false) }
    val showPlaceholder = imageModel == null || imageLoadFailed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScreenshotTokens.EditImagePreviewHeight)
            .clip(RoundedCornerShape(ScreenshotTokens.EditImagePreviewCornerRadius)),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = RecapGray100,
        ) {
            if (showPlaceholder) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.screenshot_image_load_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = RecapGray500,
                        modifier = Modifier.padding(ScreenshotTokens.HorizontalPadding),
                    )
                }
            } else {
                AsyncImage(
                    model = imageModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { imageLoadFailed = true },
                )
            }
        }
        ScreenshotIconButton(
            iconResId = R.drawable.ic_fullscreen_24,
            contentDescription = stringResource(
                R.string.screenshot_detail_fullscreen_content_description,
            ),
            onClick = {},
            tint = RecapGray900,
            outlined = true,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(ScreenshotTokens.FullscreenButtonPadding),
        )
    }
}

@Preview(name = "Screenshot Edit", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ScreenshotEditScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotEditScreen(
            content = previewScreenshotContent(),
            onAction = {},
            onCancel = {},
            onDone = {},
            onChangeType = {},
        )
    }
}
