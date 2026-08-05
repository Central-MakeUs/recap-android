package com.chalkak.recap.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.LocalRecapToastDispatcher
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.feature.onboarding.component.OnboardingLayoutDefaults
import com.chalkak.recap.feature.onboarding.screen.OnboardingLandingScreen

/**
 * 세션 만료 후 재로그인 화면.
 *
 * 온보딩 Landing을 그대로 재사용하되 로그인만 처리하고, 권한 가이드나 튜토리얼 단계로 이어지지 않는다.
 */
@Composable
fun ReauthRoute(
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReauthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val toastDispatcher = LocalRecapToastDispatcher.current
    val loginFailedMessage = stringResource(R.string.onboarding_login_failed_message)
    val loginCancelledMessage = stringResource(R.string.onboarding_login_cancelled_message)

    // 재로그인 전에는 돌아갈 화면이 없으므로 back은 앱을 종료한다.
    BackHandler { onExitApp() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ReauthEvent.ShowLoginError -> {
                    toastDispatcher.showToast(
                        message = if (event.isCancelled) {
                            loginCancelledMessage
                        } else {
                            loginFailedMessage
                        },
                        type = RecapToastType.Error,
                    )
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            OnboardingLandingScreen(
                onAction = { action ->
                    if (action == OnboardingAction.LoginWithKakao) {
                        viewModel.loginWithKakao(context)
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(OnboardingLayoutDefaults.LandingScreenPadding),
                isLoading = uiState.isLoading,
            )
        }
    }
}
