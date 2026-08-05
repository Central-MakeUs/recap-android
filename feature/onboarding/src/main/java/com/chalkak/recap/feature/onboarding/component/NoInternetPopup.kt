package com.chalkak.recap.feature.onboarding.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.White

@Composable
internal fun NoInternetPopup(
    onDismissRequest: () -> Unit,
) {
    RecapPopup(
        title = stringResource(R.string.no_internet_popup_title),
        description = stringResource(R.string.no_internet_popup_description),
        confirmButtonText = stringResource(R.string.no_internet_popup_confirm),
        onConfirmClick = onDismissRequest,
        onDismissRequest = onDismissRequest,
        confirmButtonColor = RecapBlue300,
        confirmButtonContentColor = White,
    )
}

@Preview(name = "No Internet Popup", showBackground = true)
@Composable
private fun NoInternetPopupPreview() {
    RECAPTheme {
        NoInternetPopup(onDismissRequest = {})
    }
}
