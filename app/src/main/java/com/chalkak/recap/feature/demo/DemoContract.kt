package com.chalkak.recap.feature.demo

data class DemoUiState(
    val title: String = "Demo",
    val description: String = "Technical MVP experiments will appear here.",
)

sealed interface DemoAction
