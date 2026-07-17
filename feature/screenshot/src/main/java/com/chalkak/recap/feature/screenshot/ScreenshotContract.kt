package com.chalkak.recap.feature.screenshot

import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType

data class ScreenshotEditDraft(
    val title: String = "",
    val summary: String = "",
    val body: String = "",
    val contentType: ScreenshotContentType = ScreenshotContentType.OTHER,
)

sealed interface ScreenshotUiState {
    data object Loading : ScreenshotUiState

    data class Content(
        val card: StoredScreenshotCard,
        val editDraft: ScreenshotEditDraft,
        val isFavoriteUpdating: Boolean = false,
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val showDeleteConfirmDialog: Boolean = false,
        val showDiscardEditConfirmDialog: Boolean = false,
        val titleError: Boolean = false,
        val actionErrorMessageResId: Int? = null,
    ) : ScreenshotUiState

    data class NotFound(
        val actionErrorMessageResId: Int? = null,
    ) : ScreenshotUiState

    data class LoadError(
        val actionErrorMessageResId: Int? = null,
    ) : ScreenshotUiState
}

sealed interface ScreenshotAction {
    data object RetryLoad : ScreenshotAction
    data object ToggleFavorite : ScreenshotAction
    data object PrepareEditDraft : ScreenshotAction
    data object ClearActionError : ScreenshotAction
    data class UpdateEditTitle(val title: String) : ScreenshotAction
    data class UpdateEditSummary(val summary: String) : ScreenshotAction
    data class UpdateEditBody(val body: String) : ScreenshotAction
    data class UpdateEditContentType(val contentType: ScreenshotContentType) : ScreenshotAction
    data object DiscardEditDraft : ScreenshotAction
    data object SaveEdit : ScreenshotAction
    data object ShowDiscardEditConfirmDialog : ScreenshotAction
    data object DismissDiscardEditConfirmDialog : ScreenshotAction
    data object ShowDeleteConfirmDialog : ScreenshotAction
    data object DismissDeleteConfirmDialog : ScreenshotAction
    data object DeleteScreenshot : ScreenshotAction
}

sealed interface ScreenshotEvent {
    data object SaveSucceeded : ScreenshotEvent
    data object DeleteSucceeded : ScreenshotEvent
    data class ShowFavoriteToast(val isFavorite: Boolean) : ScreenshotEvent
}

internal object ScreenshotLimits {
    const val TitleMaxLength = 30
    const val SummaryMaxLength = 80
}

internal fun StoredScreenshotCard.toEditDraft(): ScreenshotEditDraft {
    return ScreenshotEditDraft(
        title = analysisResult.title,
        summary = analysisResult.summary,
        body = analysisResult.body,
        contentType = analysisResult.contentTypes.primaryContentType,
    )
}

internal fun ScreenshotEditDraft.normalizedForSave(): ScreenshotEditDraft {
    return copy(
        title = title.trim(),
        summary = summary.trim(),
        body = body.trim(),
    )
}

internal fun sanitizeEditTitleInput(raw: String): String =
    raw.replace("\r\n", "").replace("\n", "").replace("\r", "")

internal fun sanitizeEditSummaryInput(raw: String): String =
    raw.replace("\r\n", " ").replace("\n", " ").replace("\r", " ")

internal fun ScreenshotEditDraft.isTitleValid(): Boolean = title.trim().isNotEmpty()

internal fun ScreenshotUiState.Content.hasUnsavedEditChanges(): Boolean {
    return editDraft != card.toEditDraft()
}
