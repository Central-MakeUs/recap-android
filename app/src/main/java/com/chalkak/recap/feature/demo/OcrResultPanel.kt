package com.chalkak.recap.feature.demo

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun OcrResultPanel(
    ocrState: OcrUiState,
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "OCR Raw Result",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = ocrState.engine?.resultLabel ?: "아직 실행하지 않음",
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
                    text = "${ocrState.completedCount}/${ocrState.totalCount}",
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

            Text(
                text = ocrState.toRawJsonLikeText(),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun OcrUiState.toRawJsonLikeText(): String {
    return buildString {
        appendLine("{")
        appendLine("  \"engine\": ${engine?.resultLabel.quoteOrNull()},")
        appendLine("  \"isRunning\": $isRunning,")
        appendLine("  \"completedCount\": $completedCount,")
        appendLine("  \"totalCount\": $totalCount,")
        appendLine("  \"errorMessage\": ${errorMessage.quoteOrNull()},")
        appendLine("  \"results\": [")
        results.forEachIndexed { resultIndex, result ->
            appendLine("    {")
            appendLine("      \"imageIndex\": ${result.imageIndex},")
            appendLine("      \"imageUri\": ${result.imageUri.quote()},")
            appendLine("      \"text\": ${result.text.quote()},")
            appendLine("      \"blocks\": [")
            result.blocks.forEachIndexed { blockIndex, block ->
                appendLine("        {")
                appendLine("          \"text\": ${block.text.quote()},")
                appendLine("          \"lines\": [")
                block.lines.forEachIndexed { lineIndex, line ->
                    append("            ${line.quote()}")
                    appendLine(if (lineIndex == block.lines.lastIndex) "" else ",")
                }
                appendLine("          ]")
                append("        }")
                appendLine(if (blockIndex == result.blocks.lastIndex) "" else ",")
            }
            appendLine("      ]")
            append("    }")
            appendLine(if (resultIndex == results.lastIndex) "" else ",")
        }
        appendLine("  ]")
        append("}")
    }
}

private fun String?.quoteOrNull(): String {
    return this?.quote() ?: "null"
}

private fun String.quote(): String {
    return buildString {
        append('"')
        this@quote.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
}

@Preview(name = "OCR Result")
@Preview(name = "OCR Result - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
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
