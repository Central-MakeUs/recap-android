package com.chalkak.recap

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeSharedAnalysisIntent(intent)
    }

    private fun consumeSharedAnalysisIntent(intent: Intent) {
        val images = entryViewModel.consumeSharedAnalysisIntent(intent) ?: return
        analysisProgressViewModel.startAnalysis(images)
    }
}
