package com.chalkak.recap.feature.mypage

import androidx.annotation.StringRes
import com.chalkak.recap.R

data class MyPageUiState(
    @get:StringRes val titleResId: Int = R.string.my_page_title,
    @get:StringRes val descriptionResId: Int = R.string.my_page_description,
)

sealed interface MyPageAction {
    data object OpenSettings : MyPageAction
}
