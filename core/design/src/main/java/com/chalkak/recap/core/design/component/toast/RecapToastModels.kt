package com.chalkak.recap.core.design.component.toast

import androidx.compose.runtime.Immutable

@Immutable
data class RecapToastPresentation(
    val message: String,
    val type: RecapToastType,
)

@Immutable
data class RecapToastRequest(
    val message: String,
    val type: RecapToastType,
    val durationMillis: Long,
)

/** Matches [RecapToast] enter/exit animation duration so FIFO waits for exit to finish. */
const val RecapToastExitAnimationDurationMillis: Int = 200
