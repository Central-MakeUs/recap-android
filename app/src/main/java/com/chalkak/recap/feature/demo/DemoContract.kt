package com.chalkak.recap.feature.demo

import android.net.Uri
import androidx.annotation.StringRes
import com.chalkak.recap.R

data class DemoUiState(
    @StringRes val titleResId: Int = R.string.demo_title,
    @StringRes val descriptionResId: Int = R.string.demo_description,
    val imagePermissionLevel: ImagePermissionLevel = ImagePermissionLevel.Denied,
    val recentScreenshotUris: List<Uri> = emptyList(),
    val ocrState: OcrUiState = OcrUiState(),
)

sealed interface DemoAction {
    data object RequestImagePermission : DemoAction
    data object RefreshImagePermission : DemoAction
    data class RunOcr(val engine: OcrEngine) : DemoAction
}

data class OcrUiState(
    val engine: OcrEngine? = null,
    val isRunning: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val results: List<OcrImageResult> = emptyList(),
    val errorMessage: String? = null,
) {
    val progress: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
}

enum class OcrEngine(
    @StringRes val buttonLabelResId: Int,
    @StringRes val resultLabelResId: Int,
) {
    Latin(
        buttonLabelResId = R.string.demo_ocr_engine_latin,
        resultLabelResId = R.string.demo_ocr_engine_latin,
    ),
    Korean(
        buttonLabelResId = R.string.demo_ocr_engine_korean,
        resultLabelResId = R.string.demo_ocr_engine_korean,
    ),
}

data class OcrImageResult(
    val imageIndex: Int,
    val imageUri: String,
    val text: String,
    val blocks: List<OcrTextBlock>,
)

data class OcrTextBlock(
    val text: String,
    val lines: List<String>,
)

enum class ImagePermissionLevel(
    @StringRes val labelResId: Int,
    @StringRes val descriptionResId: Int,
) {
    Full(
        labelResId = R.string.demo_image_permission_full_label,
        descriptionResId = R.string.demo_image_permission_full_description,
    ),
    Selected(
        labelResId = R.string.demo_image_permission_selected_label,
        descriptionResId = R.string.demo_image_permission_selected_description,
    ),
    Denied(
        labelResId = R.string.demo_image_permission_denied_label,
        descriptionResId = R.string.demo_image_permission_denied_description,
    ),
}
