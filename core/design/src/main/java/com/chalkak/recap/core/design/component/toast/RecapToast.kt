package com.chalkak.recap.core.design.component.toast

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapToastBackground
import com.chalkak.recap.core.design.theme.RecapToastContent
import com.chalkak.recap.core.design.theme.RecapToastErrorIconContainer
import com.chalkak.recap.core.design.theme.RecapToastSuccessIconContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

enum class RecapToastType(
    val iconTint: Color,
    @DrawableRes val iconResId: Int,
    @StringRes val iconContentDescriptionResId: Int,
) {
    Success(
        iconTint = RecapToastSuccessIconContainer,
        iconResId = R.drawable.ic_check_circle_24,
        iconContentDescriptionResId = R.string.recap_toast_success_icon_content_description,
    ),
    Error(
        iconTint = RecapToastErrorIconContainer,
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

@Immutable
internal data class RecapToastData(
    val message: String,
    val type: RecapToastType,
)

object RecapToastDefaults {
    val HorizontalPadding = 16.dp
    val VerticalPadding = 12.dp
    val IconSize = 24.dp
    val IconSpacing = 10.dp
    val Shape = RoundedCornerShape(percent = 50)

    fun colors(): RecapToastColors = RecapToastColors(
        container = RecapToastBackground,
        content = RecapToastContent,
    )
}

@Stable
class RecapToastHostState {
    private val mutex = Mutex()

    internal var currentToastData by mutableStateOf<RecapToastData?>(null)
        private set

    suspend fun showToast(
        message: String,
        type: RecapToastType = RecapToastType.Error,
        duration: RecapToastDuration = RecapToastDuration.Short,
    ) {
        mutex.withLock {
            try {
                currentToastData = RecapToastData(
                    message = message,
                    type = type,
                )
                delay(duration.millis.milliseconds)
            } finally {
                currentToastData = null
            }
        }
    }
}

@Composable
fun rememberRecapToastHostState(): RecapToastHostState = remember { RecapToastHostState() }

@Composable
fun RecapToastHost(
    hostState: RecapToastHostState,
    modifier: Modifier = Modifier,
) {
    val toastData = hostState.currentToastData
    AnimatedVisibility(
        visible = toastData != null,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(RecapToastAnimationDurationMillis)) +
            slideInVertically(
                animationSpec = tween(RecapToastAnimationDurationMillis),
                initialOffsetY = { fullHeight -> fullHeight / 2 },
            ),
        exit = fadeOut(animationSpec = tween(RecapToastAnimationDurationMillis)) +
            slideOutVertically(
                animationSpec = tween(RecapToastAnimationDurationMillis),
                targetOffsetY = { fullHeight -> fullHeight / 2 },
            ),
    ) {
        toastData?.let { data ->
            RecapToast(
                message = data.message,
                type = data.type,
            )
        }
    }
}

@Composable
fun RecapToast(
    message: String,
    type: RecapToastType,
    modifier: Modifier = Modifier,
    colors: RecapToastColors = RecapToastDefaults.colors(),
) {
    Surface(
        modifier = modifier,
        shape = RecapToastDefaults.Shape,
        color = colors.container,
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
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
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
        RecapToast(
            message = stringResource(R.string.recap_toast_preview_login_failed_message),
            type = RecapToastType.Success,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Recap Toast Error", showBackground = true, backgroundColor = 0xFF4D586C)
@Composable
private fun RecapToastErrorPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapToast(
            message = stringResource(R.string.recap_toast_preview_login_failed_message),
            type = RecapToastType.Error,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Recap Toast Variants", showBackground = true, backgroundColor = 0xFF4D586C)
@Composable
private fun RecapToastVariantsPreview() {
    RECAPTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecapToast(
                message = stringResource(R.string.recap_toast_preview_login_failed_message),
                type = RecapToastType.Success,
            )
            RecapToast(
                message = stringResource(R.string.recap_toast_preview_login_failed_message),
                type = RecapToastType.Error,
            )
        }
    }
}

private const val RecapToastAnimationDurationMillis = 200
