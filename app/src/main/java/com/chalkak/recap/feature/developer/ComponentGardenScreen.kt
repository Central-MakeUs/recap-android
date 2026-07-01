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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.ImageLoadFailureBottomSheet
import com.chalkak.recap.core.design.component.OrganizedCaptureCard
import com.chalkak.recap.core.design.component.PhotoAccessPermissionBottomSheet
import com.chalkak.recap.core.design.component.PhotoAccessPermissionBottomSheetText
import com.chalkak.recap.core.design.component.RecapButton
import com.chalkak.recap.core.design.component.RecapButtonSize
import com.chalkak.recap.core.design.component.ReviewRequiredScreenshotCard
import com.chalkak.recap.core.design.theme.RECAPTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComponentGardenScreen(
    modifier: Modifier = Modifier,
) {
    var showPhotoAccessPermissionBottomSheet by remember { mutableStateOf(false) }
    var showImageLoadFailureBottomSheet by remember { mutableStateOf(false) }

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
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_ui_components_section_title)
            ) {
                RecapButton(
                    text = stringResource(R.string.photo_access_permission_request_permission),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    size = RecapButtonSize.Medium,
                    shadowElevation = 20.dp
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_bottom_sheets_section_title),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showPhotoAccessPermissionBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_photo_access_permission_bottom_sheet_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showImageLoadFailureBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_image_load_failure_bottom_sheet_button
                        ),
                    )
                }
            }
        }
    }

    if (showPhotoAccessPermissionBottomSheet) {
        PhotoAccessPermissionBottomSheet(
            onDismissRequest = { showPhotoAccessPermissionBottomSheet = false },
            onPrimaryButtonClick = { showPhotoAccessPermissionBottomSheet = false },
            onLaterClick = { showPhotoAccessPermissionBottomSheet = false },
            text = PhotoAccessPermissionBottomSheetText(
                iconContentDescription = stringResource(
                    R.string.photo_access_permission_icon_content_description
                ),
                title = stringResource(R.string.photo_access_permission_title),
                description = stringResource(R.string.photo_access_permission_description),
                notice = stringResource(R.string.photo_access_permission_notice),
                primaryButton = stringResource(R.string.photo_access_permission_request_permission),
                laterButton = stringResource(R.string.photo_access_permission_later_button),
            ),
        )
    }
    if (showImageLoadFailureBottomSheet) {
        ImageLoadFailureBottomSheet(
            onDismissRequest = { showImageLoadFailureBottomSheet = false },
            onRetryClick = { showImageLoadFailureBottomSheet = false },
            onGoHomeClick = { showImageLoadFailureBottomSheet = false },
        )
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
