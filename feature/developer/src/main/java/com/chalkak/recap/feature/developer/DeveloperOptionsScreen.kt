package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3

@Composable
internal fun DeveloperOptionsScreen(
    uiState: DeveloperOptionsUiState,
    onAction: (DeveloperOptionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.developer_options_title),
                style = RecapHeading3,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (uiState.feedbackMessageResId != null) {
                Text(
                    text = stringResource(uiState.feedbackMessageResId),
                    style = RecapBody2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DeveloperOption.entries.forEach { option ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(option.action) },
                ) {
                    Text(stringResource(option.labelResId))
                }
            }
        }
    }
}

internal enum class DeveloperOption(
    @get:StringRes val labelResId: Int,
    val action: DeveloperOptionAction,
) {
    ComponentGarden(
        labelResId = R.string.developer_options_component_garden_button,
        action = DeveloperOptionAction.OpenComponentGarden,
    ),
    ResetOnboarding(
        labelResId = R.string.developer_options_reset_onboarding_button,
        action = DeveloperOptionAction.ResetOnboarding,
    ),
    ResetScreenshotData(
        labelResId = R.string.developer_options_reset_screenshot_data_button,
        action = DeveloperOptionAction.ResetScreenshotData,
    ),
    TestCrash(
        labelResId = R.string.developer_options_test_crash_button,
        action = DeveloperOptionAction.ForceTestCrash,
    ),
}

internal sealed interface DeveloperOptionAction {
    data object OpenComponentGarden : DeveloperOptionAction
    data object ResetOnboarding : DeveloperOptionAction
    data object ResetScreenshotData : DeveloperOptionAction
    data object ForceTestCrash : DeveloperOptionAction
}

@Preview(name = "Developer Options", showBackground = true, widthDp = 360)
@Composable
private fun DeveloperOptionsScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        DeveloperOptionsScreen(
            uiState = DeveloperOptionsUiState(),
            onAction = {},
        )
    }
}

@Preview(name = "Developer Options Feedback", showBackground = true, widthDp = 360)
@Composable
private fun DeveloperOptionsScreenFeedbackPreview() {
    RECAPTheme(dynamicColor = false) {
        DeveloperOptionsScreen(
            uiState = DeveloperOptionsUiState(
                feedbackMessageResId = R.string.developer_options_reset_screenshot_data_success,
            ),
            onAction = {},
        )
    }
}
