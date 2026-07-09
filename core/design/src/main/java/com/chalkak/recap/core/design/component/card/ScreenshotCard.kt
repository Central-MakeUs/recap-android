package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun ScreenshotCard(
    thumbnailModel: Any?,
    title: String,
    description: String,
    organizedDate: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailContentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ScreenshotCardTokens.ContainerCornerRadius),
        color = Color.Transparent,
        contentColor = RecapGray900,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ScreenshotCardTokens.ContainerHorizontalPadding,
                vertical = ScreenshotCardTokens.ContainerVerticalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(ScreenshotCardTokens.ContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = thumbnailContentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(ScreenshotCardTokens.ThumbnailSize)
                    .clip(RoundedCornerShape(ScreenshotCardTokens.ThumbnailCornerRadius)),
            )
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(end = ScreenshotCardTokens.FavoriteIconTouchSize),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = RecapGray900,
                        maxLines = ScreenshotCardTokens.TitleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = description,
                        modifier = Modifier.padding(
                            top = ScreenshotCardTokens.TitleToDescriptionSpacing,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = RecapGray500,
                        maxLines = ScreenshotCardTokens.DescriptionMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = organizedDate,
                        modifier = Modifier.padding(
                            top = ScreenshotCardTokens.DescriptionToOrganizedDateSpacing,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = RecapGray300,
                        maxLines = ScreenshotCardTokens.OrganizedDateMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                ScreenshotCardStarButton(
                    isFavorite = isFavorite,
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
    }
}

@Composable
private fun ScreenshotCardStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val favoriteContentDescription = stringResource(
        if (isFavorite) {
            R.string.favorite_category_card_remove_favorite_content_description
        } else {
            R.string.favorite_category_card_add_favorite_content_description
        },
    )

    Icon(
        painter = painterResource(R.drawable.ic_star_24),
        contentDescription = favoriteContentDescription,
        modifier = modifier
            .size(ScreenshotCardTokens.FavoriteIconTouchSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(ScreenshotCardTokens.FavoriteIconPadding)
            .size(ScreenshotCardTokens.FavoriteIconSize),
        tint = if (isFavorite) {
            RecapBlue500
        } else {
            RecapGray200
        },
    )
}

private object ScreenshotCardTokens {
    val ContainerCornerRadius = 0.dp // 곡률 없음
    val ContainerHorizontalPadding = 16.dp
    val ContainerVerticalPadding = 10.dp
    val ContentSpacing = 15.dp
    val ThumbnailSize = 68.dp
    val ThumbnailCornerRadius = 12.dp
    val TitleToDescriptionSpacing = 2.dp
    val DescriptionToOrganizedDateSpacing = 8.dp
    val FavoriteIconTouchSize = 32.dp
    val FavoriteIconPadding = 4.dp
    val FavoriteIconSize = 24.dp
    const val TitleMaxLines = 1
    const val DescriptionMaxLines = 2
    const val OrganizedDateMaxLines = 1
}

@Preview(name = "Screenshot Card", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotCardPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            title = ScreenshotCardPreviewTitle,
            description = ScreenshotCardPreviewDescription,
            organizedDate = ScreenshotCardPreviewOrganizedDate,
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Screenshot Card favorited", showBackground = false, widthDp = 360)
@Composable
private fun ScreenshotCardFavoritedPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            title = ScreenshotCardPreviewTitle,
            description = ScreenshotCardPreviewDescription,
            organizedDate = ScreenshotCardPreviewOrganizedDate,
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val ScreenshotCardPreviewTitle = "연말정산 서류 목록"
private const val ScreenshotCardPreviewDescription = "연말정산 제출에 필요한 서류 정리"
private const val ScreenshotCardPreviewOrganizedDate = "6월 27일 정리"
