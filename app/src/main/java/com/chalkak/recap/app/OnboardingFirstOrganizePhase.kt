package com.chalkak.recap.app

enum class OnboardingFirstOrganizePhase {
    Idle,
    OrganizeOverlay,
    AnalysisSession,
}

sealed interface OnboardingFirstOrganizeEvent {
    data object OpenPicker : OnboardingFirstOrganizeEvent
    data object DismissOrganize : OnboardingFirstOrganizeEvent
    data object StartAnalysis : OnboardingFirstOrganizeEvent
    data object CancelAnalysis : OnboardingFirstOrganizeEvent
    data object DismissTerminalResult : OnboardingFirstOrganizeEvent
}

fun reduceOnboardingFirstOrganize(
    phase: OnboardingFirstOrganizePhase,
    event: OnboardingFirstOrganizeEvent,
): OnboardingFirstOrganizePhase = when (event) {
    OnboardingFirstOrganizeEvent.OpenPicker -> when (phase) {
        OnboardingFirstOrganizePhase.Idle -> OnboardingFirstOrganizePhase.OrganizeOverlay
        else -> phase
    }

    OnboardingFirstOrganizeEvent.DismissOrganize -> when (phase) {
        OnboardingFirstOrganizePhase.OrganizeOverlay -> OnboardingFirstOrganizePhase.Idle
        else -> phase
    }

    OnboardingFirstOrganizeEvent.StartAnalysis -> when (phase) {
        OnboardingFirstOrganizePhase.OrganizeOverlay ->
            OnboardingFirstOrganizePhase.AnalysisSession
        else -> phase
    }

    OnboardingFirstOrganizeEvent.CancelAnalysis,
    OnboardingFirstOrganizeEvent.DismissTerminalResult,
    -> when (phase) {
        OnboardingFirstOrganizePhase.AnalysisSession -> OnboardingFirstOrganizePhase.Idle
        else -> phase
    }
}
