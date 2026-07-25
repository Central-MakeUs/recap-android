package com.chalkak.recap.app.share

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.app.RecapToastViewModel
import com.chalkak.recap.app.resolveEffectiveToastDurationMillis
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.ProvideRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.RecapToastHost
import com.chalkak.recap.core.design.component.toast.RecapToastRequest
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.feature.organize.MAX_SELECTION_COUNT
import com.chalkak.recap.feature.organize.OrganizeRoute
import com.chalkak.recap.feature.organize.UnsupportedShareScreen
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun ShareReceiverRoute(
    shareIntakeViewModel: ShareIntakeViewModel,
    toastViewModel: RecapToastViewModel,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendingShareIntake by shareIntakeViewModel.pendingShareIntake.collectAsStateWithLifecycle()
    val isLoading by shareIntakeViewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastDispatcher = remember(toastViewModel, context) {
        object : RecapToastDispatcher {
            override fun showToast(
                message: String,
                type: RecapToastType,
                duration: RecapToastDuration,
            ) {
                toastViewModel.enqueue(
                    RecapToastRequest(
                        message = message,
                        type = type,
                        durationMillis = resolveEffectiveToastDurationMillis(context, duration),
                    ),
                )
            }
        }
    }
    val toastHazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    val currentToast by toastViewModel.currentToast.collectAsStateWithLifecycle()
    val toastBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding() + 8.dp

    RECAPTheme {
        ProvideRecapToastDispatcher(dispatcher = toastDispatcher) {
            Box(
                modifier = modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = toastHazeState),
                ) {
                    ShareReceiverContent(
                        pendingShareIntake = pendingShareIntake,
                        isLoading = isLoading,
                        onFinish = { sessionId ->
                            sessionId?.let(shareIntakeViewModel::completePendingShareIntake)
                            onFinish()
                        },
                        onOrganizeComplete = shareIntakeViewModel::requestStartOrganize,
                    )
                }

                RecapToastHost(
                    currentToast = currentToast,
                    hazeState = toastHazeState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = toastBottomPadding),
                )
            }
        }
    }
}

@Composable
internal fun ShareReceiverContent(
    pendingShareIntake: PendingShareIntake?,
    isLoading: Boolean,
    onFinish: (sessionId: String?) -> Unit,
    onOrganizeComplete: (List<LocalImage>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val toastDispatcher = LocalRecapToastDispatcher.current
    var presentedShareSessionId by rememberSaveable { mutableStateOf<String?>(null) }

    val shareConfirmation = pendingShareIntake as? PendingShareIntake.Confirmation
    val isFirstSharePresentation = shareConfirmation != null &&
            presentedShareSessionId != shareConfirmation.sessionId
    val shareNonImageRemovedToastMessage =
        if (isFirstSharePresentation && shareConfirmation.rejectedCount > 0) {
            stringResource(
                R.string.share_non_image_removed,
                shareConfirmation.rejectedCount,
            )
        } else {
            null
        }
    val shareMaxSelectionToastMessage =
        if (isFirstSharePresentation && shareConfirmation.trimmedByMax) {
            stringResource(
                R.string.share_max_selection_message,
                MAX_SELECTION_COUNT,
            )
        } else {
            null
        }

    LaunchedEffect(pendingShareIntake) {
        when (val pending = pendingShareIntake) {
            is PendingShareIntake.Confirmation -> {
                shareNonImageRemovedToastMessage?.let { message ->
                    toastDispatcher.showToast(
                        message = message,
                        type = RecapToastType.Error,
                    )
                }
                shareMaxSelectionToastMessage?.let { message ->
                    toastDispatcher.showToast(
                        message = message,
                        type = RecapToastType.Error,
                    )
                }
                presentedShareSessionId = pending.sessionId
            }

            is PendingShareIntake.Unsupported -> {
                presentedShareSessionId = pending.sessionId
            }

            null -> Unit
        }
    }

    LaunchedEffect(isLoading, pendingShareIntake) {
        if (!isLoading && pendingShareIntake == null) {
            onFinish(null)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = RecapBlue500)
                }
            }

            pendingShareIntake is PendingShareIntake.Confirmation -> {
                key(pendingShareIntake.sessionId) {
                    OrganizeRoute(
                        sharedImages = pendingShareIntake.images,
                        shareSessionId = pendingShareIntake.sessionId,
                        clearSelectionOnComplete = false,
                        onNavigateBack = {
                            onFinish(pendingShareIntake.sessionId)
                        },
                        onOrganizeComplete = onOrganizeComplete,
                    )
                }
            }

            pendingShareIntake is PendingShareIntake.Unsupported -> {
                UnsupportedShareScreen(
                    onCloseClick = {
                        onFinish(pendingShareIntake.sessionId)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareReceiverLoadingPreview() {
    RECAPTheme {
        ProvideRecapToastDispatcher(
            dispatcher = object : RecapToastDispatcher {
                override fun showToast(
                    message: String,
                    type: RecapToastType,
                    duration: RecapToastDuration,
                ) = Unit
            },
        ) {
            ShareReceiverContent(
                pendingShareIntake = null,
                isLoading = true,
                onFinish = {},
                onOrganizeComplete = {},
            )
        }
    }
}
