package com.chalkak.recap.core.design.component.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-scoped toast enqueue API. Feature routes obtain this via [LocalRecapToastDispatcher].
 * Calls return immediately; presentation lifecycle is owned outside the caller's coroutine.
 */
interface RecapToastDispatcher {
    fun showToast(
        message: String,
        type: RecapToastType = RecapToastType.Error,
        duration: RecapToastDuration = RecapToastDuration.Short,
    )
}

val LocalRecapToastDispatcher = staticCompositionLocalOf<RecapToastDispatcher> {
    error("No RecapToastDispatcher provided. Wrap with CompositionLocalProvider or supply a Preview dispatcher.")
}

@Composable
fun ProvideRecapToastDispatcher(
    dispatcher: RecapToastDispatcher,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalRecapToastDispatcher provides dispatcher, content = content)
}
