package com.chalkak.recap.feature.demo

import android.content.res.Configuration
import android.net.Uri
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun DemoContent(
    uiState: DemoUiState = DemoUiState(),
    modifier: Modifier = Modifier,
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
                text = uiState.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = uiState.description,
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
                    Text("이미지 권한 요청")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onAction(DemoAction.RefreshImagePermission)
                    },
                ) {
                    Text("상태 새로고침")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val canRunOcr = uiState.recentScreenshotUris.isNotEmpty() && !uiState.ocrState.isRunning
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = canRunOcr,
                    onClick = {
                        onAction(DemoAction.RunOcr(OcrEngine.Latin))
                    },
                ) {
                    Text(OcrEngine.Latin.buttonLabel)
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = canRunOcr,
                    onClick = {
                        onAction(DemoAction.RunOcr(OcrEngine.Korean))
                    },
                ) {
                    Text(OcrEngine.Korean.buttonLabel)
                }
            }
            OcrResultPanel(
                ocrState = uiState.ocrState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "Demo Content - Default", showBackground = true)
@Preview(name = "Demo Content - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DemoContentPreview() {
    RECAPTheme(dynamicColor = false) {
        DemoContent(
            uiState = DemoUiState(
                title = "Demo",
                description = "이미지 권한 정책과 가져오기 플로우를 확인합니다.",
                imagePermissionLevel = ImagePermissionLevel.Selected,
                recentScreenshotUris = listOf(
                    Uri.parse("content://com.chalkak.recap.preview/screenshot/1"),
                    Uri.parse("content://com.chalkak.recap.preview/screenshot/2"),
                    Uri.parse("content://com.chalkak.recap.preview/screenshot/3"),
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
