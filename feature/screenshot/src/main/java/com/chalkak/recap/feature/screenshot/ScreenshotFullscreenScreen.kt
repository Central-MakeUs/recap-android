package com.chalkak.recap.feature.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun ScreenshotFullscreenScreen(
    imageModel: Any?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var imageLoadFailed by remember(imageModel) { mutableStateOf(false) }
    val showError = imageModel == null || imageLoadFailed

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.screenshot_image_load_error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(ScreenshotTokens.HorizontalPadding),
                    )
                }
            } else {
                AsyncImage(
                    model = imageModel,
                    contentDescription = stringResource(
                        R.string.screenshot_image_placeholder_content_description,
                    ),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                    onError = { imageLoadFailed = true },
                )
            }

            ScreenshotIconButton(
                iconResId = R.drawable.ic_close_24,
                contentDescription = stringResource(
                    R.string.screenshot_fullscreen_close_content_description,
                ),
                onClick = onNavigateBack,
                tint = Color.White,
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopStart)
                    .padding(horizontal = ScreenshotTokens.OverlayHorizontalPadding),
            )
        }
    }
}

@Preview(name = "Screenshot Fullscreen", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScreenshotFullscreenScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotFullscreenScreen(
            imageModel = null,
            onNavigateBack = {},
        )
    }
}
