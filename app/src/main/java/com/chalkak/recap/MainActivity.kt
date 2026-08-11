package com.chalkak.recap

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.chalkak.recap.app.MainActivityEntryViewModel
import com.chalkak.recap.app.RecapApp
import com.chalkak.recap.app.RecapStartupViewModel
import com.chalkak.recap.app.RecapToastViewModel
import com.chalkak.recap.app.ScreenshotAnalysisProgressViewModel
import com.chalkak.recap.app.resolveEffectiveToastDurationMillis
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.RecapToastRequest
import com.chalkak.recap.core.design.component.toast.RecapToastType
import com.chalkak.recap.core.model.observability.OrganizeTraceEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : RecapComponentActivity() {
    private val startupViewModel: RecapStartupViewModel by viewModels()
    private val toastViewModel: RecapToastViewModel by viewModels()
    private val analysisProgressViewModel: ScreenshotAnalysisProgressViewModel by viewModels()
    private val entryViewModel: MainActivityEntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge( // 라이트모드 강제
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        consumeSharedAnalysisIntent(intent)
        consumeOnboardingSampleShareIntent(intent)
        setContent {
            val pendingHomeNavigationRequestId by
                entryViewModel.pendingHomeNavigationRequestId.collectAsStateWithLifecycle()
            RecapApp(
                startupViewModel = startupViewModel,
                toastViewModel = toastViewModel,
                analysisProgressViewModel = analysisProgressViewModel,
                pendingHomeNavigationRequestId = pendingHomeNavigationRequestId,
                onRequestNavigateHome = entryViewModel::requestNavigateToHome,
                onHomeNavigationComplete = entryViewModel::completeHomeNavigation,
                pendingOnboardingSampleShareAdvanceRequestIds =
                    entryViewModel.pendingOnboardingSampleShareAdvanceRequestId,
                onOnboardingSampleShareAdvanceComplete =
                    entryViewModel::completeOnboardingSampleShareAdvance,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeSharedAnalysisIntent(intent)
        consumeOnboardingSampleShareIntent(intent)
    }

    private fun consumeSharedAnalysisIntent(intent: Intent) {
        val candidates = entryViewModel.consumeSharedAnalysisIntent(intent) ?: return
        analysisProgressViewModel.startAnalysis(
            candidates = candidates,
            entry = OrganizeTraceEntry.SHARE,
        )
    }

    private fun consumeOnboardingSampleShareIntent(intent: Intent) {
        if (!entryViewModel.consumeOnboardingSampleShareSuccess(intent)) {
            return
        }
        toastViewModel.enqueue(
            RecapToastRequest(
                message = getString(R.string.share_onboarding_sample_success),
                type = RecapToastType.Success,
                durationMillis = resolveEffectiveToastDurationMillis(
                    context = this,
                    duration = RecapToastDuration.Short,
                ),
            ),
        )
    }
}
