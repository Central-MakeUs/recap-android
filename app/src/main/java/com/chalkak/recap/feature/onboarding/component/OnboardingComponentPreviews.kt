package com.chalkak.recap.feature.onboarding.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme

@Preview(name = "Light", showBackground = true, widthDp = 360)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    widthDp = 360,
)
annotation class OnboardingComponentPreview

@Composable
internal fun OnboardingComponentPreviewContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    RECAPTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = modifier.padding(24.dp)) {
                content()
            }
        }
    }
}
