package com.chalkak.recap.feature.home

import androidx.annotation.StringRes
import com.chalkak.recap.R
import com.chalkak.recap.core.model.OcrJob

data class HomeUiState(
    @get:StringRes val titleResId: Int = R.string.home_title,
    @get:StringRes val descriptionResId: Int = R.string.home_description,
    val latestOcrJob: OcrJob? = null,
)

sealed interface HomeAction {
    data object StartImport : HomeAction
    data object EnterDeveloperOptions : HomeAction
}
