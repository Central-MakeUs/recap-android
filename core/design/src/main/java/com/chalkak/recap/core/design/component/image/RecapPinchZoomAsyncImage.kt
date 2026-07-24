package com.chalkak.recap.core.design.component.image

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme

/**
 * 스크린샷 확대 뷰 공통 정책:
 * - 초기 표시는 가용 영역 대비 상하좌우 최소 [EdgeInsetFraction] 여백을 둔 Fit
 * - 두 손가락 핀치/팬으로 [MinScale]~[MaxScale] 확대
 */
@Composable
fun RecapPinchZoomAsyncImage(
    model: Any,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentPaddingTop: Dp = 0.dp,
    onTap: (() -> Unit)? = null,
    onError: (() -> Unit)? = null,
) {
    var scale by remember(model) { mutableFloatStateOf(RecapPinchZoomImageTokens.MinScale) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(top = contentPaddingTop),
    ) {
        val horizontalInset = maxWidth * RecapPinchZoomImageTokens.EdgeInsetFraction
        val verticalInset = maxHeight * RecapPinchZoomImageTokens.EdgeInsetFraction

        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            onError = if (onError != null) {
                { onError() }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalInset, vertical = verticalInset)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .then(
                    if (onTap != null) {
                        Modifier.pointerInput(onTap) {
                            detectTapGestures(onTap = { onTap() })
                        }
                    } else {
                        Modifier
                    },
                )
                .pointerInput(model) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(
                            RecapPinchZoomImageTokens.MinScale,
                            RecapPinchZoomImageTokens.MaxScale,
                        )
                        scale = newScale
                        if (newScale > RecapPinchZoomImageTokens.MinScale) {
                            offset += pan
                        } else {
                            offset = Offset.Zero
                        }
                    }
                },
        )
    }
}

object RecapPinchZoomImageTokens {
    const val EdgeInsetFraction = 0.1f
    const val MinScale = 1f
    const val MaxScale = 4f
}

@Preview(name = "Recap Pinch Zoom Image", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RecapPinchZoomAsyncImagePreview() {
    RECAPTheme(dynamicColor = false) {
        RecapPinchZoomAsyncImage(
            model = R.drawable.mock_home_screenshot_hotel,
            contentDescription = "Preview",
            modifier = Modifier.fillMaxSize(),
        )
    }
}
