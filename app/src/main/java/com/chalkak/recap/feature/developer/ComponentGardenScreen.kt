package com.chalkak.recap.feature.developer

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.OrganizedCaptureCard
import com.chalkak.recap.core.design.component.ReviewRequiredScreenshotCard
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
internal fun ComponentGardenScreen(
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.component_garden_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_home_cards_section_title),
            ) {
                ReviewRequiredScreenshotCard(
                    reviewRequiredCount = ComponentGardenReviewRequiredCount,
                    onClick = {},
                )
                OrganizedCaptureCard(
                    organizedCaptureCount = ComponentGardenOrganizedCaptureCount,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun ComponentGardenSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Preview(name = "Component Garden", showBackground = true, widthDp = 360)
@Preview(
    name = "Component Garden - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ComponentGardenScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ComponentGardenScreen()
    }
}

private const val ComponentGardenReviewRequiredCount = 3
private const val ComponentGardenOrganizedCaptureCount = 12
