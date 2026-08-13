package com.chalkak.recap.app

import android.content.Context
import android.content.res.Configuration

/** App-wide upper bound for system font scale (accessibility override). */
internal const val RECAP_MAX_FONT_SCALE = 1.5f

internal fun Context.withCappedFontScale(
    maxFontScale: Float = RECAP_MAX_FONT_SCALE,
): Context {
    val currentScale = resources.configuration.fontScale
    if (currentScale <= maxFontScale) return this
    val capped = Configuration().apply {
        fontScale = maxFontScale
    }
    return createConfigurationContext(capped)
}

internal fun Configuration.capFontScale(
    maxFontScale: Float = RECAP_MAX_FONT_SCALE,
) {
    if (fontScale > maxFontScale) {
        fontScale = maxFontScale
    }
}
