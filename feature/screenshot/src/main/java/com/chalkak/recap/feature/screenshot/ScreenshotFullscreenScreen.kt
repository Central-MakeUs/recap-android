package com.chalkak.recap.feature.screenshot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.image.RecapPinchZoomAsyncImage
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1

@Composable
fun ScreenshotFullscreenScreen(
    imageModel: Any?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var imageLoadFailed by remember(imageModel) { mutableStateOf(false) }
    val showError = imageModel == null || imageLoadFailed
    val closeInteractionSource = remember { MutableInteractionSource() }

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
                        style = RecapBody1,
                        color = Color.White,
                        modifier = Modifier.padding(ScreenshotTokens.HorizontalPadding),
                    )
                }
            } else {
                RecapPinchZoomAsyncImage(
                    model = imageModel,
                    contentDescription = stringResource(
                        R.string.screenshot_image_placeholder_content_description,
                    ),
                    modifier = Modifier.fillMaxSize(),
                    onError = { imageLoadFailed = true },
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_close_24),
                contentDescription = stringResource(
                    R.string.screenshot_fullscreen_close_content_description,
                ),
                tint = Color.White,
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopStart)
                    .padding(horizontal = ScreenshotTokens.OverlayHorizontalPadding)
                    .size(24.dp)
                    .clickable(
                        interactionSource = closeInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onNavigateBack,
                    ),
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
