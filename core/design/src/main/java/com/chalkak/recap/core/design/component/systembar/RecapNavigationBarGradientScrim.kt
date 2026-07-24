package com.chalkak.recap.core.design.component.systembar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground

/**
 * 시스템 네비게이션 바 영역([WindowInsets.navigationBars]) 높이에 맞춰
 * 하단은 불투명 [scrimColor], 상단은 투명한 세로 그라데이션을 그린다.
 * 터치는 하위 UI로 전달한다.
 */
@Composable
fun RecapNavigationBarGradientScrim(
    modifier: Modifier = Modifier,
    scrimColor: Color = RecapBackground,
) {
    val density = LocalDensity.current
    val navigationBarHeightPx = WindowInsets.navigationBars.getBottom(density)
    if (navigationBarHeightPx <= 0) {
        return
    }
    val navigationBarHeight = with(density) { navigationBarHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navigationBarHeight)
            .background(
                brush = Brush.verticalGradient(
                    0f to scrimColor.copy(alpha = 0f),
                    1f to scrimColor,
                ),
            )
            .pointerInteropFilter { false },
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF808080)
@Composable
private fun RecapNavigationBarGradientScrimPreview() {
    RECAPTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            RecapNavigationBarGradientScrim(
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
