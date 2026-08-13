package com.chalkak.recap

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chalkak.recap.app.RecapToastViewModel
import com.chalkak.recap.app.share.ShareIntakeEvent
import com.chalkak.recap.app.share.ShareIntakeViewModel
import com.chalkak.recap.app.share.ShareReceiverRoute
import com.chalkak.recap.app.share.OnboardingSampleShareIntentContract
import com.chalkak.recap.app.share.SharedAnalysisIntentContract
import com.chalkak.recap.core.design.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareReceiverActivity : RecapComponentActivity() {
    private val shareIntakeViewModel: ShareIntakeViewModel by viewModels()
    private val toastViewModel: RecapToastViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        shareIntakeViewModel.submitShareIntent(intent)
        observeShareIntakeEvents()
        setContent {
            ShareReceiverRoute(
                shareIntakeViewModel = shareIntakeViewModel,
                toastViewModel = toastViewModel,
                onFinish = ::finish,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shareIntakeViewModel.submitShareIntent(
            intent = intent,
            forceNewSession = true,
        )
    }

    private fun observeShareIntakeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                shareIntakeViewModel.events.collect { event ->
                    when (event) {
                        ShareIntakeEvent.LoginRequired -> {
                            Toast.makeText(
                                this@ShareReceiverActivity,
                                getString(R.string.share_login_required),
                                Toast.LENGTH_SHORT,
                            ).show()
                            startMainActivityAndFinish()
                        }

                        ShareIntakeEvent.OnboardingRequired -> {
                            Toast.makeText(
                                this@ShareReceiverActivity,
                                getString(R.string.share_onboarding_required),
                                Toast.LENGTH_SHORT,
                            ).show()
                            startMainActivityAndFinish()
                        }

                        ShareIntakeEvent.ReturnAfterOnboardingSampleShare -> {
                            startActivity(
                                OnboardingSampleShareIntentContract.createIntent(
                                    this@ShareReceiverActivity,
                                ),
                            )
                            finish()
                        }

                        is ShareIntakeEvent.LaunchMainAnalysis -> {
                            startActivity(
                                SharedAnalysisIntentContract.createIntent(
                                    context = this@ShareReceiverActivity,
                                    requestId = event.requestId,
                                    images = event.images,
                                ),
                            )
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun startMainActivityAndFinish() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
        )
        finish()
    }
}
