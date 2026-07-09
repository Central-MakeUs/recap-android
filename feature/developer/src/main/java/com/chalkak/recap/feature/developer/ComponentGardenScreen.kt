package com.chalkak.recap.feature.developer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottomsheet.OrganizeNotificationPermissionBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.LogoutConfirmationBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheet
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheetDefaults
import com.chalkak.recap.core.design.component.bottomsheet.RecapActionBottomSheetNoticeAlignment
import com.chalkak.recap.core.design.component.bottomsheet.WithdrawalConfirmationBottomSheet
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.card.FavoriteCategoryCard
import com.chalkak.recap.core.design.component.card.FrequentSaveTypeFolderCard
import com.chalkak.recap.core.design.component.card.OrganizedCaptureCard
import com.chalkak.recap.core.design.component.card.RecentOrganizedScreenshotCard
import com.chalkak.recap.core.design.component.card.ReviewRequiredScreenshotCard
import com.chalkak.recap.core.design.component.chip.RecapCategoryChip
import com.chalkak.recap.core.design.component.chip.RecapCategoryChipType
import com.chalkak.recap.core.design.component.chip.RecapFilterTag
import com.chalkak.recap.core.design.component.chip.RecapFilterTagOption
import com.chalkak.recap.core.design.component.input.RecapInputField
import com.chalkak.recap.core.design.component.search.RecapSearchBar
import com.chalkak.recap.core.design.component.toast.RecapToast
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.component.toast.rememberRecapToastHostState
import com.chalkak.recap.core.design.theme.RECAPTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComponentGardenScreen(
    modifier: Modifier = Modifier,
) {
    var showPhotoAccessPermissionBottomSheet by remember { mutableStateOf(false) }
    var showImageLoadFailureBottomSheet by remember { mutableStateOf(false) }
    var showOrganizeNotificationPermissionBottomSheet by remember { mutableStateOf(false) }
    var showNotificationDisabledBottomSheet by remember { mutableStateOf(false) }
    var showDeletionConfirmationActionBottomSheet by remember { mutableStateOf(false) }
    var showUnsavedChangesBottomSheet by remember { mutableStateOf(false) }
    var showLogoutConfirmationBottomSheet by remember { mutableStateOf(false) }
    var showWithdrawalConfirmationBottomSheet by remember { mutableStateOf(false) }
    var withdrawalConfirmationChecked by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var inputFieldValue by remember { mutableStateOf("") }
    var multilineInputFieldValue by remember { mutableStateOf("") }
    var isFavoriteCategoryCardFavorited by remember { mutableStateOf(false) }
    var selectedFilterTagOptionId by remember { mutableStateOf("latest") }
    var isFilterTagExpanded by remember { mutableStateOf(false) }
    val toastHostState = rememberRecapToastHostState()
    val coroutineScope = rememberCoroutineScope()
    val toastPreviewMessage = stringResource(R.string.recap_toast_preview_login_failed_message)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
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
                RecentOrganizedScreenshotCard(
                    thumbnailModel = R.drawable.mock_home_screenshot_return,
                    title = stringResource(R.string.home_recent_screenshot_return_title),
                    categoryLabel = stringResource(R.string.home_category_shopping_product),
                    onClick = {},
                )
                FrequentSaveTypeFolderCard(
                    categoryLabel = stringResource(R.string.home_category_shopping_product),
                    recapCount = ComponentGardenFrequentSaveTypeCount,
                    onClick = {},
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_category_chips_section_title),
            ) {
                ComponentGardenCategoryChips()
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_filter_tag_section_title),
            ) {
                RecapFilterTag(
                    options = listOf(
                        RecapFilterTagOption(
                            id = "latest",
                            label = stringResource(R.string.collection_sort_latest),
                        ),
                        RecapFilterTagOption(
                            id = "favorite",
                            label = stringResource(R.string.home_favorites_title),
                        ),
                    ),
                    selectedOptionId = selectedFilterTagOptionId,
                    onOptionSelected = { selectedFilterTagOptionId = it.id },
                    expanded = isFilterTagExpanded,
                    onExpandedChange = { isFilterTagExpanded = it },
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
                FavoriteCategoryCard(
                    thumbnailModel = R.drawable.bid_landscape_24px,
                    categoryLabel = stringResource(
                        R.string.component_garden_favorite_category_card_category_label
                    ),
                    title = stringResource(R.string.component_garden_favorite_category_card_title),
                    description = stringResource(
                        R.string.component_garden_favorite_category_card_description
                    ),
                    isFavorite = isFavoriteCategoryCardFavorited,
                    onClick = {},
                    onFavoriteClick = {
                        isFavoriteCategoryCardFavorited = !isFavoriteCategoryCardFavorited
                    },
                )
                RecapButton(
                    text = stringResource(R.string.photo_access_permission_request_permission),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    size = RecapButtonSize.Medium,
                    shadowElevation = 12.dp
                )
            }
            ComponentGardenSection(
                title = stringResource(R.string.component_garden_toasts_section_title),
            ) {
                RecapToast(
                    message = toastPreviewMessage,
                    type = RecapToastType.Success,
                )
                RecapToast(
                    message = toastPreviewMessage,
                    type = RecapToastType.Error,
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            toastHostState.showToast(
                                message = toastPreviewMessage,
                                type = RecapToastType.Success,
                            )
                        }
                    },
                ) {
                    Text(text = stringResource(R.string.component_garden_toast_success_button))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        coroutineScope.launch {
                            toastHostState.showToast(
                                message = toastPreviewMessage,
                                type = RecapToastType.Error,
                            )
                        }
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
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showOrganizeNotificationPermissionBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_organize_notification_permission_bottom_sheet_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showNotificationDisabledBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_notification_disabled_bottom_sheet_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDeletionConfirmationActionBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_deletion_confirmation_bottom_sheet_button
                        ),
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showUnsavedChangesBottomSheet = true },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_unsaved_changes_bottom_sheet_button
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
                    onClick = {
                        withdrawalConfirmationChecked = false
                        showWithdrawalConfirmationBottomSheet = true
                    },
                ) {
                    Text(
                        text = stringResource(
                            R.string.component_garden_withdrawal_confirmation_bottom_sheet_button
                        ),
                    )
                }
            }
        }
        }

        RecapToastHost(
            hostState = toastHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
    }

    if (showPhotoAccessPermissionBottomSheet) {
        RecapActionBottomSheet(
            icon = Icons.Outlined.Image,
            iconContentDescription = stringResource(
                R.string.photo_access_permission_icon_content_description
            ),
            iconStyle = RecapActionBottomSheetDefaults.primaryIconStyle(),
            title = stringResource(R.string.photo_access_permission_title),
            description = stringResource(R.string.photo_access_permission_description),
            topNotice = stringResource(R.string.photo_access_permission_notice),
            primaryButtonText = stringResource(R.string.photo_access_permission_request_permission),
            secondaryButtonText = stringResource(R.string.photo_access_permission_later_button),
            onDismissRequest = { showPhotoAccessPermissionBottomSheet = false },
            onPrimaryClick = { showPhotoAccessPermissionBottomSheet = false },
            onSecondaryClick = { showPhotoAccessPermissionBottomSheet = false },
        )
    }
    if (showImageLoadFailureBottomSheet) {
        RecapActionBottomSheet(
            icon = Icons.Outlined.BrokenImage,
            iconContentDescription = null,
            iconStyle = RecapActionBottomSheetDefaults.errorIconStyle(),
            title = stringResource(R.string.image_load_failure_title),
            description = stringResource(R.string.image_load_failure_description),
            primaryButtonText = stringResource(R.string.image_load_failure_retry_button),
            secondaryButtonText = stringResource(R.string.image_load_failure_home_button),
            onDismissRequest = { showImageLoadFailureBottomSheet = false },
            onPrimaryClick = { showImageLoadFailureBottomSheet = false },
            onSecondaryClick = { showImageLoadFailureBottomSheet = false },
            primaryButtonLeadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )
    }
    if (showOrganizeNotificationPermissionBottomSheet) {
        OrganizeNotificationPermissionBottomSheet(
            onDismissRequest = { showOrganizeNotificationPermissionBottomSheet = false },
            onAllowNotificationClick = { showOrganizeNotificationPermissionBottomSheet = false },
            onLaterClick = { showOrganizeNotificationPermissionBottomSheet = false },
        )
    }
    if (showNotificationDisabledBottomSheet) {
        RecapActionBottomSheet(
            icon = Icons.Outlined.NotificationsOff,
            iconContentDescription = stringResource(
                R.string.notification_disabled_icon_content_description
            ),
            iconStyle = RecapActionBottomSheetDefaults.surfaceVariantIconStyle(),
            title = stringResource(R.string.notification_disabled_title),
            description = stringResource(R.string.notification_disabled_description),
            bottomNotice = stringResource(R.string.notification_disabled_notice),
            bottomNoticeAlignment = RecapActionBottomSheetNoticeAlignment.Center,
            primaryButtonText = stringResource(R.string.notification_disabled_settings_button),
            secondaryButtonText = stringResource(R.string.notification_disabled_later_button),
            bottomPadding = 40.dp,
            onDismissRequest = { showNotificationDisabledBottomSheet = false },
            onPrimaryClick = { showNotificationDisabledBottomSheet = false },
            onSecondaryClick = { showNotificationDisabledBottomSheet = false },
        )
    }
    if (showDeletionConfirmationActionBottomSheet) {
        RecapActionBottomSheet(
            icon = Icons.Outlined.Delete,
            iconContentDescription = stringResource(
                R.string.deletion_confirmation_icon_content_description
            ),
            iconStyle = RecapActionBottomSheetDefaults.deleteIconStyle(),
            title = stringResource(R.string.deletion_confirmation_preview_title),
            description = stringResource(R.string.deletion_confirmation_preview_description),
            primaryButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            secondaryButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onDismissRequest = { showDeletionConfirmationActionBottomSheet = false },
            onPrimaryClick = { showDeletionConfirmationActionBottomSheet = false },
            onSecondaryClick = { showDeletionConfirmationActionBottomSheet = false },
            textSpacing = 12.dp,
            bottomPadding = 40.dp,
            primaryButtonColors = RecapActionBottomSheetDefaults.destructiveFilledColors(),
            primaryButtonElevation = 0.dp,
            secondaryButtonColors = RecapButtonDefaults.outlinedColors(),
            secondaryButtonSize = RecapButtonSize.Medium,
            secondaryButtonBorder = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }
    if (showUnsavedChangesBottomSheet) {
        RecapActionBottomSheet(
            icon = Icons.Outlined.ErrorOutline,
            iconContentDescription = stringResource(
                R.string.unsaved_changes_icon_content_description
            ),
            iconStyle = RecapActionBottomSheetDefaults.warningIconStyle(),
            title = stringResource(R.string.unsaved_changes_title),
            description = stringResource(R.string.unsaved_changes_description),
            primaryButtonText = stringResource(R.string.unsaved_changes_keep_editing_button),
            secondaryButtonText = stringResource(
                R.string.unsaved_changes_exit_without_saving_button
            ),
            onDismissRequest = { showUnsavedChangesBottomSheet = false },
            onPrimaryClick = { showUnsavedChangesBottomSheet = false },
            onSecondaryClick = { showUnsavedChangesBottomSheet = false },
            textSpacing = 12.dp,
            secondaryButtonColors = RecapActionBottomSheetDefaults.destructiveTextColors(),
        )
    }
    if (showLogoutConfirmationBottomSheet) {
        LogoutConfirmationBottomSheet(
            onDismissRequest = { showLogoutConfirmationBottomSheet = false },
            onCancelClick = { showLogoutConfirmationBottomSheet = false },
            onLogoutClick = { showLogoutConfirmationBottomSheet = false },
        )
    }
    if (showWithdrawalConfirmationBottomSheet) {
        WithdrawalConfirmationBottomSheet(
            checked = withdrawalConfirmationChecked,
            onCheckedChange = { withdrawalConfirmationChecked = it },
            onDismissRequest = { showWithdrawalConfirmationBottomSheet = false },
            onCancelClick = { showWithdrawalConfirmationBottomSheet = false },
            onWithdrawClick = { showWithdrawalConfirmationBottomSheet = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComponentGardenCategoryChips(
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecapCategoryChipType.entries.forEach { type ->
            RecapCategoryChip(type = type)
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
@Composable
private fun ComponentGardenScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        ComponentGardenScreen()
    }
}

private const val ComponentGardenReviewRequiredCount = 3
private const val ComponentGardenOrganizedCaptureCount = 12
private const val ComponentGardenFrequentSaveTypeCount = 12
