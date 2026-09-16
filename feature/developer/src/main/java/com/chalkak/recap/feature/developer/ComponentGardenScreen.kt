package com.chalkak.recap.feature.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.bottomsheet.AiDataTransferConsentBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.LogoutConfirmationBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.NotificationPermissionRequestBottomSheet
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.FrequentSaveTypeFolderCard
import com.chalkak.recap.core.design.component.card.HomeFavoriteCard
import com.chalkak.recap.core.design.component.card.OrganizedScreenshotSummaryCard
import com.chalkak.recap.core.design.component.card.RecapHazeFolderCard
import com.chalkak.recap.core.design.component.card.RecentOrganizedScreenshotCard
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.component.card.ShareFavoriteGuideCard
import com.chalkak.recap.core.design.component.chip.RecapCategoryRoundChip
import com.chalkak.recap.core.design.component.chip.RecapCategoryTextChip
import com.chalkak.recap.core.design.component.chip.RecapCategoryTextChipWithIcon
import com.chalkak.recap.core.design.component.chip.RecapSortToggle
import com.chalkak.recap.core.design.component.input.RecapInputField
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.component.swipe.ScreenshotCardSwipeRow
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.ProvideRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToast
import com.chalkak.recap.core.design.component.toast.RecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.feature.organize.OrganizeAction
import com.chalkak.recap.feature.organize.OrganizeUiState
import com.chalkak.recap.feature.organize.ScreenshotPicker
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComponentGardenScreen(
    modifier: Modifier = Modifier,
) {
    var showNotificationPermissionRequestBottomSheet by remember { mutableStateOf(false) }
    var showAiDataTransferConsentBottomSheet by remember { mutableStateOf(false) }
    var showLogoutConfirmationBottomSheet by remember { mutableStateOf(false) }
    var showScreenshotPicker by remember { mutableStateOf(false) }
    var showConfirmPopupError by remember { mutableStateOf(false) }
    var showConfirmPopupPrimary by remember { mutableStateOf(false) }
    var screenshotSelectionUiState by remember {
        mutableStateOf(
            OrganizeUiState(isLoading = false),
        )
    }
    var searchQuery by remember { mutableStateOf("") }
    var inputFieldValue by remember { mutableStateOf("") }
    var multilineInputFieldValue by remember { mutableStateOf("") }
    var isScreenshotCardFavorited by remember { mutableStateOf(false) }
    var isScreenshotCardSwipeRevealed by remember { mutableStateOf(false) }
    var isSortLatest by remember { mutableStateOf(true) }
    val toastHazeState = rememberHazeState()
    val toastDispatcher = LocalRecapToastDispatcher.current
    val toastPreviewMessage = stringResource(R.string.recap_toast_preview_login_failed_message)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .hazeSource(state = toastHazeState),
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
                style = RecapHeading3,
                color = MaterialTheme.colorScheme.onBackground,
            )
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_home_cards_section_title),
            ) {
                OrganizedScreenshotSummaryCard(
                    organizedCount = ComponentGardenOrganizedScreenshotSummaryCount,
                )
                ShareFavoriteGuideCard(onClick = {})
                RecentOrganizedScreenshotCard(
                    thumbnailModel = null,
                    title = stringResource(R.string.recent_organized_screenshot_card_preview_title),
                    categoryType = RecapCategoryType.InfoKnowledge,
                    onClick = {},
                )
                FrequentSaveTypeFolderCard(
                    categoryLabel = stringResource(R.string.category_type_shopping_product),
                    recapCount = ComponentGardenFrequentSaveTypeCount,
                    onClick = {},
                )
                HomeFavoriteCard(
                    categoryType = RecapCategoryType.RecordCapture,
                    title = stringResource(R.string.home_favorite_card_preview_title),
                    onClick = {},
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_haze_folder_cards_section_title),
            ) {
                ComponentGardenHazeFolderCards()
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_category_chips_section_title),
            ) {
                ComponentGardenCategoryChips()
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_sort_toggle_section_title),
            ) {
                RecapSortToggle(
                    label = stringResource(
                        if (isSortLatest) {
                            R.string.collection_sort_latest
                        } else {
                            R.string.collection_sort_oldest
                        },
                    ),
                    onClick = { isSortLatest = !isSortLatest },
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_input_field_section_title),
            ) {
                RecapInputField(
                    value = inputFieldValue,
                    onValueChange = { inputFieldValue = it },
                    label = stringResource(R.string.recap_input_field_preview_label),
                    placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
                )
                RecapInputField(
                    value = "",
                    onValueChange = {},
                    label = stringResource(R.string.recap_input_field_preview_label),
                    placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
                    isError = true,
                    errorMessage = stringResource(R.string.recap_input_field_preview_error_message),
                )
                RecapInputField(
                    value = multilineInputFieldValue,
                    onValueChange = { multilineInputFieldValue = it },
                    label = stringResource(R.string.recap_input_field_preview_label),
                    placeholder = stringResource(R.string.recap_input_field_preview_placeholder),
                    singleLine = false,
                    minLines = 4,
                    maxLength = 300,
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_ui_components_section_title)
            ) {
                RecapSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                )
                ScreenshotCardSwipeRow(
                    onEditClick = {},
                    onDeleteClick = {},
                    revealed = isScreenshotCardSwipeRevealed,
                    onRevealedChange = { revealed ->
                        isScreenshotCardSwipeRevealed = revealed
                    },
                ) {
                    ScreenshotCard(
                        thumbnailModel = R.drawable.bid_landscape_24px,
                        categoryType = RecapCategoryType.ShoppingProduct,
                        title = stringResource(R.string.component_garden_screenshot_card_title),
                        description = stringResource(
                            R.string.component_garden_screenshot_card_description
                        ),
                        isFavorite = isScreenshotCardFavorited,
                        onClick = {},
                        onFavoriteClick = {
                            isScreenshotCardFavorited = !isScreenshotCardFavorited
                        },
                        suppressPressScale = isGestureActive,
                        modifier = Modifier.screenshotCardSwipeSemantics(),
                    )
                }
                RecapButton(
                    text = stringResource(R.string.photo_access_permission_request_permission),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    size = RecapButtonSize.Medium,
                    shadowElevation = 12.dp
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_popups_section_title),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showConfirmPopupError = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_confirm_popup_error_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showConfirmPopupPrimary = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_confirm_popup_primary_button
                        ),
                    )
                }
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_toasts_section_title),
            ) {
                RecapToast(
                    message = toastPreviewMessage,
                    type = RecapToastType.Success,
                    hazeState = toastHazeState,
                )
                RecapToast(
                    message = toastPreviewMessage,
                    type = RecapToastType.Error,
                    hazeState = toastHazeState,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        toastDispatcher.showToast(
                            message = toastPreviewMessage,
                            type = RecapToastType.Success,
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.component_garden_toast_success_button))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        toastDispatcher.showToast(
                            message = toastPreviewMessage,
                            type = RecapToastType.Error,
                        )
                    },
                ) {
                    Text(text = stringResource(R.string.component_garden_toast_error_button))
                }
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_bottom_sheets_section_title),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showNotificationPermissionRequestBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_organize_notification_permission_bottom_sheet_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showAiDataTransferConsentBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_ai_data_transfer_consent_bottom_sheet_button
                        ),
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showLogoutConfirmationBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_logout_confirmation_bottom_sheet_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showScreenshotPicker = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_screenshot_picker_button
                        ),
                    )
                }
            }
        }
    }
    if (showNotificationPermissionRequestBottomSheet) {
        NotificationPermissionRequestBottomSheet(
            onDismissRequest = { showNotificationPermissionRequestBottomSheet = false },
            onAllowNotificationClick = { showNotificationPermissionRequestBottomSheet = false },
            onLaterClick = { showNotificationPermissionRequestBottomSheet = false },
        )
    }
    if (showAiDataTransferConsentBottomSheet) {
        AiDataTransferConsentBottomSheet(
            onDismissRequest = { showAiDataTransferConsentBottomSheet = false },
            onAgreeClick = { showAiDataTransferConsentBottomSheet = false },
            onCancelClick = { showAiDataTransferConsentBottomSheet = false },
            onPrivacyPolicyClick = {},
        )
    }
    if (showLogoutConfirmationBottomSheet) {
        LogoutConfirmationBottomSheet(
            onDismissRequest = { showLogoutConfirmationBottomSheet = false },
            onCancelClick = { showLogoutConfirmationBottomSheet = false },
            onLogoutClick = { showLogoutConfirmationBottomSheet = false },
        )
    }
    if (showScreenshotPicker) {
        ScreenshotPicker(
            uiState = screenshotSelectionUiState,
            onAction = { action ->
                screenshotSelectionUiState = screenshotSelectionUiState.reduceGardenAction(action)
            },
            onDismissRequest = { showScreenshotPicker = false },
            onCloseClick = { showScreenshotPicker = false },
            onConfirmClick = { showScreenshotPicker = false },
        )
    }
    if (showConfirmPopupError) {
        RecapPopup(
            title = stringResource(R.string.recap_popup_preview_title),
            description = stringResource(R.string.recap_popup_preview_description),
            confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onConfirmClick = { showConfirmPopupError = false },
            onCancelClick = { showConfirmPopupError = false },
            onDismissRequest = { showConfirmPopupError = false },
            confirmButtonColor = RecapError,
        )
    }
    if (showConfirmPopupPrimary) {
        RecapPopup(
            title = stringResource(R.string.recap_popup_preview_title),
            description = stringResource(R.string.recap_popup_preview_description),
            confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onConfirmClick = { showConfirmPopupPrimary = false },
            onCancelClick = { showConfirmPopupPrimary = false },
            onDismissRequest = { showConfirmPopupPrimary = false },
            confirmButtonColor = RecapBlue300,
        )
    }
}

