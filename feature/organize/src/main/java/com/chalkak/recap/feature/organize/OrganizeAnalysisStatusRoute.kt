package com.chalkak.recap.feature.organize

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.feature.organize.content.OrganizeFailedContent
import com.chalkak.recap.feature.organize.content.OrganizePartialFailedContent
import com.chalkak.recap.feature.organize.content.OrganizeProgressContent
import com.chalkak.recap.feature.organize.content.OrganizeSuccessBackgroundGradient
import com.chalkak.recap.feature.organize.content.OrganizeSuccessContent

@Composable
fun OrganizeAnalysisStatusRoute(
    uiState: OrganizeAnalysisStatusUiState,
    onCancelClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificationsEnabled: Boolean? = null,
    organizeCompleteNotificationEnabled: Boolean? = null,
    onOrganizeCompleteNotificationEnabledChange: (Boolean) -> Unit = {},
    onTryMarkOrganizeNotificationPermissionPromptShown: suspend () -> Boolean = { false },
) {
    when (uiState) {
        OrganizeAnalysisStatusUiState.Hidden -> Unit

        else -> {
            OrganizeAnalysisStatusScaffold(
                uiState = uiState,
                onCancelClick = onCancelClick,
                onDismissClick = onDismissClick,
                modifier = modifier,
                notificationsEnabled = notificationsEnabled,
            )
            OrganizeProgressNotificationPermissionEffect(
                organizeCompleteNotificationEnabled = organizeCompleteNotificationEnabled,
                onOrganizeCompleteNotificationEnabledChange =
                    onOrganizeCompleteNotificationEnabledChange,
                onTryMarkOrganizeNotificationPermissionPromptShown =
                    onTryMarkOrganizeNotificationPermissionPromptShown,
            )
        }
    }
}

@Composable
private fun OrganizeAnalysisStatusScaffold(
    uiState: OrganizeAnalysisStatusUiState,
    onCancelClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificationsEnabled: Boolean? = null,
) {
    val buttonModel = uiState.toButtonModel()
    val currentOnCancelClick by rememberUpdatedState(onCancelClick)
    val currentOnDismissClick by rememberUpdatedState(onDismissClick)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState is OrganizeAnalysisStatusUiState.Success) {
                OrganizeSuccessBackgroundGradient(
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = OrganizeAnalysisStatusTokens.HorizontalPadding),
            ) {
                AnimatedContent(
                    targetState = uiState,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = OrganizeAnalysisStatusTokens.FadeMs,
                                delayMillis = OrganizeAnalysisStatusTokens.FadeMs,
                            ),
                        ) togetherWith fadeOut(
                            animationSpec = tween(
                                durationMillis = OrganizeAnalysisStatusTokens.FadeMs,
                            ),
                        )
                    },
                    contentKey = { state -> state.contentKey() },
                    label = "organize_analysis_status_content",
                ) { state ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (state) {
                            OrganizeAnalysisStatusUiState.Hidden -> Unit

                            is OrganizeAnalysisStatusUiState.Progress -> {
                                OrganizeProgressContent(
                                    progress = state.progress,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .offset(
                                            y = OrganizeAnalysisStatusTokens.ProgressContentOffsetY,
                                        ),
                                    notificationsEnabled = notificationsEnabled,
                                )
                            }

                            is OrganizeAnalysisStatusUiState.Success -> {
                                OrganizeSuccessContent(
                                    successCount = state.successCount,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            is OrganizeAnalysisStatusUiState.Failed -> {
                                OrganizeFailedContent(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .offset(
                                            y = OrganizeAnalysisStatusTokens.FailedContentOffsetY,
                                        ),
                                    usageLimitExceeded = state.usageLimitExceeded,
                                )
                            }

                            is OrganizeAnalysisStatusUiState.PartialFailed -> {
                                OrganizePartialFailedContent(
                                    successCount = state.successCount,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .offset(
                                            y = OrganizeAnalysisStatusTokens
                                                .PartialFailedContentOffsetY,
                                        ),
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = buttonModel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = OrganizeAnalysisStatusTokens.BottomPadding),
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = OrganizeAnalysisStatusTokens.FadeMs,
                            ),
                        ) togetherWith fadeOut(
                            animationSpec = tween(
                                durationMillis = OrganizeAnalysisStatusTokens.FadeMs,
                            ),
                        )
                    },
                    label = "organize_analysis_status_button",
                ) { model ->
                    RecapButton(
                        text = stringResource(model.textRes),
                        onClick = {
                            if (model.style == OrganizeStatusButtonStyle.Secondary) {
                                currentOnCancelClick()
                            } else {
                                currentOnDismissClick()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = when (model.style) {
                            OrganizeStatusButtonStyle.Secondary ->
                                RecapButtonDefaults.secondaryColors()
                            OrganizeStatusButtonStyle.Primary ->
                                RecapButtonDefaults.primaryColors()
                        },
                        contentPadding = PaddingValues(vertical = 15.dp),
                    )
                }
            }
        }
    }
}

