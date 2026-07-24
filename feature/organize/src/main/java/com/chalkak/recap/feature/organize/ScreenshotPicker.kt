@file:OptIn(ExperimentalMaterial3Api::class)

package com.chalkak.recap.feature.organize

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.popup.RecapPopupContent
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.RecapToastExitAnimationDurationMillis
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastPresentation
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.model.LocalImage
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotPicker(
    uiState: OrganizeUiState,
    onAction: (OrganizeAction) -> Unit,
    onDismissRequest: () -> Unit,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
) {
    val screenHeight = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    var showDiscardSelectionConfirm by remember { mutableStateOf(false) }
    var zoomImageModel by remember { mutableStateOf<Any?>(null) }
    // 최초 Composition/측정은 shell만 두고, show()로 target이 non-hidden이 되는 순간
    // gallery를 활성화해 상승 애니메이션과 이미지 로딩이 겹치도록 한다.
    var showGalleryBody by remember { mutableStateOf(false) }
    // entrance 중 sheet offset이 매 프레임 바뀌며 onGloballyPositioned가 폭주하므로 settle 후에만 켠다.
    var trackItemBounds by remember { mutableStateOf(false) }
    val requestExit = {
        if (zoomImageModel != null) {
            zoomImageModel = null
        } else if (uiState.selectionCount >= MIN_SELECTION_COUNT) {
            showDiscardSelectionConfirm = true
        } else {
            onCloseClick()
        }
    }

    LaunchedEffect(sheetState) {
        snapshotFlow {
            sheetState.targetValue != SheetValue.Hidden
        }.first { visibleTarget -> visibleTarget }
        showGalleryBody = true
    }

    LaunchedEffect(sheetState) {
        snapshotFlow {
            sheetState.targetValue != SheetValue.Hidden && !sheetState.isAnimationRunning
        }.first { settled -> settled }
        trackItemBounds = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = ScreenshotPickerTokens.SheetTopCornerRadius,
            topEnd = ScreenshotPickerTokens.SheetTopCornerRadius,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null,
        // Material Expanded→partialExpand back을 막고, Dialog OnBackPressedDispatcher에 직접 연결한다.
        // activity-compose BackHandler는 NavigationEventDispatcher(Activity)를 우선해 Dialog back을 못 받는다.
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false),
    ) {
        ScreenshotPickerDialogBackHandler(onBack = requestExit)

        ScreenshotPickerContent(
            uiState = uiState,
            onAction = onAction,
            onCloseClick = requestExit,
            onConfirmClick = onConfirmClick,
            onImageLongClick = { imageModel -> zoomImageModel = imageModel },
            sheetState = sheetState,
            showGalleryBody = showGalleryBody,
            trackItemBounds = trackItemBounds,
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * ScreenshotPickerTokens.ExpandedSheetHeightFraction),
        )
    }

    zoomImageModel?.let { imageModel ->
        ScreenshotPickerZoomOverlay(
            imageModel = imageModel,
            onDismissRequest = { zoomImageModel = null },
        )
    }

    if (showDiscardSelectionConfirm) {
        RecapPopup(
            title = stringResource(R.string.organize_discard_selection_confirm_title),
            description = stringResource(R.string.organize_discard_selection_confirm_description),
            confirmButtonText = stringResource(R.string.organize_discard_selection_confirm_quit),
            cancelButtonText = stringResource(
                R.string.organize_discard_selection_confirm_keep_organizing,
            ),
            onConfirmClick = {
                showDiscardSelectionConfirm = false
                onCloseClick()
            },
            onCancelClick = { showDiscardSelectionConfirm = false },
            onDismissRequest = { showDiscardSelectionConfirm = false },
            confirmButtonColor = RecapError,
        )
    }
}

@Composable
private fun ScreenshotPickerDialogBackHandler(onBack: () -> Unit) {
    // CompositionLocal/NavigationEvent(Activity)가 아니라 Dialog 윈도우 ViewTree dispatcher를 쓴다.
    val backDispatcher = LocalView.current
        .findViewTreeOnBackPressedDispatcherOwner()
        ?.onBackPressedDispatcher
    val currentOnBack by rememberUpdatedState(onBack)

    DisposableEffect(backDispatcher) {
        if (backDispatcher == null) {
            return@DisposableEffect onDispose { }
        }
        val callback = object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
        backDispatcher.addCallback(callback)
        onDispose { callback.remove() }
    }
}

