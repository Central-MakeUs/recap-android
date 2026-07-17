package com.chalkak.recap.feature.screenshot

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.component.toast.rememberRecapToastHostState
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotRoute(
    imageId: String,
    onNavigateBack: () -> Unit,
    onDeleteSucceeded: () -> Unit,
    viewModel: ScreenshotViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(ScreenshotDestination.Detail)
    val toastHostState = rememberRecapToastHostState()
    val toastHazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    val favoriteAddedToastMessage = stringResource(R.string.screenshot_detail_favorite_added_toast)
    val favoriteRemovedToastMessage = stringResource(R.string.screenshot_detail_favorite_removed_toast)
    var showActionSheet by rememberSaveable { mutableStateOf(false) }
    var showTypePicker by rememberSaveable { mutableStateOf(false) }
    var tempTypeSelection by rememberSaveable {
        mutableStateOf(ScreenshotContentType.OTHER.name)
    }

    LaunchedEffect(imageId) {
        viewModel.bind(imageId)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ScreenshotEvent.SaveSucceeded -> {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }

                ScreenshotEvent.DeleteSucceeded -> {
                    showActionSheet = false
                    onDeleteSucceeded()
                }

                is ScreenshotEvent.ShowFavoriteToast -> {
                    toastHostState.showToast(
                        message = if (event.isFavorite) {
                            favoriteAddedToastMessage
                        } else {
                            favoriteRemovedToastMessage
                        },
                        type = RecapToastType.Success,
                    )
                }
            }
        }
    }

    val contentState = uiState as? ScreenshotUiState.Content
    val tempType = runCatching {
        ScreenshotContentType.valueOf(tempTypeSelection)
    }.getOrDefault(ScreenshotContentType.OTHER)

    fun leaveEditScreen() {
        // Prefer uiState over contentState snapshot (NavEntry content can be remembered).
        val content = uiState as? ScreenshotUiState.Content
        if (content != null && content.hasUnsavedEditChanges()) {
            viewModel.onAction(ScreenshotAction.ShowDiscardEditConfirmDialog)
            return
        }
        viewModel.onAction(ScreenshotAction.DiscardEditDraft)
        if (backStack.lastOrNull() is ScreenshotDestination.Edit) {
            backStack.removeLastOrNull()
        }
    }

    fun confirmLeaveEditScreen() {
        viewModel.onAction(ScreenshotAction.DiscardEditDraft)
        if (backStack.lastOrNull() is ScreenshotDestination.Edit) {
            backStack.removeLastOrNull()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = toastHazeState),
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = {
                    when {
                        backStack.size <= 1 -> onNavigateBack()
                        backStack.lastOrNull() is ScreenshotDestination.Edit -> leaveEditScreen()
                        else -> backStack.removeLastOrNull()
                    }
                },
                entryProvider = { destination ->
                    when (destination) {
                        ScreenshotDestination.Detail -> NavEntry(destination) {
                            ScreenshotDetailScreen(
                                uiState = uiState,
                                onAction = viewModel::onAction,
                                onNavigateBack = onNavigateBack,
                                onOpenEdit = {
                                    showActionSheet = false
                                    viewModel.onAction(ScreenshotAction.PrepareEditDraft)
                                    if (backStack.lastOrNull() !is ScreenshotDestination.Edit) {
                                        backStack.add(ScreenshotDestination.Edit)
                                    }
                                },
                                onOpenFullscreen = {
                                    if (backStack.lastOrNull() !is ScreenshotDestination.Fullscreen) {
                                        backStack.add(ScreenshotDestination.Fullscreen)
                                    }
                                },
                                onOpenMore = { showActionSheet = true },
                            )
                        }

                        ScreenshotDestination.Edit -> NavEntry(destination) {
                            val editContent = uiState as? ScreenshotUiState.Content
                            if (editContent == null) {
                                ScreenshotDetailScreen(
                                    uiState = uiState,
                                    onAction = viewModel::onAction,
                                    onNavigateBack = {
                                        backStack.removeLastOrNull()
                                    },
                                    onOpenEdit = {},
                                    onOpenFullscreen = {},
                                    onOpenMore = {},
                                )
                            } else {
                                ScreenshotEditScreen(
                                    content = editContent,
                                    onAction = viewModel::onAction,
                                    onCancel = ::leaveEditScreen,
                                    onDone = {
                                        viewModel.onAction(ScreenshotAction.SaveEdit)
                                    },
                                    onChangeType = {
                                        tempTypeSelection = editContent.editDraft.contentType.name
                                        showTypePicker = true
                                    },
                                    onOpenFullscreen = {
                                        if (backStack.lastOrNull() !is ScreenshotDestination.Fullscreen) {
                                            backStack.add(ScreenshotDestination.Fullscreen)
                                        }
                                    },
                                )
                            }
                        }

                        ScreenshotDestination.Fullscreen -> NavEntry(destination) {
                            val fullscreenContent = uiState as? ScreenshotUiState.Content
                            val imageModel = fullscreenContent?.let { content ->
                                resolveScreenshotImageModel(
                                    storedImagePath = content.card.imageRefs.storedImagePath,
                                    sourceImageUri = content.card.imageRefs.sourceImageUri,
                                    thumbnailPath = content.card.imageRefs.thumbnailPath,
                                    priority = ScreenshotImageResolvePriority.Fullscreen,
                                )
                            }
                            ScreenshotFullscreenScreen(
                                imageModel = imageModel,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }

                        else -> error("Unknown screenshot destination: $destination")
                    }
                },
            )
        }

        RecapToastHost(
            hostState = toastHostState,
            hazeState = toastHazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
    }

    if (showActionSheet && contentState != null) {
        ScreenshotActionBottomSheet(
            onDismissRequest = { showActionSheet = false },
            onEditClick = {
                showActionSheet = false
                viewModel.onAction(ScreenshotAction.PrepareEditDraft)
                if (backStack.lastOrNull() !is ScreenshotDestination.Edit) {
                    backStack.add(ScreenshotDestination.Edit)
                }
            },
            onDeleteClick = {
                showActionSheet = false
                viewModel.onAction(ScreenshotAction.ShowDeleteConfirmDialog)
            },
            onCloseClick = { showActionSheet = false },
            enabled = !contentState.isDeleting,
        )
    }

    if (contentState?.showDeleteConfirmDialog == true) {
        RecapPopup(
            title = stringResource(R.string.screenshot_delete_confirm_title),
            description = stringResource(R.string.screenshot_delete_confirm_description),
            confirmButtonText = stringResource(R.string.deletion_confirmation_delete_button),
            cancelButtonText = stringResource(R.string.deletion_confirmation_cancel_button),
            onConfirmClick = { viewModel.onAction(ScreenshotAction.DeleteScreenshot) },
            onCancelClick = { viewModel.onAction(ScreenshotAction.DismissDeleteConfirmDialog) },
            onDismissRequest = { viewModel.onAction(ScreenshotAction.DismissDeleteConfirmDialog) },
            confirmButtonColor = RecapError,
        )
    }

    if (contentState?.showDiscardEditConfirmDialog == true) {
        RecapPopup(
            title = stringResource(R.string.screenshot_edit_discard_confirm_title),
            description = stringResource(R.string.screenshot_edit_discard_confirm_description),
            confirmButtonText = stringResource(R.string.screenshot_edit_discard_confirm_quit),
            cancelButtonText = stringResource(R.string.screenshot_edit_discard_confirm_keep_editing),
            onConfirmClick = ::confirmLeaveEditScreen,
            onCancelClick = {
                viewModel.onAction(ScreenshotAction.DismissDiscardEditConfirmDialog)
            },
            onDismissRequest = {
                viewModel.onAction(ScreenshotAction.DismissDiscardEditConfirmDialog)
            },
            confirmButtonColor = RecapError,
        )
    }

    if (showTypePicker && contentState != null) {
        ScreenshotTypePickerBottomSheet(
            selectedType = tempType,
            onDismissRequest = { showTypePicker = false },
            onTypeSelected = { type ->
                tempTypeSelection = type.name
            },
            onConfirmClick = {
                viewModel.onAction(ScreenshotAction.UpdateEditContentType(tempType))
                showTypePicker = false
            },
        )
    }
}

@Serializable
private sealed interface ScreenshotDestination : NavKey {
    @Serializable
    data object Detail : ScreenshotDestination

    @Serializable
    data object Edit : ScreenshotDestination

    @Serializable
    data object Fullscreen : ScreenshotDestination
}
