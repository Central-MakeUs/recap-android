package com.chalkak.recap.app

import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.chalkak.recap.core.design.component.toast.RecapToastAccessibilityContentFlags
import com.chalkak.recap.core.design.component.toast.RecapToastDuration
import com.chalkak.recap.core.design.component.toast.effectiveToastDurationMillis

internal fun resolveEffectiveToastDurationMillis(
    context: Context,
    duration: RecapToastDuration,
): Long {
    val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
        ?: return duration.millis
    val recommendedTimeoutMillis = accessibilityManager.getRecommendedTimeoutMillis(
        duration.millis.toInt().coerceAtLeast(0),
        RecapToastAccessibilityContentFlags,
    )
    return effectiveToastDurationMillis(
        baseDurationMillis = duration.millis,
        recommendedTimeoutMillis = recommendedTimeoutMillis.toLong(),
    )
}
