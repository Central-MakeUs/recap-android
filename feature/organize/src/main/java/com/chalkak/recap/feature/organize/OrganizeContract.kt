package com.chalkak.recap.feature.organize

import com.chalkak.recap.core.model.LocalImage

data class OrganizeUiState(
    val isLoading: Boolean = true,
    val availableScreenshots: List<LocalImage> = emptyList(),
    val selectedUris: List<String> = emptyList(),
    val showMaxSelectionReached: Boolean = false,
) {
    val selectionCount: Int
        get() = selectedUris.size

    val canProceed: Boolean
        get() = selectionCount in MIN_SELECTION_COUNT..MAX_SELECTION_COUNT

    fun selectionOrder(uri: String): Int? {
        val index = selectedUris.indexOf(uri)
        return if (index >= 0) index + 1 else null
    }
}

sealed interface OrganizeAction {
    data class ToggleSelection(val uri: String) : OrganizeAction
    data class RemoveSelection(val uri: String) : OrganizeAction
    data object DismissMaxSelectionMessage : OrganizeAction
}

internal const val MIN_SELECTION_COUNT = 1
internal const val MAX_SELECTION_COUNT = 20
