package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.chip.RecapCategoryTextChip
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapGray50
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
            RecentOrganizedScreenshotThumbnail(
                thumbnailModel = thumbnailModel,
                thumbnailContentDescription = thumbnailContentDescription,
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

@Composable
private fun RecentOrganizedScreenshotThumbnail(
    thumbnailModel: Any?,
    thumbnailContentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val thumbnailShape = RoundedCornerShape(
        RecentOrganizedScreenshotCardTokens.ThumbnailCornerRadius,
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .size(
                width = RecentOrganizedScreenshotCardTokens.ThumbnailWidth,
                height = RecentOrganizedScreenshotCardTokens.ThumbnailHeight,
            )
            .clip(thumbnailShape)
            .background(RecapGray50),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.recap_placeholder_1),
            contentDescription = null,
        )
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(thumbnailModel)
                .crossfade(RecentOrganizedScreenshotCardTokens.ImageCrossfadeMillis)
                .build(),
            contentDescription = thumbnailContentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private object RecentOrganizedScreenshotCardTokens {
    val CardWidth = 134.dp
    val ThumbnailWidth = 134.dp
    val ThumbnailHeight = 85.dp
    val ContainerCornerRadius = 0.dp
    val ContentSpacing = 7.dp
    val TextSpacing = 5.dp
    val ThumbnailCornerRadius = 5.dp
    const val TitleMaxLines = 2
    const val ImageCrossfadeMillis = 150
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

@Preview(
    name = "Recent Organized Screenshot Card placeholder",
    showBackground = true,
    widthDp = 180
)
@Composable
private fun RecentOrganizedScreenshotCardPlaceholderPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotCard(
            thumbnailModel = null,
            title = stringResource(R.string.recent_organized_screenshot_card_preview_title),
            categoryType = RecapCategoryType.InfoKnowledge,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