@Composable
fun ScreenshotPickerContent(
    uiState: OrganizeUiState,
    onAction: (OrganizeAction) -> Unit,
    onCloseClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState? = null,
    showGalleryBody: Boolean = true,
    trackItemBounds: Boolean = true,
    onImageLongClick: (Any) -> Unit = {},
) {
    // ModalBottomSheet는 Dialog 윈도우라 앱 루트 RecapToastHost가 가려진다. 시트 안에 호스트한다.
    val toastHazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    var currentToast by remember { mutableStateOf<RecapToastPresentation?>(null) }
    val maxSelectionMessage = stringResource(
        R.string.organize_max_selection_message,
        MAX_SELECTION_COUNT,
    )
    // Partial에서 시트 content 하단은 화면 밖이라 Popup이 window 하단으로 clamp된다.
    // IntOffset 여백은 clamp에 먹히므로, bottom padding을 Popup content에 둔다.
    // hide() 중에만 sheet offset 증가분을 Popup translation으로 싱크한다.
    var ctaHideSinkY by remember { mutableFloatStateOf(0f) }
    val confirmAppearProgress = remember { Animatable(0f) }
    var isConfirmButtonPresent by remember { mutableStateOf(false) }
    val itemBoundsInRoot = remember { mutableMapOf<String, Rect>() }
    val dragSelectBaseline = remember { mutableSetOf<String>() }
    val dragSelectLocalSelected = remember { mutableSetOf<String>() }
    var isDragSelecting by remember { mutableStateOf(false) }
    var dragSelectActivate by remember { mutableStateOf(true) }
    var dragSelectStartIndex by remember { mutableIntStateOf(-1) }
    var dragSelectEndIndex by remember { mutableIntStateOf(-1) }
    val currentUiState by rememberUpdatedState(uiState)
    val currentOnAction by rememberUpdatedState(onAction)

    fun hitTestUri(positionInRoot: Offset): String? {
        return itemBoundsInRoot.entries
            .firstOrNull { (_, bounds) -> bounds.contains(positionInRoot) }
            ?.key
    }

    fun applyDragSelectRange(endIndex: Int) {
        if (!isDragSelecting || dragSelectStartIndex < 0) return
        dragSelectEndIndex = endIndex
        val screenshots = currentUiState.availableScreenshots
        val from = minOf(dragSelectStartIndex, endIndex)
        val to = maxOf(dragSelectStartIndex, endIndex)
        var notifiedMaxSelection = false

        screenshots.forEachIndexed { index, screenshot ->
            val uri = screenshot.uri
            val desiredSelected = if (index in from..to) {
                dragSelectActivate
            } else {
                uri in dragSelectBaseline
            }
            val isSelected = uri in dragSelectLocalSelected
            if (desiredSelected == isSelected) return@forEachIndexed

            if (desiredSelected) {
                if (dragSelectLocalSelected.size >= MAX_SELECTION_COUNT) {
                    if (!notifiedMaxSelection) {
                        // ViewModel이 상한 도달 토스트를 띄우도록 한 번 시도한다.
                        currentOnAction(OrganizeAction.ToggleSelection(uri))
                        notifiedMaxSelection = true
                    }
                    return@forEachIndexed
                }
                dragSelectLocalSelected += uri
                currentOnAction(OrganizeAction.ToggleSelection(uri))
            } else {
                dragSelectLocalSelected -= uri
                currentOnAction(OrganizeAction.RemoveSelection(uri))
            }
        }
    }

    fun onDragSelectStart(uri: String) {
        val startIndex = currentUiState.availableScreenshots
            .indexOfFirst { screenshot -> screenshot.uri == uri }
        if (startIndex < 0) return

        isDragSelecting = true
        dragSelectBaseline.clear()
        dragSelectBaseline.addAll(currentUiState.selectedUris)
        dragSelectLocalSelected.clear()
        dragSelectLocalSelected.addAll(currentUiState.selectedUris)
        // 시작 항목이 미선택이면 활성화(선택) 드래그, 선택되어 있으면 비활성화(해제) 드래그.
        dragSelectActivate = uri !in dragSelectBaseline
        dragSelectStartIndex = startIndex
        applyDragSelectRange(startIndex)
    }

    fun onDragSelectMove(positionInRoot: Offset) {
        if (!isDragSelecting) return
        val uri = hitTestUri(positionInRoot) ?: return
        val endIndex = currentUiState.availableScreenshots
            .indexOfFirst { screenshot -> screenshot.uri == uri }
        if (endIndex < 0 || endIndex == dragSelectEndIndex) return
        applyDragSelectRange(endIndex)
    }

    fun onDragSelectEnd() {
        isDragSelecting = false
        dragSelectStartIndex = -1
        dragSelectEndIndex = -1
        dragSelectBaseline.clear()
        dragSelectLocalSelected.clear()
    }

    LaunchedEffect(uiState.showMaxSelectionReached) {
        if (!uiState.showMaxSelectionReached) return@LaunchedEffect
        currentToast = RecapToastPresentation(
            message = maxSelectionMessage,
            type = RecapToastType.Error,
        )
        delay(RecapToastDuration.Short.millis.milliseconds)
        currentToast = null
        delay(RecapToastExitAnimationDurationMillis.toLong().milliseconds)
        onAction(OrganizeAction.DismissMaxSelectionMessage)
    }

    LaunchedEffect(uiState.canProceed) {
        val appearSpec = tween<Float>(
            durationMillis = ScreenshotPickerTokens.ConfirmButtonAnimationDurationMillis,
            easing = FastOutSlowInEasing,
        )
        if (uiState.canProceed) {
            isConfirmButtonPresent = true
            confirmAppearProgress.animateTo(1f, animationSpec = appearSpec)
        } else if (isConfirmButtonPresent) {
            confirmAppearProgress.animateTo(0f, animationSpec = appearSpec)
            isConfirmButtonPresent = false
        }
    }

    LaunchedEffect(sheetState) {
        val state = sheetState ?: return@LaunchedEffect
        var hideStartOffset: Float? = null
        snapshotFlow {
            state.targetValue to runCatching { state.requireOffset() }.getOrNull()
        }.collect { (target, offset) ->
            if (offset == null || offset.isNaN()) return@collect
            if (target == SheetValue.Hidden) {
                if (hideStartOffset == null) {
                    hideStartOffset = offset
                }
                ctaHideSinkY = (offset - hideStartOffset!!).coerceAtLeast(0f)
            } else {
                hideStartOffset = null
                ctaHideSinkY = 0f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = toastHazeState)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            ScreenshotPickerToolbar(
                selectionCount = uiState.selectionCount,
                onCloseClick = onCloseClick,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    uiState.availableScreenshots.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.organize_selection_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RecapGray300,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = ScreenshotPickerTokens.EmptyHorizontalPadding),
                        )
                    }

                    !showGalleryBody -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    else -> {
                        val gridBottomPadding = if (uiState.canProceed) {
                            ScreenshotPickerTokens.ConfirmButtonReservedHeight
                        } else {
                            0.dp
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(ScreenshotPickerTokens.GridColumns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = gridBottomPadding),
                            horizontalArrangement = Arrangement.spacedBy(
                                ScreenshotPickerTokens.GridSpacing,
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                ScreenshotPickerTokens.GridSpacing,
                            ),
                            userScrollEnabled = !isDragSelecting,
                        ) {
                            itemsIndexed(
                                items = uiState.availableScreenshots,
                                key = { _, screenshot -> screenshot.uri },
                            ) { index, screenshot ->
                                val imageModel = screenshot.toSheetImageModel()
                                ScreenshotPickerGridItem(
                                    imageModel = imageModel,
                                    itemIndex = index + 1,
                                    selectionOrder = uiState.selectionOrder(screenshot.uri),
                                    onClick = {
                                        onAction(OrganizeAction.ToggleSelection(screenshot.uri))
                                    },
                                    onImageLongClick = {
                                        onImageLongClick(imageModel)
                                    },
                                    onCheckLongPressStart = {
                                        onDragSelectStart(screenshot.uri)
                                    },
                                    onCheckLongPressDrag = { positionInRoot ->
                                        onDragSelectMove(positionInRoot)
                                    },
                                    onCheckLongPressEnd = ::onDragSelectEnd,
                                    trackItemBounds = trackItemBounds,
                                    onBoundsChanged = { bounds ->
                                        itemBoundsInRoot[screenshot.uri] = bounds
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Partial에서 anchor가 화면 밖이면 Popup이 window 하단으로 clamp된다.
        // 여백은 IntOffset이 아니라 content bottom padding으로 유지한다.
        if (isConfirmButtonPresent) {
            Popup(
                alignment = Alignment.BottomCenter,
                properties = PopupProperties(
                    focusable = false,
                    excludeFromSystemGesture = true,
                ),
            ) {
                ScreenshotPickerConfirmButton(
                    selectionCount = uiState.selectionCount,
                    onClick = {
                        if (uiState.canProceed) onConfirmClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScreenshotPickerTokens.ConfirmButtonHorizontalPadding)
                        .padding(bottom = ScreenshotPickerTokens.ConfirmButtonBottomPadding)
                        .graphicsLayer {
                            val progress = confirmAppearProgress.value
                            alpha = progress
                            translationY =
                                (1f - progress) * size.height + ctaHideSinkY
                        },
                )
            }
        }

        val toastBottomPadding = if (uiState.canProceed) {
            ScreenshotPickerTokens.ConfirmButtonBottomPadding +
                    RecapButtonSize.Large.height +
                    ScreenshotPickerTokens.ToastAboveConfirmButtonSpacing
        } else {
            ScreenshotPickerTokens.ToastBottomPadding
        }
        RecapToastHost(
            currentToast = currentToast,
            hazeState = toastHazeState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = ScreenshotPickerTokens.ToastHorizontalPadding)
                .padding(bottom = toastBottomPadding),
        )
    }
}

@Composable
private fun ScreenshotPickerToolbar(
    selectionCount: Int,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(ScreenshotPickerTokens.ToolbarHeight)
            .padding(horizontal = ScreenshotPickerTokens.ToolbarHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScreenshotPickerIconButton(
            iconResId = R.drawable.ic_close_24,
            contentDescription = stringResource(
                R.string.screenshot_picker_close_content_description,
            ),
            enabled = true,
            iconTint = RecapGray900,
            onClick = onCloseClick,
        )

        Text(
            text = stringResource(R.string.organize_screenshot_selection_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RecapGray900,
            modifier = Modifier.padding(start = ScreenshotPickerTokens.TitleStartPadding),
        )

        Spacer(modifier = Modifier.width(ScreenshotPickerTokens.CountStartPadding))

        Text(
            text = stringResource(
                R.string.organize_selection_count,
                selectionCount,
                MAX_SELECTION_COUNT,
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = RecapBlue300,
        )
    }
}

@Composable
private fun ScreenshotPickerConfirmButton(
    selectionCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RecapButton(
        text = stringResource(
            R.string.organize_selection_complete_button,
            selectionCount,
        ),
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ScreenshotPickerIconButton(
    iconResId: Int,
    contentDescription: String,
    enabled: Boolean,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(ScreenshotPickerTokens.ControlMinSize)
            .semantics {
                if (!enabled) {
                    disabled()
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(ScreenshotPickerTokens.IconSize),
            tint = iconTint,
        )
    }
}

@Composable
private fun ScreenshotPickerGridItem(
    imageModel: Any,
    itemIndex: Int,
    selectionOrder: Int?,
    onClick: () -> Unit,
    onImageLongClick: () -> Unit,
    onCheckLongPressStart: () -> Unit,
    onCheckLongPressDrag: (Offset) -> Unit,
    onCheckLongPressEnd: () -> Unit,
    onBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier,
    trackItemBounds: Boolean = true,
) {
    val view = LocalView.current
    val currentView by rememberUpdatedState(view)
    val interactionSource = remember { MutableInteractionSource() }
    val checkInteractionSource = remember { MutableInteractionSource() }
    val isSelected = selectionOrder != null
    val selectionAnimationSpec = tween<Float>(
        durationMillis = ScreenshotPickerTokens.SelectionAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val selectionCornerAnimationSpec = tween<Dp>(
        durationMillis = ScreenshotPickerTokens.SelectionAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val selectionColorAnimationSpec = tween<Color>(
        durationMillis = ScreenshotPickerTokens.SelectionAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) {
            ScreenshotPickerTokens.SelectedScale
        } else {
            1f
        },
        animationSpec = selectionAnimationSpec,
        label = "screenshot_picker_item_selection_scale",
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) {
            ScreenshotPickerTokens.SelectedCornerRadius
        } else {
            2.dp
        },
        animationSpec = selectionCornerAnimationSpec,
        label = "screenshot_picker_item_selection_corner",
    )
    val checkIconTint by animateColorAsState(
        targetValue = if (isSelected) RecapBlue300 else RecapGray200,
        animationSpec = selectionColorAnimationSpec,
        label = "screenshot_picker_item_selection_color",
    )
    val contentDescription = if (selectionOrder != null) {
        stringResource(
            R.string.organize_selected_screenshot_item_content_description,
            selectionOrder,
        )
    } else {
        stringResource(
            R.string.organize_screenshot_item_content_description,
            itemIndex,
        )
    }
    val currentOnCheckLongPressStart by rememberUpdatedState(onCheckLongPressStart)
    val currentOnCheckLongPressDrag by rememberUpdatedState(onCheckLongPressDrag)
    val currentOnCheckLongPressEnd by rememberUpdatedState(onCheckLongPressEnd)
    var checkLayoutCoordinates by remember {
        mutableStateOf<LayoutCoordinates?>(null)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (trackItemBounds) {
                    Modifier.onGloballyPositioned { coordinates ->
                        onBoundsChanged(coordinates.boundsInRoot())
                    }
                } else {
                    Modifier
                },
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        AsyncImage(
            model = imageModel,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = {
                        view.performSoftLongPressHaptic()
                        onImageLongClick()
                    },
                    // 기본 LongPress 햅틱 대신 약한 피드백을 직접 재생한다.
                    hapticFeedbackEnabled = false,
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(ScreenshotPickerTokens.CheckHitSize)
                .then(
                    if (trackItemBounds) {
                        Modifier.onGloballyPositioned { coordinates ->
                            checkLayoutCoordinates = coordinates
                        }
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    interactionSource = checkInteractionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            currentView.performSoftLongPressHaptic()
                            currentOnCheckLongPressStart()
                        },
                        onDrag = { change, _ ->
                            val coordinates = checkLayoutCoordinates
                            if (coordinates != null && coordinates.isAttached) {
                                currentOnCheckLongPressDrag(
                                    coordinates.localToRoot(change.position),
                                )
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            currentOnCheckLongPressEnd()
                        },
                        onDragCancel = {
                            currentOnCheckLongPressEnd()
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle_24),
                contentDescription = null,
                modifier = Modifier.size(ScreenshotPickerTokens.CheckIconSize),
                tint = checkIconTint,
            )
        }
    }
}

/**
 * 길게 누르기용 약한 햅틱.
 * API 34+는 SEGMENT_TICK, 그 미만은 CLOCK_TICK으로 폴백한다.
 */
private fun View.performSoftLongPressHaptic() {
    val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        HapticFeedbackConstants.SEGMENT_TICK
    } else {
        HapticFeedbackConstants.CLOCK_TICK
    }
    performHapticFeedback(feedbackConstant)
}

private fun LocalImage.toSheetImageModel(): Any {
    val drawableResId = uri.toPreviewDrawableResIdOrNull()
    return drawableResId ?: uri.toUri()
}

private fun String.toPreviewDrawableResIdOrNull(): Int? {
    if (!startsWith(PreviewDrawableUriPrefix)) return null
    return removePrefix(PreviewDrawableUriPrefix)
        .substringBefore('/')
        .toIntOrNull()
}

private fun previewLocalImage(
    @DrawableRes drawableResId: Int,
    displayName: String,
    dateAddedMillis: Long,
): LocalImage = LocalImage(
    uri = "$PreviewDrawableUriPrefix$drawableResId/$displayName",
    displayName = displayName,
    dateAddedMillis = dateAddedMillis,
)

private const val PreviewDrawableUriPrefix = "drawable://"

val ScreenshotPickerPreviewScreenshots = listOf(
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_return,
        displayName = "screenshot-return",
        dateAddedMillis = 6L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_hotel,
        displayName = "screenshot-hotel",
        dateAddedMillis = 5L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_recipe,
        displayName = "screenshot-recipe",
        dateAddedMillis = 4L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_tax,
        displayName = "screenshot-tax",
        dateAddedMillis = 3L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_restaurant,
        displayName = "screenshot-restaurant",
        dateAddedMillis = 2L,
    ),
    previewLocalImage(
        drawableResId = R.drawable.mock_home_screenshot_return,
        displayName = "screenshot-return-2",
        dateAddedMillis = 1L,
    ),
)

private object ScreenshotPickerTokens {
    /** Expanded 상태 시트 높이. PartiallyExpanded(~50%)는 Material3 ModalBottomSheet가 처리한다. */
    const val ExpandedSheetHeightFraction = 0.88f
    const val GridColumns = 3
    const val SelectionAnimationDurationMillis = 100
    const val ConfirmButtonAnimationDurationMillis = 200

    val SheetTopCornerRadius = 40.dp
    val ToolbarHeight = 64.dp
    val ToolbarHorizontalPadding = 16.dp
    val TitleStartPadding = 4.dp
    val CountStartPadding = 8.dp
    val ControlMinSize = 48.dp
    val IconSize = 24.dp
    val GridSpacing = 2.dp
    val EmptyHorizontalPadding = 24.dp
    val CheckIconSize = 24.dp
    val CheckHitSize = 40.dp
    const val SelectedScale = 0.95f
    val SelectedCornerRadius = 12.dp
    val ConfirmButtonHorizontalPadding = 24.dp
    val ConfirmButtonBottomPadding = 33.dp
    val ConfirmButtonReservedHeight = 88.dp
    val ToastHorizontalPadding = 24.dp
    val ToastBottomPadding = 16.dp
    val ToastAboveConfirmButtonSpacing = 15.dp
}

@Preview(
    name = "Screenshot Picker - Populated",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(
                isLoading = false,
                availableScreenshots = ScreenshotPickerPreviewScreenshots,
                selectedUris = listOf(
                    ScreenshotPickerPreviewScreenshots[0].uri,
                    ScreenshotPickerPreviewScreenshots[1].uri,
                    ScreenshotPickerPreviewScreenshots[2].uri,
                ),
            ),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}

@Preview(
    name = "Screenshot Picker - Empty",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(isLoading = false),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}

@Preview(
    name = "Screenshot Picker - Loading",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerLoadingPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotPickerContent(
            uiState = OrganizeUiState(isLoading = true),
            onAction = {},
            onCloseClick = {},
            onConfirmClick = {},
        )
    }
}

@Preview(
    name = "Screenshot Picker - Discard Selection Confirm",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun ScreenshotPickerDiscardSelectionConfirmPreview() {
    RECAPTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RecapGray900.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            RecapPopupContent(
                title = stringResource(R.string.organize_discard_selection_confirm_title),
                description = stringResource(
                    R.string.organize_discard_selection_confirm_description,
                ),
                confirmButtonText = stringResource(
                    R.string.organize_discard_selection_confirm_quit,
                ),
                cancelButtonText = stringResource(
                    R.string.organize_discard_selection_confirm_keep_organizing,
                ),
                onConfirmClick = {},
                onCancelClick = {},
                confirmButtonColor = RecapError,
            )
        }
    }
}
