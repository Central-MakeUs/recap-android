package com.chalkak.recap.feature.demo

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun DemoScreen(
    modifier: Modifier = Modifier,
    viewModel: DemoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.onAction(DemoAction.RefreshImagePermission)
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAction(DemoAction.RefreshImagePermission)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DemoContent(
        uiState = uiState,
        modifier = modifier,
        onAction = { action ->
            when (action) {
                DemoAction.RequestImagePermission -> {
                    permissionLauncher.launch(viewModel.imagePermissionRequest())
                    viewModel.onAction(action)
                }

                DemoAction.RefreshImagePermission -> viewModel.onAction(action)
                is DemoAction.RunOcr -> viewModel.onAction(action)
                is DemoAction.SelectAnalysisInputMode -> viewModel.onAction(action)
                is DemoAction.SelectAnalysisRequestMode -> viewModel.onAction(action)
                DemoAction.RunGeminiAnalysis -> viewModel.onAction(action)
            }
        },
    )
}

@Preview(name = "Demo Screen", showSystemUi = true)
@Preview(name = "Demo Screen - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun DemoScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        DemoContent(
            uiState = DemoUiState(
                imagePermissionLevel = ImagePermissionLevel.Full,
                recentScreenshotUris = listOf(
                    "content://com.chalkak.recap.preview/screenshot/1".toUri(),
                    "content://com.chalkak.recap.preview/screenshot/2".toUri(),
                    "content://com.chalkak.recap.preview/screenshot/3".toUri(),
                    "content://com.chalkak.recap.preview/screenshot/4".toUri(),
                    "content://com.chalkak.recap.preview/screenshot/5".toUri(),
                ),
                ocrState = OcrUiState(
                    engine = OcrEngine.Latin,
                    completedCount = 5,
                    totalCount = 5,
                    results = listOf(
                        OcrImageResult(
                            imageIndex = 1,
                            imageUri = "content://com.chalkak.recap.preview/screenshot/1",
                            text = "Sample recognized text",
                            blocks = listOf(
                                OcrTextBlock(
                                    text = "Sample recognized text",
                                    lines = listOf("Sample recognized text"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
