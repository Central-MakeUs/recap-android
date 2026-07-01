package com.chalkak.recap.feature.developer

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.model.OcrImageResult

@Composable
internal fun DeveloperOptionsScreen(
    onAction: (DeveloperOptionAction) -> Unit,
    modifier: Modifier = Modifier,
    ocrRawResults: List<OcrImageResult> = emptyList(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.developer_options_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            DeveloperOption.entries.forEach { option ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(option.action) },
                ) {
                    Text(stringResource(option.labelResId))
                }
            }
            OcrRawResultList(
                results = ocrRawResults,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OcrRawResultList(
    results: List<OcrImageResult>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.developer_options_ocr_raw_result_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
        )
        if (results.isEmpty()) {
            Text(
                text = stringResource(R.string.developer_options_ocr_raw_result_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            results.forEach { result ->
                OcrRawResultItem(
                    result = result,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun OcrRawResultItem(
    result: OcrImageResult,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = result.rawText.ifBlank {
                    stringResource(R.string.developer_options_ocr_raw_result_blank)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal enum class DeveloperOption(
    @get:StringRes val labelResId: Int,
    val action: DeveloperOptionAction,
) {
    TechnicalDemo(
        labelResId = R.string.developer_options_technical_demo_button,
        action = DeveloperOptionAction.OpenTechnicalDemo,
    ),
    ComponentGarden(
        labelResId = R.string.developer_options_component_garden_button,
        action = DeveloperOptionAction.OpenComponentGarden,
    ),
    ResetOnboarding(
        labelResId = R.string.developer_options_reset_onboarding_button,
        action = DeveloperOptionAction.ResetOnboarding,
    ),
}

internal sealed interface DeveloperOptionAction {
    data object OpenTechnicalDemo : DeveloperOptionAction
    data object OpenComponentGarden : DeveloperOptionAction
    data object ResetOnboarding : DeveloperOptionAction
}

@Preview(name = "Developer Options", showSystemUi = true)
@Preview(name = "Developer Options - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun DeveloperOptionsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        DeveloperOptionsScreen(
            onAction = {},
            ocrRawResults = listOf(
                OcrImageResult(
                    imageUri = "content://screenshots/1",
                    displayName = "Screenshot_20260701_120000.png",
                    rawText = "예약 확인\n서울 강남구\n오후 7:30",
                    sortIndex = 0,
                ),
                OcrImageResult(
                    imageUri = "content://screenshots/2",
                    displayName = "Screenshot_20260701_121500.png",
                    rawText = "",
                    sortIndex = 1,
                ),
            ),
        )
    }
}
