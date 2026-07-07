package com.chalkak.recap.feature.demo

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.model.Confidence
import com.chalkak.recap.core.model.ConfidenceLevel
import com.chalkak.recap.core.model.KeyField
import com.chalkak.recap.core.model.RecapAnalysisBatchResult
import com.chalkak.recap.core.model.RecapAnalysisInputMode
import com.chalkak.recap.core.model.RecapAnalysisRequestMode
import com.chalkak.recap.core.model.RecapAnalysisResult

@Composable
fun RecapAnalysisPanel(
    selectedInputMode: RecapAnalysisInputMode,
    selectedRequestMode: RecapAnalysisRequestMode,
    analysisState: RecapAnalysisUiState,
    analysisHistory: List<RecapAnalysisRunSummary>,
    canRunAnalysis: Boolean,
    onAction: (DemoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.demo_gemini_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            AnalysisRadioGroup(
                titleResId = R.string.demo_gemini_input_mode_title,
                options = RecapAnalysisInputMode.entries,
                selectedOption = selectedInputMode,
                label = { stringResource(it.labelResId()) },
                onSelected = { inputMode ->
                    onAction(DemoAction.SelectAnalysisInputMode(inputMode))
                },
            )

            AnalysisRadioGroup(
                titleResId = R.string.demo_gemini_request_mode_title,
                options = RecapAnalysisRequestMode.entries,
                selectedOption = selectedRequestMode,
                label = { stringResource(it.labelResId()) },
                onSelected = { requestMode ->
                    onAction(DemoAction.SelectAnalysisRequestMode(requestMode))
                },
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canRunAnalysis && !analysisState.isRunning,
                onClick = { onAction(DemoAction.RunGeminiAnalysis) },
            ) {
                Text(stringResource(R.string.demo_gemini_run_button))
            }

            if (analysisState.isRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = stringResource(R.string.demo_gemini_running),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            analysisState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            analysisState.result?.let { result ->
                AnalysisResultList(result = result)
            }

            AnalysisHistoryList(history = analysisHistory)
        }
    }
}

@Composable
private fun <T> AnalysisRadioGroup(
    @StringRes titleResId: Int,
    options: List<T>,
    selectedOption: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(option) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = option == selectedOption,
                    onClick = { onSelected(option) },
                )
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun AnalysisResultList(
    result: RecapAnalysisBatchResult,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                R.string.demo_gemini_result_summary,
                result.inputMode.name,
                result.requestMode.name,
                result.batchSize,
                result.results.size,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        result.results.forEach { item ->
            AnalysisResultItem(result = item)
        }
    }
}

