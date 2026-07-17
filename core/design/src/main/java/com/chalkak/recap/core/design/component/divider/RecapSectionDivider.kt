package com.chalkak.recap.core.design.component.divider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray100

@Composable
fun RecapSectionDivider(
    modifier: Modifier = Modifier,
    height: Dp = RecapSectionDividerDefaults.Height,
    color: Color = RecapSectionDividerDefaults.Color,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(color),
    )
}

object RecapSectionDividerDefaults {
    val Height = 8.dp
    val Color = RecapGray100
}

@Preview(name = "RecapSectionDivider", showBackground = true, widthDp = 360)
@Composable
private fun RecapSectionDividerPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapSectionDivider()
    }
}
