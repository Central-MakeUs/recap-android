package com.chalkak.recap.feature.organize

import com.chalkak.recap.core.model.LocalImage
import com.chalkak.recap.core.model.ScreenshotUploadCandidate

data class OrganizeUiState(
    val monthlyRemaining: Int? = null,
    val hasEnteredConfirmation: Boolean = false,
    val monthlyLimitNotice: Int = 0,
    val isLoading: Boolean = true,
    val availableScreenshots: List<LocalImage> = emptyList(),
    val selectedUris: List<String> = emptyList(),
    val showMaxSelectionReached: Boolean = false,
    val showAiDataTransferConsentSheet: Boolean = false,
    val isConsentSubmitting: Boolean = false,
) {
    val selectionCount: Int
        get() = selectedUris.size

    val canProceed: Boolean
        get() = selectionCount in MIN_SELECTION_COUNT..MAX_SELECTION_COUNT

    val selectionLimit: Int
        get() = if (hasEnteredConfirmation) {
            monthlyRemaining?.coerceIn(0, MAX_SELECTION_COUNT) ?: MAX_SELECTION_COUNT
        } else {
            MAX_SELECTION_COUNT
        }

    val canStartOrganizing: Boolean
        get() = canProceed && selectionCount <= selectionLimit

    fun selectionOrder(uri: String): Int? {
        val index = selectedUris.indexOf(uri)
        return if (index >= 0) index + 1 else null
    }
}

sealed interface OrganizeAction {
    data class ToggleSelection(val uri: String) : OrganizeAction
    data class RemoveSelection(val uri: String) : OrganizeAction
    data object ClearSelection : OrganizeAction
    data object DismissMaxSelectionMessage : OrganizeAction
    data object StartOrganizing : OrganizeAction
    data object AgreeAiDataTransferConsent : OrganizeAction
    data object DismissAiDataTransferConsent : OrganizeAction
}

sealed interface OrganizeEvent {
    data object MonthlyLimitExceeded : OrganizeEvent

    data class ProceedToOrganize(
        val candidates: List<ScreenshotUploadCandidate>,
    ) : OrganizeEvent
}

internal const val MIN_SELECTION_COUNT = 1
const val MAX_SELECTION_COUNT = 20
