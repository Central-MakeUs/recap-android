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
import com.chalkak.recap.core.data.screenshot.AnalysisDataSourceMode
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapError

@Composable
internal fun DeveloperOptionsScreen(
    uiState: DeveloperOptionsUiState,
    onAction: (DeveloperOptionAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val switchTargetMode = when (uiState.analysisDataSourceMode) {
        AnalysisDataSourceMode.MOCK -> AnalysisDataSourceMode.REMOTE
        AnalysisDataSourceMode.REMOTE -> AnalysisDataSourceMode.MOCK
    }
    val currentModeLabel = stringResource(uiState.analysisDataSourceMode.labelResId)
    val switchButtonLabelResId = when (switchTargetMode) {
        AnalysisDataSourceMode.MOCK -> R.string.developer_options_switch_to_mock_button
        AnalysisDataSourceMode.REMOTE -> R.string.developer_options_switch_to_remote_button
    }

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
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (uiState.feedbackMessageResId != null) {
                Text(
                    text = stringResource(uiState.feedbackMessageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(
                    R.string.developer_options_analysis_data_source_current,
                    currentModeLabel,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSwitchAnalysisDataSource,
                onClick = {
                    onAction(DeveloperOptionAction.RequestAnalysisDataSourceSwitch(switchTargetMode))
                },
            ) {
                Text(stringResource(switchButtonLabelResId))
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

    val pendingTarget = uiState.pendingSwitchTargetMode
    if (pendingTarget != null) {
        RecapPopup(
            title = stringResource(R.string.developer_options_switch_analysis_data_source_confirm_title),
            description = stringResource(
                R.string.developer_options_switch_analysis_data_source_confirm_description,
            ),
            confirmButtonText = stringResource(
                R.string.developer_options_switch_analysis_data_source_confirm_button,
            ),
            cancelButtonText = stringResource(
                R.string.developer_options_switch_analysis_data_source_cancel_button,
            ),
            onConfirmClick = { onAction(DeveloperOptionAction.ConfirmAnalysisDataSourceSwitch) },
            onCancelClick = { onAction(DeveloperOptionAction.DismissAnalysisDataSourceSwitchDialog) },
            onDismissRequest = {
                onAction(DeveloperOptionAction.DismissAnalysisDataSourceSwitchDialog)
            },
            confirmButtonColor = RecapError,
        )
    }
}

private val AnalysisDataSourceMode.labelResId: Int
    @StringRes
    get() = when (this) {
        AnalysisDataSourceMode.MOCK -> R.string.developer_options_analysis_data_source_mock
        AnalysisDataSourceMode.REMOTE -> R.string.developer_options_analysis_data_source_remote
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
}

internal sealed interface DeveloperOptionAction {
    data object OpenComponentGarden : DeveloperOptionAction
    data object ResetOnboarding : DeveloperOptionAction
    data object ResetScreenshotData : DeveloperOptionAction
    data class RequestAnalysisDataSourceSwitch(
        val targetMode: AnalysisDataSourceMode,
    ) : DeveloperOptionAction

    data object ConfirmAnalysisDataSourceSwitch : DeveloperOptionAction
    data object DismissAnalysisDataSourceSwitchDialog : DeveloperOptionAction
}

@Preview(name = "Developer Options Mock", showBackground = true, widthDp = 360)
@Composable
private fun DeveloperOptionsScreenMockPreview() {
    RECAPTheme(dynamicColor = false) {
        DeveloperOptionsScreen(
            uiState = DeveloperOptionsUiState(
                analysisDataSourceMode = AnalysisDataSourceMode.MOCK,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Developer Options Remote Running", showBackground = true, widthDp = 360)
@Composable
private fun DeveloperOptionsScreenRemoteRunningPreview() {
    RECAPTheme(dynamicColor = false) {
        DeveloperOptionsScreen(
            uiState = DeveloperOptionsUiState(
                analysisDataSourceMode = AnalysisDataSourceMode.REMOTE,
                isAnalysisRunning = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Developer Options Switch Confirm", showBackground = true, widthDp = 360)
@Composable
private fun DeveloperOptionsScreenSwitchConfirmPreview() {
    RECAPTheme(dynamicColor = false) {
        DeveloperOptionsScreen(
            uiState = DeveloperOptionsUiState(
                analysisDataSourceMode = AnalysisDataSourceMode.MOCK,
                pendingSwitchTargetMode = AnalysisDataSourceMode.REMOTE,
            ),
            onAction = {},
        )
    }
}
