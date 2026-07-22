package com.chalkak.recap.feature.organize

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun ScreenshotPickerZoomOverlay(
    imageModel: Any,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        HideStatusBarEffect()
        ScreenshotPickerZoomOverlayContent(
            imageModel = imageModel,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
internal fun ScreenshotPickerZoomOverlayContent(
    imageModel: Any,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val view = LocalView.current
    val density = LocalDensity.current
    val statusBarTopPadding = remember(view, density) {
        val topPx = ViewCompat.getRootWindowInsets(view)
            ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())
            ?.top
            ?: 0
        with(density) { topPx.toDp() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RecapGray900.copy(alpha = ScreenshotPickerZoomTokens.ScrimAlpha))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismissRequest() })
            },
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarTopPadding),
        ) {
            val horizontalInset = maxWidth * ScreenshotPickerZoomTokens.EdgeInsetFraction
            val verticalInset = maxHeight * ScreenshotPickerZoomTokens.EdgeInsetFraction

            AsyncImage(
                model = imageModel,
                contentDescription = stringResource(
                    R.string.organize_screenshot_item_content_description,
                    1,
                ),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalInset, vertical = verticalInset)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismissRequest() })
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(
                                ScreenshotPickerZoomTokens.MinScale,
                                ScreenshotPickerZoomTokens.MaxScale,
                            )
                            scale = newScale
                            if (newScale > ScreenshotPickerZoomTokens.MinScale) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
            )
        }
    }
}

@Composable
private fun HideStatusBarEffect() {
    val view = LocalView.current
    if (view.isInEditMode) {
        return
    }
    DisposableEffect(view) {
        val activity = view.context.findActivity()
        val windows = buildList {
            (view.parent as? DialogWindowProvider)?.window?.let(::add)
            activity?.window?.let(::add)
        }.distinct()
        if (windows.isEmpty()) {
            return@DisposableEffect onDispose {}
        }

        // hide(statusBars) zeroes Activity WindowInsets.statusBars; pin content top
        // padding first so Home/TopBar does not jump into the status-bar region.
        val activityContent = activity?.findViewById<View>(android.R.id.content)
        val statusBarHeightPx = (
            activityContent?.let(ViewCompat::getRootWindowInsets)
                ?: ViewCompat.getRootWindowInsets(view)
            )
            ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())
            ?.top
            ?: 0
        val previousPaddingTop = activityContent?.paddingTop ?: 0
        if (activityContent != null && statusBarHeightPx > 0) {
            activityContent.setPadding(
                activityContent.paddingLeft,
                previousPaddingTop + statusBarHeightPx,
                activityContent.paddingRight,
                activityContent.paddingBottom,
            )
        }
        val compensatedContent =
            activityContent.takeIf { it != null && statusBarHeightPx > 0 }

        data class StatusBarState(
            val controller: WindowInsetsControllerCompat,
            val previousSystemBarsBehavior: Int,
        )
        val previousStates = windows.map { window ->
            val controller = WindowCompat.getInsetsController(window, view)
            StatusBarState(
                controller = controller,
                previousSystemBarsBehavior = controller.systemBarsBehavior,
            )
        }
        previousStates.forEach { state ->
            state.controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            state.controller.hide(WindowInsetsCompat.Type.statusBars())
        }
        onDispose {
            previousStates.forEach { state ->
                state.controller.show(WindowInsetsCompat.Type.statusBars())
                state.controller.systemBarsBehavior = state.previousSystemBarsBehavior
            }
            compensatedContent?.post {
                compensatedContent.setPadding(
                    compensatedContent.paddingLeft,
                    previousPaddingTop,
                    compensatedContent.paddingRight,
                    compensatedContent.paddingBottom,
                )
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context: Context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

private object ScreenshotPickerZoomTokens {
    const val ScrimAlpha = 0.72f
    const val EdgeInsetFraction = 0.1f
    const val MinScale = 1f
    const val MaxScale = 4f
}

@Preview(
    name = "Screenshot Picker Zoom Overlay",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerZoomOverlayPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerZoomOverlayContent(
            imageModel = R.drawable.mock_home_screenshot_hotel,
            onDismissRequest = {},
        )
    }
}
