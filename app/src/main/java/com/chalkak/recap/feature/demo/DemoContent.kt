package com.chalkak.recap.feature.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.chalkak.recap.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun DemoContent(
    modifier: Modifier = Modifier,
    uiState: DemoUiState = DemoUiState(),
    onAction: (DemoAction) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(uiState.titleResId),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(uiState.descriptionResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ImagePermissionStatus(
                permissionLevel = uiState.imagePermissionLevel,
                modifier = Modifier.fillMaxWidth(),
            )
            RecentScreenshotsRow(
                imageUris = uiState.recentScreenshotUris,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onAction(DemoAction.RequestImagePermission)
                    },
                ) {
                    Text(stringResource(R.string.demo_request_image_permission_button))
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onAction(DemoAction.RefreshImagePermission)
                    },
                ) {
                    Text(stringResource(R.string.demo_refresh_status_button))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val canRunOcr =
                    uiState.recentScreenshotUris.isNotEmpty() && !uiState.ocrState.isRunning
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canRunOcr,
                    onClick = {
                        onAction(DemoAction.RunOcr(OcrEngine.Latin))
                    },
                ) {
                    Text(stringResource(OcrEngine.Latin.buttonLabelResId))
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = canRunOcr,
                    onClick = {
                        onAction(DemoAction.RunOcr(OcrEngine.Korean))
                    },
                ) {
                    Text(stringResource(OcrEngine.Korean.buttonLabelResId))
                }
            }
            OcrResultPanel(
                ocrState = uiState.ocrState,
                modifier = Modifier.fillMaxWidth(),
            )
            RecapAnalysisPanel(
                selectedInputMode = uiState.selectedAnalysisInputMode,
                selectedRequestMode = uiState.selectedAnalysisRequestMode,
                analysisState = uiState.analysisState,
                analysisHistory = uiState.analysisHistory,
                canRunAnalysis = uiState.recentScreenshotUris.isNotEmpty() &&
                        !uiState.ocrState.isRunning,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Demo Content - Default", showBackground = true)
@Composable
private fun DemoContentPreview() {
    RECAPTheme(dynamicColor = false) {
        DemoContent(
            uiState = DemoUiState(
                imagePermissionLevel = ImagePermissionLevel.Selected,
                recentScreenshotUris = listOf(
                    "content://com.chalkak.recap.preview/screenshot/1".toUri(),
                    "content://com.chalkak.recap.preview/screenshot/2".toUri(),
                    "content://com.chalkak.recap.preview/screenshot/3".toUri(),
                ),
                ocrState = OcrUiState(
                    engine = OcrEngine.Korean,
                    completedCount = 3,
                    totalCount = 5,
                    results = listOf(
                        OcrImageResult(
                            imageIndex = 1,
                            imageUri = "content://com.chalkak.recap.preview/screenshot/1",
                            text = "오늘 회의 메모",
                            blocks = listOf(
                                OcrTextBlock(
                                    text = "오늘 회의 메모",
                                    lines = listOf("오늘 회의 메모"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
