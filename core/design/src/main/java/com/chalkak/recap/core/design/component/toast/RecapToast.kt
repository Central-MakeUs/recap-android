package com.chalkak.recap.core.design.component.toast

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapSuccess
import com.chalkak.recap.core.design.theme.RecapToastBackground
import com.chalkak.recap.core.design.theme.RecapToastContent
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

enum class RecapToastType(
    val iconTint: Color,
    @get:DrawableRes val iconResId: Int,
    @get:StringRes val iconContentDescriptionResId: Int,
) {
    Success(
        iconTint = RecapSuccess,
        iconResId = R.drawable.ic_check_circle_24,
        iconContentDescriptionResId = R.string.recap_toast_success_icon_content_description,
    ),
    Error(
        iconTint = RecapError,
        iconResId = R.drawable.ic_error_circle_24,
        iconContentDescriptionResId = R.string.recap_toast_error_icon_content_description,
    ),
}

enum class RecapToastDuration(val millis: Long) {
    Short(2_000L),
    Long(3_500L),
}

@Immutable
data class RecapToastColors(
    val container: Color,
    val content: Color,
)

object RecapToastDefaults {
    val HorizontalPadding = 21.dp
    val VerticalPadding = 10.5.dp
    val IconSize = 24.dp
    val IconSpacing = 10.dp
    val Shape = RoundedCornerShape(percent = 50)
    val BlurRadius: Dp = 24.dp
    const val NoiseFactor: Float = 0.0f

    fun colors(): RecapToastColors = RecapToastColors(
        container = RecapToastBackground,
        content = RecapToastContent,
    )
}

@Composable
fun RecapToastHost(
    currentToast: RecapToastPresentation?,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    var visibleToast by remember { mutableStateOf<RecapToastPresentation?>(null) }
    if (currentToast != null) {
        visibleToast = currentToast
    }
    AnimatedVisibility(
        visible = currentToast != null,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(RecapToastExitAnimationDurationMillis)) +
            slideInVertically(
                animationSpec = tween(RecapToastExitAnimationDurationMillis),
                initialOffsetY = { fullHeight -> fullHeight / 2 },
            ),
        exit = fadeOut(animationSpec = tween(RecapToastExitAnimationDurationMillis)) +
            slideOutVertically(
                animationSpec = tween(RecapToastExitAnimationDurationMillis),
                targetOffsetY = { fullHeight -> fullHeight / 2 },
            ),
    ) {
        visibleToast?.let { toast ->
            RecapToast(
                message = toast.message,
                type = toast.type,
                hazeState = hazeState,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }
    }
}

@Composable
fun RecapToast(
    message: String,
    type: RecapToastType,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    colors: RecapToastColors = RecapToastDefaults.colors(),
) {
    val blurStyle = CupertinoMaterials.ultraThin()
    // Haze is a no-op in Compose Preview; fall back to the tint color so the toast is visible.
    val containerColor =
        if (LocalInspectionMode.current) colors.container else Color.Transparent
    Surface(
        modifier = modifier
            .clip(RecapToastDefaults.Shape)
            .hazeEffect(state = hazeState) {
                inputScale = HazeInputScale.Fixed(0.5f)
                blurEffect {
                    blurEnabled = true
                    blurRadius = RecapToastDefaults.BlurRadius
                    style = blurStyle
                    colorEffects = listOf(
                        HazeColorEffect.tint(colors.container),
                    )
                    noiseFactor = RecapToastDefaults.NoiseFactor
                }
            },
        shape = RecapToastDefaults.Shape,
        color = containerColor,
        contentColor = colors.content,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RecapToastDefaults.HorizontalPadding,
                vertical = RecapToastDefaults.VerticalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(RecapToastDefaults.IconSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecapToastIcon(type = type)
            Text(
                text = message,
                style = RecapCaption1,
                color = colors.content,
            )
        }
    }
}

@Composable
private fun RecapToastIcon(
    type: RecapToastType,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(type.iconResId),
        contentDescription = stringResource(type.iconContentDescriptionResId),
        modifier = modifier.size(RecapToastDefaults.IconSize),
        tint = type.iconTint,
    )
}

@Preview(name = "Recap Toast Success", showBackground = true, backgroundColor = 0xFF4D586C)
@Composable
private fun RecapToastSuccessPreview() {
    RECAPTheme(dynamicColor = false) {
        val hazeState = rememberHazeState()
        RecapToastGlassPreviewBackground(hazeState = hazeState) {
            RecapToast(
                message = stringResource(R.string.recap_toast_preview_login_failed_message),
                type = RecapToastType.Success,
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
        }
    }
}

@Preview(name = "Recap Toast Error", showBackground = true, backgroundColor = 0xFF4D586C)
@Composable
private fun RecapToastErrorPreview() {
    RECAPTheme(dynamicColor = false) {
        val hazeState = rememberHazeState()
        RecapToastGlassPreviewBackground(hazeState = hazeState) {
            RecapToast(
                message = stringResource(R.string.recap_toast_preview_login_failed_message),
                type = RecapToastType.Error,
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
            )
        }
    }
}

@Preview(name = "Recap Toast Variants", showBackground = true, backgroundColor = 0xFF4D586C)
@Composable
private fun RecapToastVariantsPreview() {
    RECAPTheme(dynamicColor = false) {
        val hazeState = rememberHazeState()
        RecapToastGlassPreviewBackground(hazeState = hazeState) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RecapToast(
                    message = stringResource(R.string.recap_toast_preview_login_failed_message),
                    type = RecapToastType.Success,
                    hazeState = hazeState,
                )
                RecapToast(
                    message = stringResource(R.string.recap_toast_preview_login_failed_message),
                    type = RecapToastType.Error,
                    hazeState = hazeState,
                )
            }
        }
    }
}

@Composable
private fun RecapToastGlassPreviewBackground(
    hazeState: HazeState,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .hazeSource(state = hazeState)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(RecapBlue50, RecapGray100),
                ),
            ),
        content = content,
    )
}
