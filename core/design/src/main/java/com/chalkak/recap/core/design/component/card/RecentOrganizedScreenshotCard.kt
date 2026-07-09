package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.chip.RecapCategoryChip
import com.chalkak.recap.core.design.component.chip.RecapCategoryChipType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun RecentOrganizedScreenshotCard(
    thumbnailModel: Any?,
    title: String,
    categoryType: RecapCategoryChipType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailContentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(RecentOrganizedScreenshotCardTokens.CardWidth),
        shape = RoundedCornerShape(RecentOrganizedScreenshotCardTokens.ContainerCornerRadius),
        color = Color.Transparent,
        contentColor = RecapGray900,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(RecentOrganizedScreenshotCardTokens.ContentSpacing),
        ) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = thumbnailContentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(RecentOrganizedScreenshotCardTokens.ThumbnailCornerRadius)),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
                maxLines = RecentOrganizedScreenshotCardTokens.TitleMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            RecapCategoryChip(type = categoryType)
        }
    }
}

private object RecentOrganizedScreenshotCardTokens {
    val CardWidth = 111.dp
    val ContainerCornerRadius = 12.dp
    val ContentSpacing = 8.dp
    val ThumbnailCornerRadius = 12.dp
    const val TitleMaxLines = 1
}

@Preview(name = "Recent Organized Screenshot Card", showBackground = false)
@Composable
private fun RecentOrganizedScreenshotCardPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            title = RecentOrganizedScreenshotCardPreviewTitle,
            categoryType = RecapCategoryChipType.ShoppingProduct,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val RecentOrganizedScreenshotCardPreviewTitle = "택배 반품 절차 정리"