@Immutable
private data class OrganizeStatusButtonModel(
    @StringRes val textRes: Int,
    val style: OrganizeStatusButtonStyle,
)

private enum class OrganizeStatusButtonStyle {
    Primary,
    Secondary,
}

private fun OrganizeAnalysisStatusUiState.contentKey(): String = when (this) {
    OrganizeAnalysisStatusUiState.Hidden -> "hidden"
    is OrganizeAnalysisStatusUiState.Progress -> "progress"
    is OrganizeAnalysisStatusUiState.Success -> "success"
    is OrganizeAnalysisStatusUiState.Failed -> "failed"
    is OrganizeAnalysisStatusUiState.PartialFailed -> "partial_failed"
}

private fun OrganizeAnalysisStatusUiState.toButtonModel(): OrganizeStatusButtonModel = when (this) {
    OrganizeAnalysisStatusUiState.Hidden -> OrganizeStatusButtonModel(
        textRes = R.string.organize_failed_close,
        style = OrganizeStatusButtonStyle.Primary,
    )

    is OrganizeAnalysisStatusUiState.Progress -> OrganizeStatusButtonModel(
        textRes = R.string.organize_progress_cancel,
        style = OrganizeStatusButtonStyle.Secondary,
    )

    is OrganizeAnalysisStatusUiState.Success -> OrganizeStatusButtonModel(
        textRes = R.string.organize_success_done,
        style = OrganizeStatusButtonStyle.Primary,
    )

    is OrganizeAnalysisStatusUiState.Failed -> OrganizeStatusButtonModel(
        textRes = R.string.organize_failed_close,
        style = OrganizeStatusButtonStyle.Primary,
    )

    is OrganizeAnalysisStatusUiState.PartialFailed -> OrganizeStatusButtonModel(
        textRes = R.string.organize_partial_failed_close,
        style = OrganizeStatusButtonStyle.Primary,
    )
}

private object OrganizeAnalysisStatusTokens {
    const val FadeMs = 280
    val HorizontalPadding = 24.dp
    val BottomPadding = 24.dp
    val ProgressContentOffsetY = (-20).dp
    val FailedContentOffsetY = (-84).dp
    val PartialFailedContentOffsetY = (-45).dp
}

@Preview(
    name = "Organize Analysis Status Progress",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeAnalysisStatusRouteProgressPreview() {
    RECAPTheme {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Progress(progress = 0.65f),
            onCancelClick = {},
            onDismissClick = {},
            notificationsEnabled = true,
        )
    }
}

@Preview(
    name = "Organize Analysis Status Success",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeAnalysisStatusRouteSuccessPreview() {
    RECAPTheme {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Success(successCount = 5),
            onCancelClick = {},
            onDismissClick = {},
        )
    }
}

@Preview(
    name = "Organize Analysis Status Failed",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeAnalysisStatusRouteFailedPreview() {
    RECAPTheme {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Failed(),
            onCancelClick = {},
            onDismissClick = {},
        )
    }
}

@Preview(
    name = "Organize Analysis Status Failed Usage Limit",
    showBackground = true,
    widthDp = 360,
    heightDp = 780,
)
@Composable
private fun OrganizeAnalysisStatusRouteFailedUsageLimitPreview() {
    RECAPTheme {
        OrganizeAnalysisStatusRoute(
            uiState = OrganizeAnalysisStatusUiState.Failed(usageLimitExceeded = true),
            onCancelClick = {},
            onDismissClick = {},
        )
    }
}