private fun OrganizeUiState.reduceGardenAction(action: OrganizeAction): OrganizeUiState {
    return when (action) {
        is OrganizeAction.ToggleSelection -> {
            val currentSelection = selectedUris
            when {
                action.uri in currentSelection -> {
                    copy(selectedUris = currentSelection.filterNot { it == action.uri })
                }

                currentSelection.size >= ComponentGardenScreenshotPickerMaxCount -> {
                    copy(showMaxSelectionReached = true)
                }

                else -> {
                    copy(selectedUris = currentSelection + action.uri)
                }
            }
        }

        is OrganizeAction.RemoveSelection -> {
            copy(selectedUris = selectedUris.filterNot { it == action.uri })
        }

        OrganizeAction.ClearSelection -> {
            copy(
                selectedUris = emptyList(),
                showMaxSelectionReached = false,
            )
        }

        OrganizeAction.DismissMaxSelectionMessage -> {
            copy(showMaxSelectionReached = false)
        }

        OrganizeAction.StartOrganizing,
        OrganizeAction.AgreeAiDataTransferConsent,
        OrganizeAction.DismissAiDataTransferConsent,
            -> this
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComponentGardenCategoryChips(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryRoundChip(type = type)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryTextChip(type = type)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecapCategoryType.entries.forEach { type ->
                RecapCategoryTextChipWithIcon(type = type)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComponentGardenHazeFolderCards(
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        maxItemsInEachRow = 3,
    ) {
        ComponentGardenHazeFolderCardItems.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(ComponentGardenHazeFolderCardWidth),
            ) {
                RecapHazeFolderCard(
                    category = item.category,
                    recapCount = item.recapCount,
                    onClick = {},
                )
                Text(
                    text = stringResource(item.category.labelResId),
                    style = RecapCaption2,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
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
            style = RecapHeading2,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Preview(name = "Component Garden", showBackground = true, widthDp = 360)
@Composable
private fun ComponentGardenScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ProvideRecapToastDispatcher(
            dispatcher = object : RecapToastDispatcher {
                override fun showToast(
                    message: String,
                    type: RecapToastType,
                    duration: RecapToastDuration,
                ) = Unit
            },
        ) {
            ComponentGardenScreen()
        }
    }
}

private const val ComponentGardenOrganizedScreenshotSummaryCount = 128
private const val ComponentGardenFrequentSaveTypeCount = 12
private const val ComponentGardenScreenshotPickerMaxCount = 20
private val ComponentGardenHazeFolderCardWidth = 99.dp

private data class ComponentGardenHazeFolderCardItem(
    val category: RecapCategoryType,
    val recapCount: Int,
)

private val ComponentGardenHazeFolderCardItems = listOf(
    ComponentGardenHazeFolderCardItem(RecapCategoryType.JobCareer, 8),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.ShoppingProduct, 20),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.PlaceRestaurant, 23),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.ScheduleReservation, 10),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.InfoKnowledge, 12),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.BookContent, 1),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.BenefitEvent, 5),
    ComponentGardenHazeFolderCardItem(RecapCategoryType.RecordCapture, 12),
)
