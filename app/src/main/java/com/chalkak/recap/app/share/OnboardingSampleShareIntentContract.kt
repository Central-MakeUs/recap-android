package com.chalkak.recap.app.share

import android.content.Context
import android.content.Intent
import com.chalkak.recap.MainActivity

object OnboardingSampleShareIntentContract {
    const val ACTION = "com.chalkak.recap.action.ONBOARDING_SAMPLE_SHARE_SUCCESS"

    internal const val EXTRA_EVENT_ID = "onboarding_sample_share_event_id"

    fun createIntent(context: Context): Intent {
        return Intent(ACTION).apply {
            setClass(context, MainActivity::class.java)
            putExtra(EXTRA_EVENT_ID, OnboardingSampleShareSuccessStore.issueEventId())
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    fun eventId(intent: Intent?): String? {
        if (intent == null || intent.action != ACTION) {
            return null
        }
        return intent.getStringExtra(EXTRA_EVENT_ID)?.takeIf { id -> id.isNotBlank() }
    }
}
