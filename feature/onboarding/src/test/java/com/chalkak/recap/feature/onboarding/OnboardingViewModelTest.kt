package com.chalkak.recap.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import com.chalkak.recap.core.data.ocr.ImagePermissionRepository
import com.chalkak.recap.core.model.ImageAccessLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingViewModelTest {
    @Test
    fun onAction_savesMovedStepToSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = OnboardingViewModel(
            savedStateHandle = savedStateHandle,
            imagePermissionRepository = FakeImagePermissionRepository(),
        )

        viewModel.onAction(OnboardingAction.SkipFirstOrganize)

        assertEquals(OnboardingStep.StartFirstAnalyze, viewModel.uiState.value.step)
        assertEquals(
            OnboardingStep.StartFirstAnalyze.name,
            savedStateHandle.get<String>(ONBOARDING_STEP_SAVED_STATE_KEY),
        )
    }

    @Test
    fun onAction_savesBackStepToSavedStateHandle() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = OnboardingViewModel(
            savedStateHandle = savedStateHandle,
            imagePermissionRepository = FakeImagePermissionRepository(),
        )

        viewModel.onAction(OnboardingAction.SkipFirstOrganize)
        viewModel.onAction(OnboardingAction.Back)

        assertEquals(OnboardingStep.AddToFavorite, viewModel.uiState.value.step)
        assertEquals(
            OnboardingStep.AddToFavorite.name,
            savedStateHandle.get<String>(ONBOARDING_STEP_SAVED_STATE_KEY),
        )
    }

    @Test
    fun createdViewModel_restoresSavedStepAndReadsCurrentPermissionLevel() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                ONBOARDING_STEP_SAVED_STATE_KEY to OnboardingStep.AddToFavorite.name,
            ),
        )
        val viewModel = OnboardingViewModel(
            savedStateHandle = savedStateHandle,
            imagePermissionRepository = FakeImagePermissionRepository(
                imageAccessLevel = ImageAccessLevel.Full,
            ),
        )

        assertEquals(OnboardingStep.AddToFavorite, viewModel.uiState.value.step)
        assertEquals(ImageAccessLevel.Full, viewModel.uiState.value.imageAccessLevel)
    }
}

private class FakeImagePermissionRepository(
    private val imageAccessLevel: ImageAccessLevel = ImageAccessLevel.Denied,
) : ImagePermissionRepository {
    override fun imagePermissionRequest(): Array<String> = emptyArray()

    override fun currentImageAccessLevel(): ImageAccessLevel = imageAccessLevel
}