@Composable
private fun AnalysisResultItem(
    result: RecapAnalysisResult,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = result.imageId,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = result.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            SelectionContainer {
                Text(
                    text = result.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (result.needsReview || result.suggestedViews.contains("UNDEFINED")) {
                Text(
                    text = stringResource(R.string.demo_gemini_review_required),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            KeyFieldList(keyFields = result.keyFields)
            LabelList(
                titleResId = R.string.demo_gemini_content_types,
                labels = result.contentTypes,
            )
            LabelList(
                titleResId = R.string.demo_gemini_utility_tags,
                labels = result.utilityTags,
            )
            LabelList(
                titleResId = R.string.demo_gemini_suggested_views,
                labels = result.suggestedViews,
            )
            LabelList(
                titleResId = R.string.demo_gemini_review_reasons,
                labels = result.reviewReasons,
            )
            ConfidenceSummary(confidence = result.confidence)
        }
    }
}

@Composable
private fun AnalysisHistoryList(
    history: List<RecapAnalysisRunSummary>,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.demo_gemini_history_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        history.forEach { item ->
            AnalysisHistoryItem(summary = item)
        }
    }
}

@Composable
private fun AnalysisHistoryItem(
    summary: RecapAnalysisRunSummary,
    modifier: Modifier = Modifier,
) {
    val statusText = summary.errorMessage ?: stringResource(
        R.string.demo_gemini_history_success,
        summary.resultCount,
        summary.reviewRequiredCount,
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.demo_gemini_history_header,
                    summary.runIndex,
                    summary.inputMode.name,
                    summary.requestMode.name,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    R.string.demo_gemini_history_meta,
                    summary.screenshotCount,
                    summary.durationMillis,
                    statusText,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (summary.errorMessage == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

@Composable
private fun KeyFieldList(
    keyFields: List<KeyField>,
    modifier: Modifier = Modifier,
) {
    if (keyFields.isEmpty()) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.demo_gemini_key_fields),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        keyFields.forEach { field ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.weight(0.34f),
                    text = field.label.ifBlank { field.key },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SelectionContainer(
                    modifier = Modifier.weight(0.66f),
                ) {
                    Text(
                        text = field.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelList(
    @StringRes titleResId: Int,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty()) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            labels.forEach { label ->
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfidenceSummary(
    confidence: Confidence,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HorizontalDivider(
            thickness = DividerDefaults.Thickness,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = stringResource(
                R.string.demo_gemini_confidence_summary,
                confidence.overall.koreanLabel(),
                confidence.title.koreanLabel(),
                confidence.summary.koreanLabel(),
                confidence.contentType.koreanLabel(),
                confidence.keyFields.koreanLabel(),
                confidence.suggestedViews.koreanLabel(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@StringRes
private fun RecapAnalysisInputMode.labelResId(): Int {
    return when (this) {
        RecapAnalysisInputMode.IMAGE_ONLY -> R.string.demo_gemini_input_mode_image_only
        RecapAnalysisInputMode.OCR_TEXT_ONLY -> R.string.demo_gemini_input_mode_ocr_text_only
        RecapAnalysisInputMode.IMAGE_WITH_OCR_TEXT -> R.string.demo_gemini_input_mode_image_with_ocr_text
    }
}

@StringRes
private fun RecapAnalysisRequestMode.labelResId(): Int {
    return when (this) {
        RecapAnalysisRequestMode.SINGLE_PER_REQUEST -> R.string.demo_gemini_request_mode_single
        RecapAnalysisRequestMode.BATCH_PER_REQUEST -> R.string.demo_gemini_request_mode_batch
    }
}

private fun ConfidenceLevel.koreanLabel(): String {
    return when (this) {
        ConfidenceLevel.HIGH -> "상"
        ConfidenceLevel.MEDIUM -> "중"
        ConfidenceLevel.LOW -> "하"
    }
}

@Preview(name = "Recap Analysis Panel")
@Composable
private fun RecapAnalysisPanelPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapAnalysisPanel(
            selectedInputMode = RecapAnalysisInputMode.IMAGE_WITH_OCR_TEXT,
            selectedRequestMode = RecapAnalysisRequestMode.BATCH_PER_REQUEST,
            canRunAnalysis = true,
            analysisState = RecapAnalysisUiState(
                result = RecapAnalysisBatchResult(
                    analysisVersion = "v0.2",
                    inputMode = RecapAnalysisInputMode.IMAGE_WITH_OCR_TEXT,
                    requestMode = RecapAnalysisRequestMode.BATCH_PER_REQUEST,
                    batchSize = 5,
                    results = listOf(
                        RecapAnalysisResult(
                            imageId = "screenshot_1",
                            title = "회의 일정 캡처",
                            summary = "7월 3일 오후 회의 일정과 장소 정보가 포함된 화면입니다.",
                            contentTypes = listOf("schedule"),
                            keyFields = listOf(
                                KeyField(
                                    key = "datetime",
                                    label = "일시",
                                    value = "2026-07-03 14:00",
                                    valueType = "datetime",
                                    displayPriority = 1,
                                ),
                            ),
                            utilityTags = listOf("has_schedule", "actionable"),
                            suggestedViews = listOf("ACTION"),
                            confidence = Confidence(
                                overall = ConfidenceLevel.HIGH,
                                title = ConfidenceLevel.HIGH,
                                summary = ConfidenceLevel.HIGH,
                                contentType = ConfidenceLevel.MEDIUM,
                                keyFields = ConfidenceLevel.MEDIUM,
                                suggestedViews = ConfidenceLevel.HIGH,
                            ),
                            needsReview = false,
                            reviewReasons = emptyList(),
                            keywords = listOf("회의", "일정"),
                        ),
                    ),
                ),
            ),
            analysisHistory = listOf(
                RecapAnalysisRunSummary(
                    runIndex = 1,
                    inputMode = RecapAnalysisInputMode.IMAGE_WITH_OCR_TEXT,
                    requestMode = RecapAnalysisRequestMode.BATCH_PER_REQUEST,
                    screenshotCount = 5,
                    durationMillis = 3280,
                    resultCount = 5,
                    reviewRequiredCount = 1,
                ),
            ),
            onAction = {},
        )
    }
}
