package com.chalkak.recap.feature.screenshot

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotRoute(
    imageId: String,
    onNavigateBack: () -> Unit,
    viewModel: ScreenshotViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(ScreenshotDestination.Detail)
    var showActionSheet by rememberSaveable { mutableStateOf(false) }
    var showTypePicker by rememberSaveable { mutableStateOf(false) }
    var tempTypeSelection by rememberSaveable {
        mutableStateOf(ScreenshotContentType.OTHER.name)
    }

    LaunchedEffect(imageId) {
        viewModel.bind(imageId)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ScreenshotEvent.SaveSucceeded -> {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }

                ScreenshotEvent.DeleteSucceeded -> {
                    showActionSheet = false
                    onNavigateBack()
                }
            }
        }
    }

    val contentState = uiState as? ScreenshotUiState.Content
    val tempType = runCatching {
        ScreenshotContentType.valueOf(tempTypeSelection)
    }.getOrDefault(ScreenshotContentType.OTHER)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                val current = backStack.lastOrNull()
                if (current is ScreenshotDestination.Edit) {
                    viewModel.onAction(ScreenshotAction.DiscardEditDraft)
                }
                backStack.removeLastOrNull()
            } else {
                onNavigateBack()
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
                    val editContent = contentState
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
                            onCancel = {
                                viewModel.onAction(ScreenshotAction.DiscardEditDraft)
                                backStack.removeLastOrNull()
                            },
                            onDone = {
                                viewModel.onAction(ScreenshotAction.SaveEdit)
                            },
                            onChangeType = {
                                tempTypeSelection = editContent.editDraft.contentType.name
                                showTypePicker = true
                            },
                        )
                    }
                }

                ScreenshotDestination.Fullscreen -> NavEntry(destination) {
                    val imageModel = contentState?.let { content ->
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
                viewModel.onAction(ScreenshotAction.DeleteScreenshot)
            },
            onCloseClick = { showActionSheet = false },
            enabled = !contentState.isDeleting,
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
