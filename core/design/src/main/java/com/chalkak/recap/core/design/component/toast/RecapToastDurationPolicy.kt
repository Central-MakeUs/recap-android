package com.chalkak.recap.core.design.component.toast

import android.view.accessibility.AccessibilityManager

/**
 * Content flags for toast timeout recommendations: text + icon, no interactive controls.
 */
const val RecapToastAccessibilityContentFlags: Int =
    AccessibilityManager.FLAG_CONTENT_TEXT or AccessibilityManager.FLAG_CONTENT_ICONS

/**
 * Chooses the longer of the app's base toast duration and the system recommended timeout.
 */
fun effectiveToastDurationMillis(
    baseDurationMillis: Long,
    recommendedTimeoutMillis: Long,
): Long = maxOf(baseDurationMillis, recommendedTimeoutMillis)
