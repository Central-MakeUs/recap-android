package com.chalkak.recap.app

fun canEnterRecapApp(
    lottieSplashComplete: Boolean,
    startupUiState: RecapStartupUiState,
): Boolean = lottieSplashComplete && startupUiState is RecapStartupUiState.Ready
