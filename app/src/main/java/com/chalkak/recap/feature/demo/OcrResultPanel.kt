package com.chalkak.recap.feature.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun OcrResultPanel(
    ocrState: OcrUiState,
    modifier: Modifier = Modifier,
) {
    val engineLabel = ocrState.engine?.let { stringResource(it.resultLabelResId) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.demo_ocr_result_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = engineLabel ?: stringResource(R.string.demo_ocr_not_run),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (ocrState.totalCount > 0) {
                LinearProgressIndicator(
                    progress = { ocrState.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(
                        R.string.demo_ocr_progress_count,
                        ocrState.completedCount,
                        ocrState.totalCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ocrState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (ocrState.results.isEmpty()) {
                Text(
                    text = stringResource(R.string.demo_ocr_not_run),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ocrState.results.forEach { result ->
                        OcrRawTextField(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrRawTextField(
    result: OcrImageResult,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.demo_ocr_screenshot_label, result.imageIndex),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            SelectionContainer {
                Text(
                    text = result.text.ifBlank {
                        stringResource(R.string.developer_options_ocr_raw_result_blank)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp, max = 128.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview(name = "OCR Result")
@Composable
private fun OcrResultPanelPreview() {
    RECAPTheme(dynamicColor = false) {
        OcrResultPanel(
            ocrState = OcrUiState(
                engine = OcrEngine.Korean,
                completedCount = 2,
                totalCount = 5,
                results = listOf(
                    OcrImageResult(
                        imageIndex = 1,
                        imageUri = "content://com.chalkak.recap.preview/screenshot/1",
                        text = "샘플 텍스트",
                        blocks = listOf(
                            OcrTextBlock(
                                text = "샘플 텍스트",
                                lines = listOf("샘플", "텍스트"),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
