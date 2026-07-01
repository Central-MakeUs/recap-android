package com.chalkak.recap.feature.developer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R

@Composable
internal fun DeveloperOptionsScreen(
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.developer_options_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
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
    TechnicalDemo(
        labelResId = R.string.developer_options_technical_demo_button,
        action = DeveloperOptionAction.OpenTechnicalDemo,
    ),
    ComponentGarden(
        labelResId = R.string.developer_options_component_garden_button,
        action = DeveloperOptionAction.OpenComponentGarden,
    ),
    ResetOnboarding(
        labelResId = R.string.developer_options_reset_onboarding_button,
        action = DeveloperOptionAction.ResetOnboarding,
    ),
}

internal sealed interface DeveloperOptionAction {
    data object OpenTechnicalDemo : DeveloperOptionAction
    data object OpenComponentGarden : DeveloperOptionAction
    data object ResetOnboarding : DeveloperOptionAction
}
