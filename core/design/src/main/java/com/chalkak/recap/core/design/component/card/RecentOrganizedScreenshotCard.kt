package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.chip.RecapCategoryTextChip
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1

@Composable
fun RecentOrganizedScreenshotCard(
    thumbnailModel: Any?,
    title: String,
    categoryType: RecapCategoryType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailContentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(RecentOrganizedScreenshotCardTokens.CardWidth),
        shape = RoundedCornerShape(RecentOrganizedScreenshotCardTokens.ContainerCornerRadius),
        color = RecapBackground,
        contentColor = RecapGray900,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                RecentOrganizedScreenshotCardTokens.ContentSpacing,
            ),
        ) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = thumbnailContentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(width = 134.dp, height = 85.dp)
                    .clip(
                        RoundedCornerShape(
                            RecentOrganizedScreenshotCardTokens.ThumbnailCornerRadius,
                        ),
                    ),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    RecentOrganizedScreenshotCardTokens.TextSpacing,
                ),
            ) {
                RecapCategoryTextChip(type = categoryType)
                Text(
                    text = title,
                    style = RecapCaption1,
                    color = RecapGray900,
                    maxLines = RecentOrganizedScreenshotCardTokens.TitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private object RecentOrganizedScreenshotCardTokens {
    val CardWidth = 134.dp
    val ContainerCornerRadius = 0.dp
    val ContentSpacing = 7.dp
    val TextSpacing = 5.dp
    val ThumbnailCornerRadius = 5.dp
    const val TitleMaxLines = 2
}

@Preview(name = "Recent Organized Screenshot Card", showBackground = true, widthDp = 180)
@Composable
private fun RecentOrganizedScreenshotCardPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotCard(
            thumbnailModel = R.drawable.mock_home_screenshot_recipe,
            title = stringResource(R.string.recent_organized_screenshot_card_preview_title),
            categoryType = RecapCategoryType.InfoKnowledge,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
