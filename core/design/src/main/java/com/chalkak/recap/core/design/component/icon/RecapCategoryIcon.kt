package com.chalkak.recap.core.design.component.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapIconBackground

/**
 * 카테고리 아이콘을 background 컨테이너 안에 표시한다.
 *
 * - 아이콘: [RecapCategoryType.borderColor] (500)
 * - 컨테이너 배경: [RecapBackground]
 * - 컨테이너 곡률: 27.dp
 * - 아이콘 크기: 30.dp
 * - 컨테이너 크기: [RecapCategoryIconSize.Compact] 61.dp / [RecapCategoryIconSize.Large] 71.dp
 */
@Composable
fun RecapCategoryIcon(
    category: RecapCategoryType,
    size: RecapCategoryIconSize,
    modifier: Modifier = Modifier,
) {
    val containerShape = RoundedCornerShape(RecapCategoryIconTokens.CornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.containerSize)
            .clip(containerShape)
            .background(RecapIconBackground),
    ) {
        Icon(
            painter = painterResource(category.iconResId),
            contentDescription = null,
            tint = category.borderColor,
            modifier = Modifier.size(RecapCategoryIconTokens.IconSize),
        )
    }
}

enum class RecapCategoryIconSize(
    val containerSize: Dp,
) {
    /** 보관함 리스트형 카테고리 아이콘 */
    Compact(61.dp),

    /** 홈 자주 정리한 캡처 아이콘 */
    Large(71.dp),
}

private object RecapCategoryIconTokens {
    val IconSize = 30.dp
    val CornerRadius = 27.dp
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Recap Category Icon Compact", showBackground = true, widthDp = 320, heightDp = 280)
@Composable
private fun RecapCategoryIconCompactPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapCategoryIconPreviewContent(size = RecapCategoryIconSize.Compact)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Recap Category Icon Large", showBackground = true, widthDp = 360, heightDp = 300)
@Composable
private fun RecapCategoryIconLargePreview() {
    RECAPTheme(dynamicColor = false) {
        RecapCategoryIconPreviewContent(size = RecapCategoryIconSize.Large)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecapCategoryIconPreviewContent(size: RecapCategoryIconSize) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "${size.name} (${size.containerSize})",
            color = RecapGray900,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecapCategoryType.entries.forEach { category ->
                RecapCategoryIcon(
                    category = category,
                    size = size,
                )
            }
        }
    }
}
