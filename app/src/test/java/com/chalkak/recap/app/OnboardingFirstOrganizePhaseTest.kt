package com.chalkak.recap.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OnboardingFirstOrganizePhaseTest {
    @Test
    fun `open picker moves idle to organize overlay`() {
        assertEquals(
            OnboardingFirstOrganizePhase.OrganizeOverlay,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.Idle,
                event = OnboardingFirstOrganizeEvent.OpenPicker,
            ),
        )
    }

    @Test
    fun `dismiss organize returns to idle`() {
        assertEquals(
            OnboardingFirstOrganizePhase.Idle,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.OrganizeOverlay,
                event = OnboardingFirstOrganizeEvent.DismissOrganize,
            ),
        )
    }

    @Test
    fun `start analysis moves organize overlay to analysis session`() {
        assertEquals(
            OnboardingFirstOrganizePhase.AnalysisSession,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.OrganizeOverlay,
                event = OnboardingFirstOrganizeEvent.StartAnalysis,
            ),
        )
    }

    @Test
    fun `cancel analysis returns to idle`() {
        assertEquals(
            OnboardingFirstOrganizePhase.Idle,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.AnalysisSession,
                event = OnboardingFirstOrganizeEvent.CancelAnalysis,
            ),
        )
    }

    @Test
    fun `dismiss terminal result returns to idle`() {
        assertEquals(
            OnboardingFirstOrganizePhase.Idle,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.AnalysisSession,
                event = OnboardingFirstOrganizeEvent.DismissTerminalResult,
            ),
        )
    }

    @Test
    fun `open picker is ignored outside idle`() {
        assertEquals(
            OnboardingFirstOrganizePhase.AnalysisSession,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.AnalysisSession,
                event = OnboardingFirstOrganizeEvent.OpenPicker,
            ),
        )
    }

    @Test
    fun `restored analysis without a session allows selecting screenshots again`() {
        val recovered = reduceOnboardingFirstOrganize(
            phase = OnboardingFirstOrganizePhase.AnalysisSession,
            event = OnboardingFirstOrganizeEvent.AnalysisSessionUnavailable,
        )

        assertEquals(OnboardingFirstOrganizePhase.Idle, recovered)
        assertEquals(
            OnboardingFirstOrganizePhase.OrganizeOverlay,
            reduceOnboardingFirstOrganize(recovered, OnboardingFirstOrganizeEvent.OpenPicker),
        )
    }

    @Test
    fun `unavailable analysis does not dismiss an open picker`() {
        assertEquals(
            OnboardingFirstOrganizePhase.OrganizeOverlay,
            reduceOnboardingFirstOrganize(
                phase = OnboardingFirstOrganizePhase.OrganizeOverlay,
                event = OnboardingFirstOrganizeEvent.AnalysisSessionUnavailable,
            ),
        )
    }
}
